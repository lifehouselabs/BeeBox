package com.example

import android.content.Context
import android.media.audiofx.PresetReverb
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class AudioEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val random = Random(System.currentTimeMillis())
    
    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                tts = TextToSpeech(context, this@AudioEngine)
            } catch (e: Exception) {
                // TTS might not be available on this emulator
                isReady = false
            }
        }
    }
    
    private val _currentlyPlayingWord = MutableStateFlow<String?>(null)
    val currentlyPlayingWord: StateFlow<String?> = _currentlyPlayingWord.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    enum class PlaybackState {
        IDLE, SEARCHING, PLAYING
    }

    private var onCompleteCallback: (() -> Unit)? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId != "___DONE___") {
                            _currentlyPlayingWord.value = utteranceId
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "___DONE___") {
                            _currentlyPlayingWord.value = null
                            _playbackState.value = PlaybackState.IDLE
                            onCompleteCallback?.invoke()
                            onCompleteCallback = null
                        } else {
                            _currentlyPlayingWord.value = null
                        }
                    }

                    @Deprecated("Deprecated in Java", ReplaceWith("onError(utteranceId, -1)"))
                    override fun onError(utteranceId: String?) {
                        if (utteranceId == "___DONE___") {
                            _currentlyPlayingWord.value = null
                            _playbackState.value = PlaybackState.IDLE
                            onCompleteCallback?.invoke()
                            onCompleteCallback = null
                        } else {
                            _currentlyPlayingWord.value = null
                        }
                    }
                })
            }
        }
    }

    fun speak(text: String, onComplete: () -> Unit) {
        if (!isReady || text.isBlank()) {
            onComplete()
            return
        }
        
        _playbackState.value = PlaybackState.SEARCHING
        onCompleteCallback = onComplete
        
        // Split text to words to mimic stitched clips
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        if (words.isEmpty()) {
            _playbackState.value = PlaybackState.IDLE
            onCompleteCallback?.invoke()
            onCompleteCallback = null
            return
        }

        // We'll queue the words with slightly different pitches to simulate stitching
        _playbackState.value = PlaybackState.PLAYING
        
        words.forEachIndexed { index, word ->
            // Random pitch between 0.7 and 1.3
            val pitch = 0.7f + random.nextFloat() * 0.6f
            val speed = 0.8f + random.nextFloat() * 0.4f
            
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)
            
            // Queue mode ADD means they play sequentially
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = word
            tts?.speak(word, queueMode, null, utteranceId)
        }
        
        // Add a silent utterance at the end to know when it's done
        val finalId = "___DONE___"
        tts?.playSilentUtterance(100, TextToSpeech.QUEUE_ADD, finalId)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
