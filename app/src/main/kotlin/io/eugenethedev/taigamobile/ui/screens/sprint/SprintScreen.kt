package io.eugenethedev.taigamobile.ui.screens.sprint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.Sprint
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.ui.components.appbars.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.components.dialogs.ConfirmActionDialog
import io.eugenethedev.taigamobile.ui.components.dialogs.EditSprintDialog
import io.eugenethedev.taigamobile.ui.components.dialogs.LoadingDialog
import io.eugenethedev.taigamobile.ui.components.loaders.CircularLoader
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.dialogTonalElevation
import io.eugenethedev.taigamobile.ui.utils.LoadingResult
import io.eugenethedev.taigamobile.core.nav.NavigateToTask
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError
import io.eugenethedev.taigamobile.ui.utils.SuccessResult
import io.eugenethedev.taigamobile.core.nav.navigateToCreateTaskScreen
import io.eugenethedev.taigamobile.core.nav.navigateToTaskScreen
import io.eugenethedev.taigamobile.ui.utils.surfaceColorAtElevationInternal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SprintScreen(
    viewModel: SprintViewModel = hiltViewModel(),
    navController: NavController,
    sprintId: Long,
    showMessage: (message: Int) -> Unit = {},
) {
    LaunchedEffect(Unit) {
        viewModel.onOpen(sprintId)
    }

    val sprint by viewModel.sprint.collectAsState()
    sprint.SubscribeOnError(showMessage)

    val statuses by viewModel.statuses.collectAsState()
    statuses.SubscribeOnError(showMessage)

    val storiesWithTasks by viewModel.storiesWithTasks.collectAsState()
    storiesWithTasks.SubscribeOnError(showMessage)

    val storylessTasks by viewModel.storylessTasks.collectAsState()
    storylessTasks.SubscribeOnError(showMessage)

    val issues by viewModel.issues.collectAsState()
    issues.SubscribeOnError(showMessage)

    val editResult by viewModel.editResult.collectAsState()
    editResult.SubscribeOnError(showMessage)

    val deleteResult by viewModel.deleteResult.collectAsState()
    deleteResult.SubscribeOnError(showMessage)
    deleteResult.takeIf { it is SuccessResult }?.let {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    SprintScreenContent(
        sprint = sprint.data,
        isLoading = sprint is LoadingResult,
        isEditLoading = editResult is LoadingResult,
        isDeleteLoading = deleteResult is LoadingResult,
        statuses = statuses.data.orEmpty(),
        storiesWithTasks = storiesWithTasks.data.orEmpty(),
        storylessTasks = storylessTasks.data.orEmpty(),
        issues = issues.data.orEmpty(),
        editSprint = viewModel::editSprint,
        deleteSprint = viewModel::deleteSprint,
        navigateBack = navController::popBackStack,
        navigateToTask = navController::navigateToTaskScreen,
        navigateToCreateTask = { type, parentId ->
            navController.navigateToCreateTaskScreen(
                type,
                parentId,
                sprintId
            )
        }
    )
}


@Composable
fun SprintScreenContent(
    sprint: Sprint?,
    isLoading: Boolean = false,
    isEditLoading: Boolean = false,
    isDeleteLoading: Boolean = false,
    statuses: List<Status> = emptyList(),
    storiesWithTasks: Map<CommonTask, List<CommonTask>> = emptyMap(),
    storylessTasks: List<CommonTask> = emptyList(),
    issues: List<CommonTask> = emptyList(),
    editSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> },
    deleteSprint: () -> Unit = {},
    navigateBack: () -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> },
    navigateToCreateTask: (type: CommonTaskType, parentId: Long?) -> Unit = { _, _ -> }
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    var isMenuExpanded by remember { mutableStateOf(false) }
    AppBarWithBackButton(
        title = {
            Column {
                Text(
                    text = sprint?.name.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.sprint_dates_template).format(
                        sprint?.start?.format(dateFormatter).orEmpty(),
                        sprint?.end?.format(dateFormatter).orEmpty()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_options),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // delete alert dialog
                var isDeleteAlertVisible by remember { mutableStateOf(false) }
                if (isDeleteAlertVisible) {
                    ConfirmActionDialog(
                        title = stringResource(R.string.delete_sprint_title),
                        text = stringResource(R.string.delete_sprint_text),
                        onConfirm = {
                            isDeleteAlertVisible = false
                            deleteSprint()
                        },
                        onDismiss = { isDeleteAlertVisible = false },
                        iconId = R.drawable.ic_delete
                    )
                }

                var isEditDialogVisible by remember { mutableStateOf(false) }
                if (isEditDialogVisible) {
                    EditSprintDialog(
                        initialName = sprint?.name.orEmpty(),
                        initialStart = sprint?.start,
                        initialEnd = sprint?.end,
                        onConfirm = { name, start, end ->
                            editSprint(name, start, end)
                            isEditDialogVisible = false
                        },
                        onDismiss = { isEditDialogVisible = false }
                    )
                }

                DropdownMenu(
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceColorAtElevationInternal(
                            dialogTonalElevation
                        )
                    ),
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    // edit
                    DropdownMenuItem(
                        onClick = {
                            isMenuExpanded = false
                            isEditDialogVisible = true
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.edit),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )

                    // delete
                    DropdownMenuItem(
                        onClick = {
                            isMenuExpanded = false
                            isDeleteAlertVisible = true
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.delete),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        },
        navigateBack = navigateBack
    )

    when {
        isLoading || sprint == null -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularLoader()
        }

        isEditLoading || isDeleteLoading -> LoadingDialog()

        else -> SprintKanban(
            statuses = statuses,
            storiesWithTasks = storiesWithTasks,
            storylessTasks = storylessTasks,
            issues = issues,
            navigateToTask = navigateToTask,
            navigateToCreateTask = navigateToCreateTask
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SprintScreenPreview() = TaigaMobileTheme {
    SprintScreenContent(
        sprint = Sprint(
            id = 0L,
            name = "0 sprint",
            start = LocalDate.now(),
            end = LocalDate.now(),
            order = 0,
            storiesCount = 0,
            isClosed = false
        )
    )
}
