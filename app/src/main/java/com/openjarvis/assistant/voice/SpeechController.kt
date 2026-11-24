package com.openjarvis.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechController(context: Context) : RecognitionListener, TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = TextToSpeech(appContext, this)
    private var ttsReady = false
    private var onText: ((String) -> Unit)? = null
    private var onStatus: ((String) -> Unit)? = null

    fun startListening(
        locale: Locale = Locale("ur", "PK"),
        onText: (String) -> Unit,
        onStatus: (String) -> Unit
    ) {
        this.onText = onText
        this.onStatus = onStatus

        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onStatus("Speech recognition is not available on this device.")
            return
        }

        try {
            recognizer?.destroy()
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).also {
                it.setRecognitionListener(this)
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            onStatus("Listening…")
            recognizer?.startListening(intent)
        } catch (error: Exception) {
            onStatus("Could not start speech recognition: ${error.localizedMessage ?: "unknown error"}")
        }
    }

    fun speak(text: String, locale: Locale = Locale("ur", "PK")) {
        val engine = textToSpeech
        if (engine == null || !ttsReady) {
            onStatus?.invoke("Voice output is still initializing.")
            return
        }
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.US)
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "open-jarvis-utterance")
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (!ttsReady) onStatus?.invoke("Text-to-speech initialization failed.")
    }

    override fun onReadyForSpeech(params: Bundle?) {
        onStatus?.invoke("I'm listening…")
    }

    override fun onBeginningOfSpeech() {
        onStatus?.invoke("Hearing you…")
    }

    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() {
        onStatus?.invoke("Processing speech…")
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            SpeechRecognizer.ERROR_CLIENT -> "Speech service was interrupted."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
            SpeechRecognizer.ERROR_NO_MATCH -> "I couldn't understand that. Please try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything."
            else -> "Speech recognition failed (code $error)."
        }
        onStatus?.invoke(message)
    }

    override fun onResults(results: Bundle?) {
        val phrase = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()
        if (phrase.isNotBlank()) {
            onText?.invoke(phrase)
            onStatus?.invoke("Speech captured. AI conversation engine is the next module.")
        } else {
            onStatus?.invoke("No speech text was returned.")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    fun release() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        ttsReady = false
    }
}
