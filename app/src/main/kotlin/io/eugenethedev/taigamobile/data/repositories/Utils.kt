package io.eugenethedev.taigamobile.data.repositories

import io.eugenethedev.taigamobile.data.api.CommonTaskResponse
import io.eugenethedev.taigamobile.data.api.SprintResponse
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.Sprint
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.StatusType
import io.eugenethedev.taigamobile.domain.entities.Tag
import io.eugenethedev.taigamobile.ui.theme.taigaGray
import io.eugenethedev.taigamobile.ui.utils.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)

inline fun <T> handle404(action: () -> List<T>): List<T> = try {
    action()
} catch (e: HttpException) {
    // suppress error if page not found (maximum page was reached)
    e.takeIf { it.code() == 404 }?.let { emptyList() } ?: throw e
}

fun CommonTaskResponse.toCommonTask(commonTaskType: CommonTaskType) = CommonTask(
    id = id,
    createdDate = created_date,
    title = subject,
    ref = ref,
    status = Status(
        id = status,
        name = status_extra_info.name,
        color = status_extra_info.color,
        type = StatusType.Status
    ),
    assignee = assigned_to_extra_info,
    projectInfo = project_extra_info,
    taskType = commonTaskType,
    colors = color?.let { listOf(it) } ?: epics.orEmpty().map { it.color },
    isClosed = is_closed,
    tags = tags.orEmpty().map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
    blockedNote = blocked_note.takeIf { is_blocked }
)

private val taigaGrayHex by lazy { taigaGray.toHex() }
fun String?.fixNullColor() =
    this ?: taigaGrayHex // gray, because api returns null instead of gray -_-

fun SprintResponse.toSprint() = Sprint(
    id = id,
    name = name,
    order = order,
    start = estimated_start,
    end = estimated_finish,
    storiesCount = user_stories.size,
    isClosed = closed
)
