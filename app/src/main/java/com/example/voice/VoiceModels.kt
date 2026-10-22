package com.example.voice

import com.example.data.local.ProductEntity

enum class VoiceActionType {
    ADD_ITEM,
    QUERY_STOCK,
    SHOW_DAILY_SALES,
    COMPLETE_BILL,
    CLEAR_BILL,
    SEARCH_PRODUCT,
    UNKNOWN
}

data class ParsedVoiceAction(
    val rawTranscript: String,
    val actionType: VoiceActionType,
    val targetProduct: ProductEntity? = null,
    val candidateProducts: List<ProductEntity> = emptyList(),
    val extractedQuantity: Double = 1.0,
    val extractedUnit: String? = null,
    val confidence: Float = 0.0f,
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String = "",
    val feedbackMessage: String = ""
)

sealed class SpeechRecognitionState {
    object Idle : SpeechRecognitionState()
    object Listening : SpeechRecognitionState()
    data class Processing(val partialText: String = "") : SpeechRecognitionState()
    data class Success(val recognizedText: String) : SpeechRecognitionState()
    data class Error(val errorMessage: String) : SpeechRecognitionState()
}
