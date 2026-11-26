package com.example.temuin.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.help.HelpRepository
import com.example.temuin.data.help.HelpReply
import com.example.temuin.data.help.HelpReportReference
import com.example.temuin.data.help.HelpTopic
import com.example.temuin.data.help.HelpTurn
import com.example.temuin.data.help.RemoteHelpRepository
import com.example.temuin.data.help.localHelpTopics
import com.example.temuin.data.remote.ApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import retrofit2.HttpException

internal const val MAX_HELP_MESSAGE_LENGTH = 500

data class HelpMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val topic: String? = null,
    val source: String? = null,
    val reports: List<HelpReportReference> = emptyList(),
)

data class HelpUiState(
    val topics: List<HelpTopic> = localHelpTopics,
    val messages: List<HelpMessage> = emptyList(),
    val firstName: String? = null,
    val sending: Boolean = false,
    val error: String? = null,
)

class HelpViewModel(
    private val repository: HelpRepository = RemoteHelpRepository(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val profileNameProvider: suspend () -> String? = { null },
) : ViewModel() {
    private val _state = MutableStateFlow(HelpUiState())
    val state: StateFlow<HelpUiState> = _state.asStateFlow()

    private data class Pending(val message: String, val history: List<HelpTurn>)
    private var pending: Pending? = null
    private val completedHistory = ArrayDeque<HelpTurn>(4)

    init {
        viewModelScope.launch(dispatcher) {
            repository.topics().onSuccess { topics ->
                if (topics.isNotEmpty()) _state.update { it.copy(topics = topics) }
            }
        }
        viewModelScope.launch(dispatcher) {
            val name = try {
                profileNameProvider()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            val firstName = name
                .orEmpty()
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.takeIf { it.isNotBlank() }
            if (firstName != null) {
                _state.update { it.copy(firstName = firstName) }
            }
        }
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isBlank() || message.codePointCount(0, message.length) > MAX_HELP_MESSAGE_LENGTH || _state.value.sending || pending != null) return
        val history = completedHistory.toList()
        val userMessage = HelpMessage(role = "user", text = message)
        pending = Pending(message, history)
        _state.update { it.copy(messages = it.messages + userMessage, error = null) }
        deliver()
    }

    fun retry() {
        if (_state.value.sending || pending == null) return
        _state.update { it.copy(error = null) }
        deliver()
    }

    fun askTopic(topic: HelpTopic) {
        if (_state.value.sending) return
        val message = HelpMessage(role = "assistant", text = topic.answer, topic = topic.id, source = "FAQ")
        _state.update { it.copy(messages = it.messages + message) }
    }

    private fun deliver() {
        val request = pending ?: return
        _state.update { it.copy(sending = true) }
        viewModelScope.launch(dispatcher) {
            repository.chat(request.message, request.history)
                .onSuccess { reply ->
                    if (completedHistory.size == 4) {
                        completedHistory.removeFirst()
                        completedHistory.removeFirst()
                    }
                    completedHistory.addLast(HelpTurn("user", request.message))
                    val end = reply.reply.offsetByCodePoints(
                        0, minOf(reply.reply.codePointCount(0, reply.reply.length), MAX_HELP_MESSAGE_LENGTH),
                    )
                    completedHistory.addLast(HelpTurn("assistant", reply.reply.substring(0, end)))
                    pending = null
                    _state.update { it.copy(messages = it.messages + reply.toMessage(), sending = false) }
                }
                .onFailure { error ->
                    val message = if (error is HttpException) ApiClient.errorMessage(error)
                    else "Belum bisa mengirim pertanyaan. Coba lagi sebentar."
                    _state.update { it.copy(sending = false, error = message) }
                }
        }
    }

    private fun HelpReply.toMessage() = HelpMessage(
        role = "assistant", text = reply, topic = topic, source = source, reports = reports,
    )
}
