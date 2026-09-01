package com.example.ui.screens.aicoach

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ImportResult
import com.example.ui.FitViewModel
import com.example.ui.theme.*

@Composable
fun AiCoachScreen(
    viewModel: FitViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiPrompt by viewModel.aiPrompt.collectAsState()
    var pastedJson by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.generateAiPrompt()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FitDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // 1. Title Header
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "AI Workout Coach",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitTextPrimary
                )
                Text(
                    text = "Offline External AI Workflow — Use your favorite LLM",
                    fontSize = 13.sp,
                    color = FitTextSecondary
                )
            }
        }

        // 2. Info Banner explaining offline external AI workflow
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F263E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCyan)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = FitCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("How this works:", color = FitCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "1. Copy your personalized Fitness Profile Prompt below.\n" +
                            "2. Paste it into ChatGPT, Gemini, or Claude.\n" +
                            "3. Discuss your routine with the AI.\n" +
                            "4. Paste the AI's final JSON response back into this app to instantly generate your program!",
                            color = FitTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 3. Step 1: Copy Prompt Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STEP 1: YOUR AI PROMPT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FitCyan
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Fit Tracker AI Prompt", aiPrompt)
                                clipboard.setPrimaryClip(clip)
                                importStatusMessage = "Prompt copied to clipboard! Paste it in ChatGPT or Gemini."
                                isError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FitCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("copy_prompt_button")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Prompt", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, FitCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = aiPrompt,
                            color = FitTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4. Step 2: Paste and Import AI JSON Plan
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitCardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, FitCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STEP 2: IMPORT AI-GENERATED JSON",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste the JSON response received from the AI below:",
                        fontSize = 12.sp,
                        color = FitTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pastedJson,
                        onValueChange = { pastedJson = it },
                        placeholder = { Text("{\n  \"program\": \"Hypertrophy 12-Week\",\n  \"durationWeeks\": 12,\n  ...\n}", color = FitGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitGreen,
                            unfocusedBorderColor = FitCardBorder,
                            focusedTextColor = FitTextPrimary,
                            unfocusedTextColor = FitTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("ai_json_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (pastedJson.isBlank()) {
                                importStatusMessage = "Please paste the JSON code block first."
                                isError = true
                                return@Button
                            }
                            viewModel.importAiJsonPlan(pastedJson) { result ->
                                when (result) {
                                    is ImportResult.Success -> {
                                        importStatusMessage = "Successfully imported ${result.name} (${result.weeks} weeks)! Workout schedule is now active."
                                        isError = false
                                        onNavigateHome()
                                    }
                                    is ImportResult.Error -> {
                                        importStatusMessage = "Import Failed: ${result.message}"
                                        isError = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FitGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_ai_plan_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Import", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validate & Activate Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    if (importStatusMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = importStatusMessage ?: "",
                            color = if (isError) FitRed else FitGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
