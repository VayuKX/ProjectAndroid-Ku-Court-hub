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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    // Google new user fields
    var showProfileDialog by remember { mutableStateOf(false) }
    var googleName by remember { mutableStateOf("") }
    var googlePhone by remember { mutableStateOf("") }
    var googleStudentId by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgOffset"
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                authViewModel.resetState()
                onLoginSuccess()
            }
            is AuthViewModel.AuthState.NewGoogleUser -> {
                showProfileDialog = true
            }
            else -> {}
        }
    }

    // ── Dialog: Google User ครั้งแรก ──
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.size(56.dp).clip(CircleShape).background(GradientOrange),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text("Complete Your Profile", fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
                    Text("Please fill in your info to continue",
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileDialogField(googleName, { googleName = it }, "Full Name", Icons.Default.Badge)
                    ProfileDialogField(googleStudentId, { googleStudentId = it }, "Student ID",
                        Icons.Default.School, KeyboardType.Number)
                    ProfileDialogField(googlePhone, { googlePhone = it }, "Phone Number",
                        Icons.Default.Phone, KeyboardType.Phone)
                    if (authState is AuthViewModel.AuthState.Error) {
                        Text((authState as AuthViewModel.AuthState.Error).message,
                            color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                val allFilled = googleName.isNotBlank() && googlePhone.isNotBlank() && googleStudentId.isNotBlank()
                Box(
                    Modifier.fillMaxWidth().height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (allFilled) GradientOrange
                        else Brush.horizontalGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))))
                        .clickable(enabled = allFilled) {
                            authViewModel.saveGoogleUserProfile(googleName, googlePhone, googleStudentId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (authState is AuthViewModel.AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save & Continue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // ── Dialog: Forgot Password ──
    if (showForgotDialog) {
        val resetState by authViewModel.authState.collectAsState()
        AlertDialog(
            onDismissRequest = { showForgotDialog = false; resetEmail = ""; authViewModel.resetState() },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.size(52.dp).clip(CircleShape).background(GradientOrange),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.LockReset, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text("Reset Password", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("Enter your email and we'll send you a reset link.",
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail, onValueChange = { resetEmail = it },
                        label = { Text("Email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = OrangeMain) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedLabelColor = OrangeMain
                        )
                    )
                    when (resetState) {
                        is AuthViewModel.AuthState.ResetPasswordSent -> {
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFECFDF5)).padding(12.dp, 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reset link sent! Check your inbox.", color = SuccessGreen, fontSize = 13.sp)
                            }
                        }
                        is AuthViewModel.AuthState.Error -> {
                            Spacer(Modifier.height(10.dp))
                            Text((resetState as AuthViewModel.AuthState.Error).message,
                                color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                Box(
                    Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (resetEmail.isNotBlank()) GradientOrange
                        else Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB))))
                        .clickable(enabled = resetEmail.isNotBlank()) { authViewModel.resetPassword(resetEmail) },
                    contentAlignment = Alignment.Center
                ) { Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false; resetEmail = ""; authViewModel.resetState() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            R.drawable.chatgpt_image_5____2569_21_52_10,
            null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0x55000000), Color(0xCC0A0A1A), Color(0xF50A0A1A)))
        ))
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x33FF6B35), Color.Transparent),
                    center = Offset(size.width * (0.85f + bgOffset * 0.05f), size.height * 0.15f),
                    radius = 300f
                ),
                radius = 300f,
                center = Offset(size.width * (0.85f + bgOffset * 0.05f), size.height * 0.15f)
            )
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Image(
                painter = painterResource(id = R.drawable.chatgpt_image_5____2569_21_37_51__1_),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
            )
            Spacer(Modifier.height(16.dp))
            Text("Ku Court Hub", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp)
            Text("Book your field · Play your game", color = Color(0xFFCBD5E1), fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(40.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(24.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(28.dp)) {
                    Text("Welcome back 👋", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Sign in to continue", color = TextSecondary, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 24.dp))

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = OrangeMain) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFFFF7F4), unfocusedContainerColor = Color.White,
                            focusedLabelColor = OrangeMain
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = OrangeMain) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null, tint = TextSecondary)
                            }
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFFFF7F4), unfocusedContainerColor = Color.White,
                            focusedLabelColor = OrangeMain
                        )
                    )

                    AnimatedVisibility(visible = authState is AuthViewModel.AuthState.Error) {
                        Row(
                            Modifier.padding(top = 10.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEE2E2))
                                .padding(12.dp, 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text((authState as? AuthViewModel.AuthState.Error)?.message ?: "",
                                color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { resetEmail = email; showForgotDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)) {
                            Text("Forgot password?", color = OrangeMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                            .shadow(if (email.isNotBlank() && password.isNotBlank()) 12.dp else 0.dp,
                                RoundedCornerShape(16.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (email.isNotBlank() && password.isNotBlank()) GradientOrange
                                else Brush.horizontalGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB)))
                            )
                            .clickable(enabled = email.isNotBlank() && password.isNotBlank()
                                    && authState !is AuthViewModel.AuthState.Loading) {
                                authViewModel.loginWithEmail(email, password)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState is AuthViewModel.AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Login, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sign In", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Divider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
                        Text("  or continue with  ", color = TextSecondary, fontSize = 12.sp)
                        Divider(Modifier.weight(1f), color = Color(0xFFE5E7EB))
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { authViewModel.signInWithGoogle(context) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color(0xFFEA4335), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.width(10.dp))
                            Text("Sign in with Google", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", color = Color(0xFFCBD5E1), fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text("Sign Up", color = OrangeLight, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileDialogField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = OrangeMain) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeMain, unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedLabelColor = OrangeMain
        )
    )
}