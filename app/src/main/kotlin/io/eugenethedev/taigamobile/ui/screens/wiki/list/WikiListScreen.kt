package io.eugenethedev.taigamobile.ui.screens.wiki.list

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.core.nav.Routes
import io.eugenethedev.taigamobile.ui.components.appbars.ClickableAppBar
import io.eugenethedev.taigamobile.ui.components.buttons.PlusButton
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox
import io.eugenethedev.taigamobile.ui.components.containers.HorizontalTabbedPager
import io.eugenethedev.taigamobile.ui.components.containers.Tab
import io.eugenethedev.taigamobile.ui.components.loaders.CircularLoader
import io.eugenethedev.taigamobile.ui.utils.LoadingResult
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError
import io.eugenethedev.taigamobile.ui.utils.navigateToWikiPageScreen

@Composable
fun WikiListScreen(
    navController: NavController,
    showMessage: (message: Int) -> Unit = {},
) {
    val viewModel: WikiListViewModel = viewModel()

    val projectName by viewModel.projectName.collectAsState()

    val wikiLinks by viewModel.wikiLinks.collectAsState()
    wikiLinks.SubscribeOnError(showMessage)

    val wikiPages by viewModel.wikiPages.collectAsState()
    wikiPages.SubscribeOnError(showMessage)

    LaunchedEffect(Unit) {
        viewModel.onOpen()
    }

    val wikiPagesSlug = wikiPages.data.orEmpty().map { it.slug }

    WikiListScreenContent(
        projectName = projectName,
        bookmarks = wikiLinks.data.orEmpty().filter { it.ref in wikiPagesSlug }
            .map { it.title to it.ref },
        allPages = wikiPagesSlug,
        isLoading = wikiLinks is LoadingResult || wikiPages is LoadingResult,
        onTitleClick = { navController.navigate(Routes.projectsSelector) },
        navigateToCreatePage = {
            navController.navigate(Routes.wiki_create_page)
        },
        navigateToPageBySlug = { slug ->
            navController.navigateToWikiPageScreen(slug)
        },
        navigateBack = { navController.popBackStack() }
    )
}

@Composable
fun WikiListScreenContent(
    projectName: String,
    bookmarks: List<Pair<String, String>> = emptyList(),
    allPages: List<String> = emptyList(),
    isLoading: Boolean = false,
    onTitleClick: () -> Unit = {},
    navigateToCreatePage: () -> Unit = {},
    navigateToPageBySlug: (slug: String) -> Unit = {},
    navigateBack: () -> Unit = {}
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    ClickableAppBar(
        projectName = projectName,
        actions = {
            PlusButton(
                onClick = navigateToCreatePage
            )
        },
        onTitleClick = onTitleClick,
        navigateBack = navigateBack
    )

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularLoader()
        }
    }

    if (bookmarks.isEmpty() && allPages.isEmpty()) {
        EmptyWikiDialog(
            createNewPage = navigateToCreatePage
        )
    }
    val tabs = WikiTabs.entries.toTypedArray()

    HorizontalTabbedPager(
        modifier = Modifier.fillMaxSize(),
        tabs = tabs,
        pagerState = rememberPagerState(pageCount = { tabs.size })
    ) { page ->
        when (WikiTabs.entries[page]) {
            WikiTabs.Bookmarks -> WikiSelectorList(
                titles = bookmarks.map { it.first },
                bookmarks = bookmarks,
                onClick = navigateToPageBySlug
            )

            WikiTabs.AllWikiPages -> WikiSelectorList(
                titles = allPages,
                onClick = navigateToPageBySlug
            )
        }
    }
}

private enum class WikiTabs(@StringRes override val titleId: Int) : Tab {
    Bookmarks(R.string.bookmarks),
    AllWikiPages(R.string.all_wiki_pages)
}

@Composable
private fun WikiSelectorList(
    titles: List<String> = emptyList(),
    bookmarks: List<Pair<String, String>> = emptyList(),
    onClick: (name: String) -> Unit = {}
) = Box(
    Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopStart
) {
    val listItemContent: @Composable LazyItemScope.(Int, String) -> Unit = lambda@{ index, item ->
        WikiSelectorItem(
            title = item,
            onClick = { onClick(bookmarks.getOrNull(index)?.second ?: item) }
        )

        if (index < titles.lastIndex) {
            Divider(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            itemsIndexed(titles, itemContent = listItemContent)
        }
    }

    if (titles.isEmpty()) {
        EmptyWikiDialog(
            isButtonAvailable = false
        )
    }
}

@Composable
private fun WikiSelectorItem(
    title: String,
    onClick: () -> Unit = {}
) = ContainerBox(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(0.8f)) {
            Text(
                text = title
            )
        }
    }
}

@Preview
@Composable
fun WikiPageSelectorPreview() {
    WikiListScreenContent(
        projectName = "Cool project"
    )
}

