package com.fghilmany.nufitai.data.chatai.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.data.chatai.datasource.ChatApiException
import com.fghilmany.nufitai.data.chatai.datasource.ChatLocalDataSource
import com.fghilmany.nufitai.data.chatai.datasource.ChatQuotaLocalDataSource
import com.fghilmany.nufitai.data.chatai.datasource.ChatRemoteDataSource
import com.fghilmany.nufitai.data.chatai.model.ChatSendRequestDto
import com.fghilmany.nufitai.db.Chat_message as ChatMessageRow
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageRole
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.chatai.entity.ExerciseSuggestion
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository
import com.fghilmany.nufitai.domain.chatai.repository.ChatSendResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class ChatRepositoryImpl(
    private val local: ChatLocalDataSource,
    private val quotaLocal: ChatQuotaLocalDataSource,
    private val remote: ChatRemoteDataSource,
    private val authRepository: AuthRepository,
) : ChatRepository {
    private val logger = AppLogger.tagged("ChatRepository")
    private val json = Json

    override suspend fun getHistory(): AppResult<List<ChatMessage>> = runCatchingDatabase {
        local.getAllMessages().map { it.toEntity() }
    }

    override suspend fun insertMessage(message: ChatMessage): AppResult<Unit> = runCatchingDatabase {
        val userId = authRepository.getCurrentSession()?.userId
        local.upsertMessage(
            id = message.id,
            role = message.role.name,
            text = message.text,
            suggestionExerciseId = message.suggestion?.exerciseId,
            suggestionReplacesExerciseId = message.suggestion?.replacesExerciseId,
            suggestedRepliesJson = message.suggestedReplies.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            status = message.status.name,
            createdAt = message.createdAt.toEpochMilliseconds(),
            userId = userId,
            updatedAt = message.createdAt.toEpochMilliseconds(),
        )
    }

    override suspend fun updateMessage(message: ChatMessage): AppResult<Unit> = insertMessage(message)

    override suspend fun reconcileStuckSendingMessages(): AppResult<Unit> = runCatchingDatabase {
        local.getSendingMessages().forEach { row ->
            updateMessage(row.toEntity().copy(status = ChatMessageStatus.FAILED))
        }
    }

    override suspend fun sendToRemote(text: String): AppResult<ChatSendResult> {
        val session = authRepository.getCurrentSession() ?: return AppResult.Error(Failure.Unauthorized())
        logger.i { "sendToRemote starting" }
        return try {
            val response = remote.send(ChatSendRequestDto(message = text), session.accessToken)
            val result = response.toEntity()
            quotaLocal.setQuota(result.quota.used, result.quota.max, result.quota.resetsAt.toEpochMilliseconds())
            logger.i { "sendToRemote succeeded, quota=${result.quota.used}/${result.quota.max}" }
            AppResult.Success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ChatApiException) {
            logger.e(e) { "sendToRemote failed -- HTTP ${e.statusCode}" }
            when {
                e.statusCode == 401 -> AppResult.Error(Failure.Unauthorized())
                e.quotaExceeded -> AppResult.Error(Failure.QuotaExceeded())
                else -> AppResult.Error(Failure.Network("Chat backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "sendToRemote failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }

    override suspend fun getQuota(): AppResult<ChatQuota?> = runCatchingDatabase {
        val (used, max, resetsAt) = quotaLocal.getQuota() ?: return@runCatchingDatabase null
        ChatQuota(used = used, max = max, resetsAt = Instant.fromEpochMilliseconds(resetsAt))
    }

    private fun ChatMessageRow.toEntity(): ChatMessage = ChatMessage(
        id = id,
        role = ChatMessageRole.valueOf(role),
        text = text,
        suggestion = suggestion_exercise_id?.let { ExerciseSuggestion(exerciseId = it, replacesExerciseId = suggestion_replaces_exercise_id) },
        suggestedReplies = suggested_replies?.let { json.decodeFromString<List<String>>(it) } ?: emptyList(),
        status = ChatMessageStatus.valueOf(status),
        createdAt = Instant.fromEpochMilliseconds(created_at),
    )
}
