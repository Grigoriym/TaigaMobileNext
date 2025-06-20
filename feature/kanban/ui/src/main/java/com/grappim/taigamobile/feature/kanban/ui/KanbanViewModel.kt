package com.grappim.taigamobile.feature.kanban.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.subscribeToAll
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.NothingResult
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val usersRepository: UsersRepository,
    session: Session
) : ViewModel() {

    val statuses = mutableResultFlow<List<Status>>()
    val team = mutableResultFlow<List<User>>()
    val stories = mutableResultFlow<List<CommonTaskExtended>>()
    val swimlanes = mutableResultFlow<List<Swimlane?>>()

    val selectedSwimlane = MutableStateFlow<Swimlane?>(null)

    private var shouldReload = true

    fun onOpen() = viewModelScope.launch {
        if (!shouldReload) return@launch
        joinAll(
            launch {
                statuses.loadOrError(preserveValue = false) {
                    tasksRepository.getStatuses(
                        CommonTaskType.UserStory
                    )
                }
            },
            launch {
                team.loadOrError(preserveValue = false) {
                    usersRepository.getTeam().map { it.toUser() }
                }
            },
            launch {
                stories.loadOrError(preserveValue = false) { tasksRepository.getAllUserStories() }
            },
            // prepend null to show "unclassified" swimlane
            launch {
                swimlanes.loadOrError {
                    listOf(null) + tasksRepository.getSwimlanes()
                }
            }
        )
        shouldReload = false
    }

    fun selectSwimlane(swimlane: Swimlane?) {
        selectedSwimlane.value = swimlane
    }

    init {
        viewModelScope.subscribeToAll(session.currentProjectId, session.taskEdit) {
            statuses.value = NothingResult()
            team.value = NothingResult()
            stories.value = NothingResult()
            swimlanes.value = NothingResult()
            shouldReload = true
        }
    }
}
