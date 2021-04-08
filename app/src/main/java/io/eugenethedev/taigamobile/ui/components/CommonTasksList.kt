@file:Suppress("FunctionName")

package io.eugenethedev.taigamobile.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.ui.components.loaders.DotsLoader
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.NavigateToTask
import io.eugenethedev.taigamobile.ui.utils.clickableUnindicated
import java.text.SimpleDateFormat
import java.util.*

/**
 * View for displaying list of stories or tasks
 */
@ExperimentalAnimationApi
fun LazyListScope.CommonTasksList(
    statuses: List<Status>,
    commonTasks: List<CommonTask>,
    loadData: (Status) -> Unit = {},
    loadingStatusIds: List<Long> = emptyList(),
    visibleStatusIds: List<Long> = emptyList(),
    onStatusClick: (Long) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> },
    isInverseVisibility: Boolean = false
) {
    if (statuses.isNotEmpty()) {
        statuses.map { st -> st to commonTasks.filter { it.status.id == st.id } }.forEach { (status, stories) ->
            val isCategoryVisible = (status.id in visibleStatusIds && !isInverseVisibility) || (status.id !in visibleStatusIds && isInverseVisibility)
            val isCategoryLoading = status.id in loadingStatusIds

            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = mainHorizontalScreenPadding)
                        .padding(top = 12.dp),
                    contentColor = Color(android.graphics.Color.parseColor(status.color))
                ) {
                    val transitionState = remember { MutableTransitionState(isCategoryVisible) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickableUnindicated {
                            transitionState.targetState = !isCategoryVisible
                            onStatusClick(status.id)
                        }
                    ) {

                        Text(
                            text = status.name.toUpperCase(Locale.getDefault()),
                            style = MaterialTheme.typography.subtitle1
                        )

                        val arrowRotation by updateTransition(transitionState).animateFloat { if (it) -180f else 0f }

                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_down),
                            contentDescription = null,
                            tint = LocalContentColor.current,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            itemsIndexed(stories.toList()) { index, commonTask ->

                @Composable
                fun Item() = Column(modifier = Modifier.fillMaxWidth()) {
                    CommonTaskItem(
                        commonTask = commonTask,
                        navigateToTask = navigateToTask
                    )

                    if (index < stories.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.LightGray
                        )
                    }
                }

                if (index < 10) {
                    AnimateExpandVisibility(
                        visible = isCategoryVisible,
                        initiallyVisible = !isCategoryLoading && isCategoryVisible
                    ) {
                        Item()
                    }
                } else if (isCategoryVisible) {
                    Item()
                }
            }

            item {
                AnimateExpandVisibility(visible = isCategoryVisible && isCategoryLoading) {
                    DotsLoader()
                }

                AnimateExpandVisibility(visible = isCategoryVisible) {
                    Spacer(Modifier.height(8.dp))
                }

                if (isCategoryVisible) {
                    LaunchedEffect(stories.size) {
                        loadData(status)
                    }
                }
            }
        }

    } else {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.nothing_to_see),
                    color = Color.Gray
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun AnimateExpandVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    initiallyVisible: Boolean = visible,
    content: @Composable () -> Unit = {}
) = AnimatedVisibility(
    visible = visible,
    enter = expandVertically(),
    exit = shrinkOut(shrinkTowards = Alignment.TopStart),
    modifier = modifier,
    initiallyVisible = initiallyVisible,
    content = content
)

/**
 * Single task item
 */
@Composable
fun CommonTaskItem(
    commonTask: CommonTask,
    horizontalPadding: Dp = mainHorizontalScreenPadding,
    verticalPadding: Dp = 8.dp,
    navigateToTask: NavigateToTask = { _, _, _ -> }
) = ContainerBox(
    horizontalPadding, verticalPadding,
    onClick = { navigateToTask(commonTask.id, commonTask.taskType, commonTask.ref) }
) {
    val dateFormatter = remember { SimpleDateFormat.getDateInstance() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = commonTask.status.name,
                color = Color(android.graphics.Color.parseColor(commonTask.status.color)),
                style = MaterialTheme.typography.body2
            )

            Text(
                text = dateFormatter.format(commonTask.createdDate),
                color = Color.Gray,
                style = MaterialTheme.typography.body2
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_with_ref_pattern).format(
                    commonTask.ref, commonTask.title
                ),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.let {
                    if (commonTask.taskType == CommonTaskType.EPIC) {
                        it.weight(0.9f, fill = false).padding(end = 8.dp)
                    } else {
                        it
                    }
                }
            )

            if (commonTask.taskType == CommonTaskType.EPIC) {
                Text(
                    text = stringResource(R.string.epic),
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = commonTask.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Black,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .weight(0.1f, fill = false)
                )
            }
        }

        Text(
            text = commonTask.assignee?.fullName?.let { stringResource(R.string.assignee_pattern).format(it) } ?: stringResource(R.string.unassigned),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTaskItemPreview() = TaigaMobileTheme {
    CommonTaskItem(
        CommonTask(
            id = 0L,
            createdDate = Date(),
            title = "Very cool story",
            ref = 100,
            status = Status(
                id = 0L,
                name = "In progress",
                color = "#729fcf"
            ),
            assignee = CommonTask.Assignee(
                id = 0,
                fullName = "Name Name"
            ),
            projectSlug = "000",
            taskType = CommonTaskType.USERSTORY
        )
    )
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun CommonTasksListPreview() = TaigaMobileTheme {
    var visibleStatusIds by remember { mutableStateOf(listOf<Long>()) }

    LazyColumn {
        CommonTasksList(
            statuses = List(3) {
                Status(
                    id = it.toLong(),
                    name = "In progress",
                    color = "#729fcf"
                )
            },
            commonTasks = List(10) {
                CommonTask(
                    id = it.toLong(),
                    createdDate = Date(),
                    title = "Very cool story",
                    ref = 100,
                    status = Status(
                        id = (0..2).random().toLong(),
                        name = "In progress",
                        color = "#729fcf"
                    ),
                    assignee = CommonTask.Assignee(
                        id = it.toLong(),
                        fullName = "Name Name"
                    ),
                    projectSlug = "000",
                    taskType = CommonTaskType.USERSTORY
                )
            },
            visibleStatusIds = visibleStatusIds,
            onStatusClick = {
                visibleStatusIds = if (it in visibleStatusIds) {
                    visibleStatusIds - it
                } else {
                    visibleStatusIds + it
                }
            }

        )
    }
}