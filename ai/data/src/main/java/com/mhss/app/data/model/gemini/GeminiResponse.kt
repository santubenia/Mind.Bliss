package com.mhss.app.data.model.gemini

import com.mhss.app.data.NetworkConstants.GEMINI_MESSAGE_MODEL_TYPE
import com.mhss.app.data.NetworkConstants.GEMINI_MESSAGE_USER_TYPE
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiMessage
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String
)

fun GeminiResponse.toAiMessage() = AiMessage(
    content = candidates!!.first().content.parts.first().text,
    type = if (candidates.first().content.role == GEMINI_MESSAGE_USER_TYPE) AiMessageType.USER else AiMessageType.MODEL,
    time = Clock.System.now().toEpochMilliseconds()
)

val GeminiResponse.text
    get() = candidates!!.first().content.parts.first().text

val AiMessageType.geminiRole
    get() = if (this == AiMessageType.USER) GEMINI_MESSAGE_USER_TYPE else GEMINI_MESSAGE_MODEL_TYPE