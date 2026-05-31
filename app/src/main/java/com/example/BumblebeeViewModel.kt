package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BumblebeeViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = AudioEngine(application)

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _clips = MutableStateFlow<List<Clip>>(emptyList())
    val clips: StateFlow<List<Clip>> = _clips.asStateFlow()

    val playbackState = audioEngine.playbackState
    val currentlyPlayingWord = audioEngine.currentlyPlayingWord

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onSpeak() {
        if (_inputText.value.isBlank()) return
        
        viewModelScope.launch {
            // Simulate searching for clips
            _clips.value = emptyList()
            val words = _inputText.value.split(Regex("\\s+")).filter { it.isNotBlank() }
            
            // Show fake search progress
            val foundClips = mutableListOf<Clip>()
            words.forEach { word ->
                delay(100) // fake search time
                foundClips.add(Clip(word, generateRandomSource()))
                _clips.value = foundClips.toList()
            }
            
            // Speak
            audioEngine.speak(_inputText.value) {
                // Done
            }
        }
    }

    private fun generateRandomSource(): String {
        val sources = listOf("Movie 1984", "Podcast Snippet", "News Broadcast 1999", "Radio KXYZ", "Interstellar DVD", "Meme Compilation", "Vintage Cartoon")
        return sources.random()
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.shutdown()
    }
    
    data class Clip(val word: String, val source: String)
}
