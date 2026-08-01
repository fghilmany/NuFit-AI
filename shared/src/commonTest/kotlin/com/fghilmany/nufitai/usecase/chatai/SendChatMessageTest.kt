package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageRole
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import com.fghilmany.nufitai.domain.chatai.repository.ChatSendResult
import com.fghilmany.nufitai.fake.FakeChatRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SendChatMessageTest {
    private fun assistantReply() = ChatMessage(
        id = "reply1", role = ChatMessageRole.ASSISTANT, text = "Coba Incline Push Up.",
        suggestion = null, suggestedReplies = listOf("Versi tanpa alat"), status = ChatMessageStatus.SENT, createdAt = currentInstant(),
    )

    @Test
    fun `given the send succeeds when invoked then the user message is SENT and the reply is persisted`() = runTest {
        val repository = FakeChatRepository().apply {
            sendResult = AppResult.Success(ChatSendResult(reply = assistantReply(), quota = ChatQuota(1, 5, currentInstant())))
        }

        val result = SendChatMessage(repository)("Bisa kasih alternatif Push Up?")

        assertIs<AppResult.Success<ChatMessage>>(result)
        assertEquals("reply1", result.data.id)
        val userMessage = repository.messages.values.single { it.role == ChatMessageRole.USER }
        assertEquals(ChatMessageStatus.SENT, userMessage.status)
        assertTrue(repository.messages.containsKey("reply1"))
    }

    @Test
    fun `given the send fails when invoked then the user message is marked FAILED, not dropped`() = runTest {
        val repository = FakeChatRepository().apply {
            sendResult = AppResult.Error(Failure.Network("offline"))
        }

        val result = SendChatMessage(repository)("Bisa kasih alternatif Push Up?")

        assertIs<AppResult.Error>(result)
        val userMessage = repository.messages.values.single { it.role == ChatMessageRole.USER }
        assertEquals(ChatMessageStatus.FAILED, userMessage.status)
    }
}
