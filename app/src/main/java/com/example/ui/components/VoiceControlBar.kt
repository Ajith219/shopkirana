package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voice.SpeechRecognitionState

@Composable
fun VoiceControlBar(
    speechState: SpeechRecognitionState,
    rmsLevel: Float,
    onStartListening: (preferTelugu: Boolean) -> Unit,
    onStopListening: () -> Unit,
    onSimulateCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var preferTelugu by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val isListening = speechState is SpeechRecognitionState.Listening || speechState is SpeechRecognitionState.Processing

    Card(
        modifier = modifier
            .testTag("voice_control_bar")
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Row: Mic Button + Status Info + Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Tactile Mic Action Button
                Box(contentAlignment = Alignment.Center) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(pulseScale)
                                .background(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                                    CircleShape
                                )
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isListening) {
                                onStopListening()
                            } else {
                                onStartListening(preferTelugu)
                            }
                        },
                        modifier = Modifier
                            .testTag("voice_mic_button")
                            .size(52.dp)
                            .background(
                                if (isListening) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Voice status transcript / prompt
                Column(modifier = Modifier.weight(1f)) {
                    val statusText = when (speechState) {
                        is SpeechRecognitionState.Listening -> "Listening… మాట్లాడండి (Speak now)"
                        is SpeechRecognitionState.Processing -> speechState.partialText.ifBlank { "Processing voice…" }
                        is SpeechRecognitionState.Success -> "Heard: \"${speechState.recognizedText}\""
                        is SpeechRecognitionState.Error -> speechState.errorMessage
                        is SpeechRecognitionState.Idle -> "Tap mic or select quick command"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isListening) FontWeight.Bold else FontWeight.Medium,
                        color = when (speechState) {
                            is SpeechRecognitionState.Listening -> MaterialTheme.colorScheme.secondary
                            is SpeechRecognitionState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "English & తెలుగు Voice Support",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Language Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = preferTelugu,
                        onClick = { preferTelugu = true },
                        label = { Text("తెలుగు", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = !preferTelugu,
                        onClick = { preferTelugu = false },
                        label = { Text("Eng", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Voice Command Simulator Chips
            Text(
                text = "Quick Command Testing / వేగవంతమైన కమాండ్లు:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCommandChip(
                    text = "Add 2 kilos rice",
                    onClick = { onSimulateCommand("Add two kilos of rice") }
                )
                QuickCommandChip(
                    text = "రెండు కిలోల బియ్యం add cheyyi",
                    onClick = { onSimulateCommand("రెండు కిలోల బియ్యం add cheyyi") }
                )
                QuickCommandChip(
                    text = "Oil stock entha undi?",
                    onClick = { onSimulateCommand("Oil stock entha undi?") }
                )
                QuickCommandChip(
                    text = "Show today's sales",
                    onClick = { onSimulateCommand("Show today's sales") }
                )
                QuickCommandChip(
                    text = "1 liter milk add",
                    onClick = { onSimulateCommand("Add 1 liter milk") }
                )
                QuickCommandChip(
                    text = "పంచదార ఎంత ఉంది",
                    onClick = { onSimulateCommand("పంచదార స్టాక్ ఎంత ఉంది") }
                )
                QuickCommandChip(
                    text = "బిల్లు చెయ్యి (Complete)",
                    onClick = { onSimulateCommand("bill cheyyi") }
                )
            }
        }
    }
}

@Composable
private fun QuickCommandChip(
    text: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp, maxLines = 1) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
