package io.eugenethedev.taigamobile.ui.screens.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.state.ThemeSetting
import io.eugenethedev.taigamobile.ui.components.appbars.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox
import io.eugenethedev.taigamobile.ui.screens.commontask.CommonTaskScreen
import io.eugenethedev.taigamobile.ui.screens.createtask.CreateTaskScreen
import io.eugenethedev.taigamobile.ui.screens.dashboard.DashboardScreen
import io.eugenethedev.taigamobile.ui.screens.epics.EpicsScreen
import io.eugenethedev.taigamobile.ui.screens.issues.IssuesScreen
import io.eugenethedev.taigamobile.ui.screens.kanban.KanbanScreen
import io.eugenethedev.taigamobile.ui.screens.login.LoginScreen
import io.eugenethedev.taigamobile.ui.screens.profile.ProfileScreen
import io.eugenethedev.taigamobile.ui.screens.projectselector.ProjectSelectorScreen
import io.eugenethedev.taigamobile.ui.screens.scrum.ScrumScreen
import io.eugenethedev.taigamobile.ui.screens.settings.SettingsScreen
import io.eugenethedev.taigamobile.ui.screens.sprint.SprintScreen
import io.eugenethedev.taigamobile.ui.screens.team.TeamScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.createpage.WikiCreatePageScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.list.WikiListScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.page.WikiPageScreen
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileRippleTheme
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import kotlinx.coroutines.launch
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    @SuppressLint("Range")
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        val inputStream = contentResolver.openInputStream(it) ?: return@registerForActivityResult
        val fileName = contentResolver.query(it, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: return@registerForActivityResult

        filePicker.filePicked(fileName, inputStream)
    }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (String, InputStream) -> Unit) {
            super.requestFile(onFilePicked)
            getContent.launch("*/*")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val navController = rememberNavController()

            val viewModel: MainViewModel = viewModel()
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeSetting.Light -> false
                ThemeSetting.Dark -> true
                ThemeSetting.System -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                        android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b),
                    ) { darkTheme },
                )
                onDispose {}
            }

            TaigaMobileTheme(darkTheme) {
                CompositionLocalProvider(
                    LocalFilePicker provides filePicker,
                    LocalRippleTheme provides TaigaMobileRippleTheme
                ) {

                    // use Scaffold from material2, because material3 Scaffold lacks some functionality
                    androidx.compose.material.Scaffold(
                        scaffoldState = scaffoldState,
                        snackbarHost = {
                            SnackbarHost(
                                hostState = it,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                Snackbar(
                                    snackbarData = it,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
                                    shape = MaterialTheme.shapes.small
                                )
                            }
                        },
                        bottomBar = {
                            val items = Screens.entries.toTypedArray()
                            val routes = items.map { it.route }
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute =
                                navBackStackEntry?.destination?.hierarchy?.first()?.route

                            // hide bottom bar for other screens
                            if (currentRoute !in routes) return@Scaffold

                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        modifier = Modifier
                                            .clip(CircleShape),
                                        icon = {
                                            Icon(
                                                painter = painterResource(screen.iconId),
                                                contentDescription = null,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = { Text(stringResource(screen.resourceId)) },
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            if (screen.route != currentRoute) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        content = {
                            MainScreen(
                                viewModel = viewModel,
                                scaffoldState = scaffoldState,
                                paddingValues = it,
                                navController = navController
                            )
                        }
                    )
                }
            }
        }
    }
}

enum class Screens(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int
) {
    Dashboard(Routes.dashboard, R.string.dashboard_short, R.drawable.ic_dashboard),
    Scrum(Routes.scrum, R.string.scrum, R.drawable.ic_scrum),
    Epics(Routes.epics, R.string.epics, R.drawable.ic_epics),
    Issues(Routes.issues, R.string.issues, R.drawable.ic_issues),
    More(Routes.more, R.string.more, R.drawable.ic_more)
}

object Routes {
    const val login = "login"
    const val dashboard = "dashboard"
    const val scrum = "scrum"
    const val epics = "epics"
    const val issues = "issues"
    const val more = "more"
    const val team = "team"
    const val settings = "settings"
    const val kanban = "kanban"
    const val wiki_selector = "wiki_selector"
    const val wiki_page = "wiki_page"
    const val wiki_create_page = "wiki_create_page"
    const val projectsSelector = "projectsSelector"
    const val sprint = "sprint"
    const val commonTask = "commonTask"
    const val createTask = "createTask"
    const val profile = "profile"

    object Arguments {
        const val sprint = "sprint"
        const val sprintId = "sprintId"
        const val swimlaneId = "swimlaneId"
        const val commonTaskId = "taskId"
        const val commonTaskType = "taskType"
        const val ref = "ref"
        const val parentId = "parentId"
        const val statusId = "statusId"
        const val userId = "userId"
        const val wikiSlug = "wikiSlug"
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    scaffoldState: ScaffoldState,
    paddingValues: PaddingValues,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val showMessage: (Int) -> Unit = { message ->
        val strMessage = context.getString(message)
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(strMessage)
        }
    }

    val isLogged by viewModel.isLogged.collectAsState()
    val isProjectSelected by viewModel.isProjectSelected.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = remember { if (isLogged) Routes.dashboard else Routes.login }
        ) {
            composable(Routes.login) {
                LoginScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            // start screen
            composable(Routes.dashboard) {
                DashboardScreen(
                    navController = navController,
                    showMessage = showMessage
                )
                // user must select project first
                LaunchedEffect(Unit) {
                    if (!isProjectSelected) {
                        navController.navigate(Routes.projectsSelector)
                    }
                }
            }

            composable(Routes.scrum) {
                ScrumScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.epics) {
                EpicsScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.issues) {
                IssuesScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.more) {
                MoreScreen(
                    navController = navController
                )
            }

            composable(Routes.team) {
                TeamScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.kanban) {
                KanbanScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.wiki_selector) {
                WikiListScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.wiki_create_page) {
                WikiCreatePageScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(
                "${Routes.wiki_page}/{${Routes.Arguments.wikiSlug}}",
                arguments = listOf(
                    navArgument(Routes.Arguments.wikiSlug) { type = NavType.StringType }
                )
            ) {
                WikiPageScreen(
                    slug = it.arguments!!.getString(Routes.Arguments.wikiSlug).orEmpty(),
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.settings) {
                SettingsScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(Routes.projectsSelector) {
                ProjectSelectorScreen(
                    navController = navController,
                    showMessage = showMessage
                )
            }

            composable(
                "${Routes.sprint}/{${Routes.Arguments.sprintId}}",
                arguments = listOf(
                    navArgument(Routes.Arguments.sprintId) { type = NavType.LongType }
                )
            ) {
                SprintScreen(
                    navController = navController,
                    sprintId = it.arguments!!.getLong(Routes.Arguments.sprintId),
                    showMessage = showMessage
                )
            }

            composable(
                "${Routes.profile}/{${Routes.Arguments.userId}}",
                arguments = listOf(
                    navArgument(Routes.Arguments.userId) { type = NavType.LongType }
                )
            ) {
                ProfileScreen(
                    navController = navController,
                    showMessage = showMessage,
                    userId = it.arguments!!.getLong(Routes.Arguments.userId),
                )
            }

            composable(
                Routes.Arguments.run { "${Routes.commonTask}/{$commonTaskId}/{$commonTaskType}/{$ref}" },
                arguments = listOf(
                    navArgument(Routes.Arguments.commonTaskType) { type = NavType.StringType },
                    navArgument(Routes.Arguments.commonTaskId) { type = NavType.LongType },
                    navArgument(Routes.Arguments.ref) { type = NavType.IntType },
                )
            ) {
                CommonTaskScreen(
                    navController = navController,
                    commonTaskId = it.arguments!!.getLong(Routes.Arguments.commonTaskId),
                    commonTaskType = CommonTaskType.valueOf(
                        it.arguments!!.getString(
                            Routes.Arguments.commonTaskType,
                            ""
                        )
                    ),
                    ref = it.arguments!!.getInt(Routes.Arguments.ref),
                    showMessage = showMessage
                )
            }

            composable(
                Routes.Arguments.run { "${Routes.createTask}/{$commonTaskType}?$parentId={$parentId}&$sprintId={$sprintId}&$statusId={$statusId}&$swimlaneId={$swimlaneId}" },
                arguments = listOf(
                    navArgument(Routes.Arguments.commonTaskType) { type = NavType.StringType },
                    navArgument(Routes.Arguments.parentId) {
                        type = NavType.LongType
                        defaultValue = -1L // long does not allow null values
                    },
                    navArgument(Routes.Arguments.sprintId) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Routes.Arguments.statusId) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Routes.Arguments.swimlaneId) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                )
            ) {
                CreateTaskScreen(
                    navController = navController,
                    commonTaskType = CommonTaskType.valueOf(
                        it.arguments!!.getString(
                            Routes.Arguments.commonTaskType,
                            ""
                        )
                    ),
                    parentId = it.arguments!!.getLong(Routes.Arguments.parentId).takeIf { it >= 0 },
                    sprintId = it.arguments!!.getLong(Routes.Arguments.sprintId).takeIf { it >= 0 },
                    statusId = it.arguments!!.getLong(Routes.Arguments.statusId).takeIf { it >= 0 },
                    swimlaneId = it.arguments!!.getLong(Routes.Arguments.swimlaneId)
                        .takeIf { it >= 0 },
                    showMessage = showMessage
                )
            }
        }
    }
}

@Composable
fun MoreScreen(
    navController: NavController
) = Column(Modifier.fillMaxSize()) {
    AppBarWithBackButton(
        title = { Text(stringResource(R.string.more)) }
    )

    @Composable
    fun Item(
        @DrawableRes iconId: Int,
        @StringRes nameId: Int,
        route: String
    ) = ContainerBox(onClick = { navController.navigate(route) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.width(8.dp))

            Text(stringResource(nameId))
        }
    }

    val space = 2.dp

    Item(R.drawable.ic_team, R.string.team, Routes.team)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_kanban, R.string.kanban, Routes.kanban)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_wiki, R.string.wiki, Routes.wiki_selector)
    Spacer(Modifier.height(space))
    Item(R.drawable.ic_settings, R.string.settings, Routes.settings)
}