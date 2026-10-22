package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val recognitionState: StateFlow<SpeechRecognitionState> = _recognitionState.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    private var onSpeechResultListener: ((String) -> Unit)? = null

    fun setOnSpeechResultListener(listener: (String) -> Unit) {
        this.onSpeechResultListener = listener
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(preferTelugu: Boolean = true) {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recognitionState.value = SpeechRecognitionState.Error(
                "Speech recognition service unavailable on this device. You can use Quick Voice chips or type commands."
            )
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                
                // Set language to Telugu (India) or English (India)
                if (preferTelugu) {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "te-IN")
                } else {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                }
            }

            _recognitionState.value = SpeechRecognitionState.Listening
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _recognitionState.value = SpeechRecognitionState.Error("Failed to start voice listener: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        if (_recognitionState.value is SpeechRecognitionState.Listening) {
            _recognitionState.value = SpeechRecognitionState.Idle
        }
    }

    /**
     * Allows one-click testing of English, Telugu, and Tanglish sample commands
     * directly in the emulator without requiring physical microphone access.
     */
    fun simulateVoiceInput(transcript: String) {
        _recognitionState.value = SpeechRecognitionState.Success(transcript)
        onSpeechResultListener?.invoke(transcript)
    }

    fun resetState() {
        _recognitionState.value = SpeechRecognitionState.Idle
        _rmsAudioLevel.value = 0f
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _recognitionState.value = SpeechRecognitionState.Listening
        }

        override fun onBeginningOfSpeech() {
            _recognitionState.value = SpeechRecognitionState.Processing("Listening…")
        }

        override fun onRmsChanged(rmsdB: Float) {
            _rmsAudioLevel.value = (rmsdB / 10f).coerceIn(0f, 1f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _recognitionState.value = SpeechRecognitionState.Processing("Processing speech…")
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record Audio permission missing"
                SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy. Retrying…"
                SpeechRecognizer.ERROR_SERVER -> "Server recognition error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap mic to retry."
                else -> "Speech recognition error ($error)"
            }
            _recognitionState.value = SpeechRecognitionState.Error(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val topMatch = matches?.firstOrNull() ?: ""
            if (topMatch.isNotBlank()) {
                _recognitionState.value = SpeechRecognitionState.Success(topMatch)
                onSpeechResultListener?.invoke(topMatch)
            } else {
                _recognitionState.value = SpeechRecognitionState.Error("No match found")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _recognitionState.value = SpeechRecognitionState.Processing(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
