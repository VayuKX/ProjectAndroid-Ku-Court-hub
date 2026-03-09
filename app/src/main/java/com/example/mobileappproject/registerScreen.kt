package com.example.mobileappproject

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val bgAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgAnim"
    )

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            authViewModel.resetState()
            onRegisterSuccess()
        }
    }

    val allFilled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
            studentId.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
    val passwordMatch = password == confirmPassword || confirmPassword.isEmpty()

    Box(Modifier.fillMaxSize()) {
        // Dark gradient background
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF0A0A1A), Color(0xFF1A0A05), Color(0xFF0A0A1A)))
            )
        )

        // Decorative orbs
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x44FF6B35), Color.Transparent),
                    center = Offset(size.width * (0.9f - bgAnim * 0.05f), size.height * 0.08f),
                    radius = 350f
                ),
                radius = 350f,
                center = Offset(size.width * (0.9f - bgAnim * 0.05f), size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x33E04E1A), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * (0.9f + bgAnim * 0.03f)),
                    radius = 250f
                ),
                radius = 250f,
                center = Offset(size.width * 0.1f, size.height * (0.9f + bgAnim * 0.03f))
            )
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Back button
            Row(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape)
                        .background(Color.White.copy(0.1f))
                        .clickable { onNavigateToLogin() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Logo + Header
            Box(
                Modifier.size(76.dp)
                    .shadow(20.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
                    .clip(CircleShape).background(GradientOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Create Account", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp)
            Text("Join SportsHub and start playing", color = Color(0xFF94A3B8), fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(28.dp))

            // ── Personal Info Card ──
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(20.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    SectionLabel(icon = Icons.Default.Person, title = "Personal Info")
                    Spacer(Modifier.height(16.dp))

                    RegisterField(
                        value = name, onValueChange = { name = it },
                        label = "Full Name", icon = Icons.Default.Badge
                    )
                    Spacer(Modifier.height(12.dp))

                    RegisterField(
                        value = studentId, onValueChange = { studentId = it },
                        label = "Student ID", icon = Icons.Default.School,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(12.dp))

                    RegisterField(
                        value = phone, onValueChange = { phone = it },
                        label = "Phone Number", icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Account Info Card ──
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(20.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    SectionLabel(icon = Icons.Default.Lock, title = "Account Info")
                    Spacer(Modifier.height(16.dp))

                    RegisterField(
                        value = email, onValueChange = { email = it },
                        label = "Email Address", icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(12.dp))

                    RegisterField(
                        value = password, onValueChange = { password = it },
                        label = "Password", icon = Icons.Default.Lock,
                        isPassword = true, passVisible = passVisible,
                        onTogglePass = { passVisible = !passVisible }
                    )

                    if (password.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        PasswordStrengthBar(password)
                    }

                    Spacer(Modifier.height(12.dp))

                    RegisterField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = "Confirm Password", icon = Icons.Default.LockPerson,
                        isPassword = true, passVisible = confirmPassVisible,
                        onTogglePass = { confirmPassVisible = !confirmPassVisible },
                        isError = !passwordMatch && confirmPassword.isNotEmpty(),
                        trailingCheck = confirmPassword.isNotEmpty() && password == confirmPassword
                    )

                    AnimatedVisibility(visible = !passwordMatch && confirmPassword.isNotEmpty()) {
                        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Passwords do not match", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }

                    // Error
                    AnimatedVisibility(visible = localError != null || authState is AuthViewModel.AuthState.Error) {
                        Row(
                            Modifier.padding(top = 10.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEE2E2))
                                .padding(12.dp, 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                localError ?: (authState as? AuthViewModel.AuthState.Error)?.message ?: "",
                                color = Color(0xFFEF4444), fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Register button
                    Box(
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                            .shadow(
                                if (allFilled && passwordMatch) 12.dp else 0.dp,
                                RoundedCornerShape(16.dp), ambientColor = OrangeMain, spotColor = OrangeMain
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (allFilled && passwordMatch) GradientOrange
                                else Brush.horizontalGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB)))
                            )
                            .clickable(
                                enabled = allFilled && passwordMatch && authState !is AuthViewModel.AuthState.Loading
                            ) {
                                if (password != confirmPassword) {
                                    localError = "Passwords do not match"
                                    return@clickable
                                }
                                localError = null
                                authViewModel.register(name, email, password, phone, studentId)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState is AuthViewModel.AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HowToReg, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Create Account", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Color(0xFF94A3B8), fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text("Sign In", color = OrangeLight, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(OrangeMain.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = OrangeMain, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
    }
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passVisible: Boolean = false,
    onTogglePass: (() -> Unit)? = null,
    isError: Boolean = false,
    trailingCheck: Boolean = false
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = OrangeMain) },
        trailingIcon = {
            if (isPassword && onTogglePass != null) {
                IconButton(onClick = onTogglePass) {
                    Icon(
                        if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null, tint = if (trailingCheck) SuccessGreen else TextSecondary
                    )
                }
            } else if (trailingCheck) {
                Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
            }
        },
        visualTransformation = if (isPassword && !passVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp), singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color(0xFFEF4444) else OrangeMain,
            unfocusedBorderColor = if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
            focusedContainerColor = Color(0xFFFFF7F4), unfocusedContainerColor = Color.White,
            focusedLabelColor = OrangeMain
        )
    )
}

@Composable
private fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 6 -> 2
        else -> 1
    }
    val (color, label) = when (strength) {
        3 -> SuccessGreen to "Strong"
        2 -> Color(0xFFF59E0B) to "Medium"
        else -> Color(0xFFEF4444) to "Weak"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { idx ->
            Box(
                Modifier.weight(1f).height(4.dp)
                    .padding(end = if (idx < 2) 4.dp else 0.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (idx < strength) color else Color(0xFFE5E7EB))
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}