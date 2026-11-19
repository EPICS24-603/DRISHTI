package com.example.assistantapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

private val Ink = Color(0xFF102A2A)
private val Teal = Color(0xFF087E78)
private val Mint = Color(0xFFE3F5EF)
private val Paper = Color(0xFFF5F7F4)

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(AppPreferences.getCustomApiKey(context)) }
    var showPassword by remember { mutableStateOf(false) }
    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var selectedInterval by remember { mutableStateOf(AppPreferences.getNavIntervalMs(context)) }
    var motionFilterEnabled by remember { mutableStateOf(AppPreferences.isMotionFilterEnabled(context)) }
    var showEpicsDialog by remember { mutableStateOf(false) }

    if (showEpicsDialog) {
        AboutEpicsDialog(onDismiss = { showEpicsDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // Top App Bar
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Settings", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Configuration & Preferences", color = Color(0xFF6E8E8B), fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // API Key Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = Teal, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Gemini API Key", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            val hasKey = AppPreferences.getEffectiveApiKey(context).isNotBlank()
                            Text(
                                if (hasKey) "Status: Active & Configured" else "Status: Not configured",
                                color = if (hasKey) Color(0xFF1E7E45) else Color(0xFFC2410C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        "Paste your personal Gemini API Key from Google AI Studio. You never need to hardcode it in source code.",
                        color = Color(0xFF557774),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            saveMessage = null
                            testStatus = null
                        },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("Paste AIzaSy... or AQ... key here") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            TextButton(onClick = { showPassword = !showPassword }) {
                                Text(if (showPassword) "Hide" else "Show", fontSize = 11.sp, color = Teal)
                            }
                        }
                    )

                    if (saveMessage != null) {
                        Text(saveMessage!!, color = Color(0xFF1E7E45), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (testStatus != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (isSuccess) Color(0xFF1E7E45) else Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                testStatus!!,
                                color = if (isSuccess) Color(0xFF1E7E45) else Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                AppPreferences.setCustomApiKey(context, apiKeyInput)
                                saveMessage = "API Key saved successfully!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Key", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val testKey = apiKeyInput.trim().ifBlank { AppPreferences.getEffectiveApiKey(context) }
                                isTesting = true
                                testStatus = null
                                scope.launch {
                                    val result = GeminiClient.testConnection(testKey)
                                    isTesting = false
                                    result.fold(
                                        onSuccess = {
                                            isSuccess = true
                                            testStatus = "Key is working properly!"
                                        },
                                        onFailure = {
                                            isSuccess = false
                                            testStatus = it.message ?: "Connection test failed."
                                        }
                                    )
                                }
                            },
                            enabled = !isTesting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(color = Teal, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test Key", color = Teal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (AppPreferences.getCustomApiKey(context).isNotBlank()) {
                        TextButton(
                            onClick = {
                                AppPreferences.clearCustomApiKey(context)
                                apiKeyInput = ""
                                saveMessage = "Custom key cleared. Reset to default."
                                testStatus = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Clear Custom Key", color = Color(0xFFB3261E), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Speed & Token Optimization Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Speed, contentDescription = null, tint = Teal, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Navigation Cadence & Speed", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Adjust update rate and token conservation", color = Color(0xFF6E8E8B), fontSize = 12.sp)
                        }
                    }

                    Text("Frequency of live environment descriptions:", color = Color(0xFF557774), fontSize = 13.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("Fast", 2500L, "2.5s"),
                            Triple("Standard", 3500L, "3.5s"),
                            Triple("Saver", 5000L, "5.0s")
                        ).forEach { (label, ms, subtitle) ->
                            val isSelected = selectedInterval == ms
                            Button(
                                onClick = {
                                    selectedInterval = ms
                                    AppPreferences.setNavIntervalMs(context, ms)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Teal else Color(0xFFE8F2EF),
                                    contentColor = if (isSelected) Color.White else Ink
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(subtitle, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5EDE9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Smart Frame Motion Filter", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Skips API calls when stationary to save battery & up to 70% tokens.",
                                color = Color(0xFF557774),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = motionFilterEnabled,
                            onCheckedChange = {
                                motionFilterEnabled = it
                                AppPreferences.setMotionFilterEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Teal, checkedTrackColor = Mint)
                        )
                    }
                }
            }

            // EPICS Project Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5F2)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EPICS Team & Project Details", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "View project description, engineering goals, and the full team roster.",
                        color = Color(0xFF4C6A67),
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = { showEpicsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View EPICS Team Roster", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
