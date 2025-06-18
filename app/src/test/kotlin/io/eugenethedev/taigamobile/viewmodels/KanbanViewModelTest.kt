package io.eugenethedev.taigamobile.viewmodels

import io.eugenethedev.taigamobile.domain.entities.CommonTaskExtended
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.Swimlane
import io.eugenethedev.taigamobile.domain.entities.TeamMember
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.kanban.KanbanViewModel
import io.eugenethedev.taigamobile.ui.utils.ErrorResult
import io.eugenethedev.taigamobile.ui.utils.SuccessResult
import io.eugenethedev.taigamobile.viewmodels.utils.assertResultEquals
import io.eugenethedev.taigamobile.viewmodels.utils.notFoundException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class KanbanViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: KanbanViewModel

    @BeforeTest
    fun setup() {
        viewModel = KanbanViewModel(mockAppComponent)
    }

    @Test
    fun `test on open`(): Unit = runBlocking {
        val listStatuses = mockk<List<Status>>(relaxed = true)
        val listTeamMembers = mockk<List<TeamMember>>(relaxed = true)
        val listCommonTaskExtended = mockk<List<CommonTaskExtended>>(relaxed = true)
        val listSwimlanes = mockk<List<Swimlane>>(relaxed = true)

        coEvery { mockTaskRepository.getStatuses(any()) } returns listStatuses
        coEvery { mockUsersRepository.getTeam() } returns listTeamMembers
        coEvery { mockTaskRepository.getAllUserStories() } returns listCommonTaskExtended
        coEvery { mockTaskRepository.getSwimlanes() } returns listSwimlanes
        viewModel.onOpen()

        assertResultEquals(SuccessResult(listStatuses), viewModel.statuses.value)
        assertResultEquals(SuccessResult(listTeamMembers.map { it.toUser() }), viewModel.team.value)
        assertResultEquals(SuccessResult(listCommonTaskExtended), viewModel.stories.value)
        assertResultEquals(SuccessResult(listOf(null) + listSwimlanes), viewModel.swimlanes.value)
    }

    @Test
    fun `test on open error`(): Unit = runBlocking {
        coEvery { mockTaskRepository.getStatuses(any()) } throws notFoundException
        coEvery { mockUsersRepository.getTeam() } throws notFoundException
        coEvery { mockTaskRepository.getAllUserStories() } throws notFoundException
        coEvery { mockTaskRepository.getSwimlanes() } throws notFoundException
        viewModel.onOpen()

        assertIs<ErrorResult<List<Status>>>(viewModel.statuses.value)
        assertIs<ErrorResult<List<User>>>(viewModel.team.value)
        assertIs<ErrorResult<List<CommonTaskExtended>>>(viewModel.stories.value)
        assertIs<ErrorResult<List<Swimlane?>>>(viewModel.swimlanes.value)
    }

    @Test
    fun `test select swimlane`(): Unit = runBlocking {
        val mockSwimlane = mockk<Swimlane>(relaxed = true)

        viewModel.selectSwimlane(mockSwimlane)
        assertIs<Swimlane?>(viewModel.selectedSwimlane.value)

        viewModel.selectSwimlane(null)
        assertIs<Swimlane?>(viewModel.selectedSwimlane.value)
    }
}