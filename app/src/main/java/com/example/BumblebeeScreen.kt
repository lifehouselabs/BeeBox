package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.DarkSurface



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BumblebeeScreen(
    viewModel: BumblebeeViewModel = viewModel()
) {
    
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val clips by viewModel.clips.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentlyPlayingWord by viewModel.currentlyPlayingWord.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bumblebee Assistant", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            InputBar(
                inputText = inputText,
                onInputChanged = viewModel::onInputTextChanged,
                onSpeak = viewModel::onSpeak,
                isPlaying = playbackState != AudioEngine.PlaybackState.IDLE
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (playbackState == AudioEngine.PlaybackState.SEARCHING && clips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (clips.isNotEmpty()) {
                Text(
                    text = "Assembling audio fragments...",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clips) { clip ->
                        val isPlaying = currentlyPlayingWord == clip.word && playbackState == AudioEngine.PlaybackState.PLAYING
                        ClipItem(clip = clip, isPlaying = isPlaying)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Type something to piece together audio fragments",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ClipItem(clip: BumblebeeViewModel.Clip, isPlaying: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else DarkSurface,
        label = "backgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isPlaying) Color.Black else MaterialTheme.colorScheme.onSurface,
        label = "contentColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = clip.word,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = "Source: ${clip.source}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
        
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Playing",
                tint = contentColor
            )
        }
    }
}

@Composable
fun InputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSpeak: () -> Unit,
    isPlaying: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter text to synthesize...") },
                shape = RoundedCornerShape(24.dp),
                enabled = !isPlaying,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            
            FloatingActionButton(
                onClick = onSpeak,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isPlaying) {
                     CircularProgressIndicator(
                         color = Color.Black,
                         modifier = Modifier.size(24.dp),
                         strokeWidth = 2.dp
                     )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Assemble and Play"
                    )
                }
            }
        }
    }
}
