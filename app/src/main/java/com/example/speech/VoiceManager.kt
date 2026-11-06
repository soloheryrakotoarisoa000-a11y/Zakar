package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.localization.AppLanguage
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit = {}
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceManager)
                }
            }
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing VoiceManager", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        }
    }

    fun startListening(language: AppLanguage) {
        val localeTag = when (language) {
            AppLanguage.MG -> "mg-MG"
            AppLanguage.FR -> "fr-FR"
            AppLanguage.EN -> "en-US"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "MPAMPAHATSIARO - Parlez...")
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onError(e.message ?: "Speech recognition error")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isTtsReady) return
        val locale = when (language) {
            AppLanguage.MG -> Locale("mg")
            AppLanguage.FR -> Locale.FRENCH
            AppLanguage.EN -> Locale.ENGLISH
        }
        textToSpeech?.language = locale
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ZakarAssistantTTS")
    }

    fun release() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        Log.w("VoiceManager", "Speech recognition error: $error")
        onError("Code d'erreur vocale: $error")
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResult(matches[0])
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
