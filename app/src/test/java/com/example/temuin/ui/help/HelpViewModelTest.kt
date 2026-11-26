package com.example.temuin.ui.help

import com.example.temuin.data.help.HelpReply
import com.example.temuin.data.help.HelpRepository
import com.example.temuin.data.help.HelpTopic
import com.example.temuin.data.help.HelpTurn
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HelpViewModelTest {

    @Test
    fun retryReusesFailedMessageWithoutAddingAnotherUserMessage() {
        var attempts = 0
        val repository = object : HelpRepository {
            override suspend fun topics() = Result.success(emptyList<HelpTopic>())

            override suspend fun chat(message: String, history: List<HelpTurn>): Result<HelpReply> {
                attempts++
                return if (attempts == 1) {
                    Result.failure(IllegalStateException("offline"))
                } else {
                    Result.success(HelpReply("Buka detail temuan.", "CLAIM_INBOX", "FAQ"))
                }
            }
        }
        val viewModel = HelpViewModel(repository, Dispatchers.Unconfined)

        viewModel.send("Bagaimana cara mengajukan klaim?")
        assertNotNull(viewModel.state.value.error)
        viewModel.send("Pertanyaan baru")
        viewModel.retry()

        val messages = viewModel.state.value.messages
        assertEquals(2, attempts)
        assertEquals(1, messages.count { it.role == "user" })
        assertEquals(1, messages.count { it.role == "assistant" })
        assertEquals(null, viewModel.state.value.error)
    }
}
