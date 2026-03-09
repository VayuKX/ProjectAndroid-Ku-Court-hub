package com.example.mobileappproject

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

// ── Brand Colors ──────────────────────────────────────────────────────────────
val OrangeMain    = Color(0xFFFF6B35)
val OrangeLight   = Color(0xFFFF8C5A)
val OrangeDark    = Color(0xFFE04E1A)
val BgLight       = Color(0xFFFAF7F4)
val BgDark        = Color(0xFF0F0F1A)
val SurfaceCard   = Color(0xFFFFFFFF)
val SurfaceCardDark = Color(0xFF1E1E2E)
val CardDark      = Color(0xFF2A2A3E)
val TextPrimary   = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val SuccessGreen  = Color(0xFF22C55E)
val GradientOrange = Brush.horizontalGradient(listOf(OrangeMain, OrangeLight))

// ── CompositionLocal for Dark Mode ────────────────────────────────────────────
val LocalIsDark = compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val vm: BookingViewModel = viewModel()
            val themeVm: ThemeViewModel = viewModel()
            val isDark by themeVm.isDarkMode.collectAsState()

            CompositionLocalProvider(LocalIsDark provides isDark) {
                MaterialTheme(
                    colorScheme = if (isDark) darkColorScheme(
                        primary     = OrangeMain,
                        background  = BgDark,
                        surface     = SurfaceCardDark,
                        onBackground = Color.White,
                        onSurface   = Color.White,
                        onPrimary   = Color.White
                    ) else lightColorScheme(
                        primary    = OrangeMain,
                        background = BgLight,
                        surface    = SurfaceCard
                    )
                ) {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login",
                            enterTransition = { fadeIn(tween(400)) },
                            exitTransition  = { fadeOut(tween(400)) }
                        ) {
                            LoginScreen(
                                onLoginSuccess = {
                                    val email = Firebase.auth.currentUser?.email ?: ""
                                    vm.fetchUserProfile(email)
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }
                        composable("register",
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition  = { slideOutHorizontally { -it } + fadeOut() }
                        ) {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    val email = Firebase.auth.currentUser?.email ?: ""
                                    vm.fetchUserProfile(email)
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }
                        composable("home",
                            enterTransition = { fadeIn(tween(500)) },
                            exitTransition  = { fadeOut(tween(300)) }
                        ) { HomeScreen(navController, vm) }
                        composable("bookings",
                            enterTransition = { fadeIn(tween(500)) },
                            exitTransition  = { fadeOut(tween(300)) }
                        ) { MyBookingsScreen(navController, vm) }
                        composable("profile",
                            enterTransition = { fadeIn(tween(500)) },
                            exitTransition  = { fadeOut(tween(300)) }
                        ) { ProfileScreen(navController, vm, themeVm) }
                        composable("fieldDetail/{fieldId}",
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition  = { slideOutHorizontally { it } + fadeOut() }
                        ) { backStackEntry ->
                            val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                            val fields by vm.fields.collectAsState()
                            val field = fields.find { it.id == fieldId }
                            if (field != null) {
                                FieldDetailScreen(navController = navController, vm = vm, field = field)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Theme helpers ─────────────────────────────────────────────────────────────
@Composable fun appBg()      = if (LocalIsDark.current) BgDark        else BgLight
@Composable fun cardBg()     = if (LocalIsDark.current) SurfaceCardDark else SurfaceCard
@Composable fun cardBg2()    = if (LocalIsDark.current) CardDark       else Color(0xFFF9FAFB)
@Composable fun textPrimary()  = if (LocalIsDark.current) Color.White  else TextPrimary
@Composable fun textSecondary() = if (LocalIsDark.current) Color(0xFF9CA3AF) else TextSecondary
@Composable fun dividerColor() = if (LocalIsDark.current) Color(0xFF2D2D44) else Color(0xFFF3F4F6)
@Composable fun navBg()      = if (LocalIsDark.current) Color(0xFF1A1A2E) else Color.White
@Composable fun inputBg()    = if (LocalIsDark.current) Color(0xFF2A2A3E) else Color.White
@Composable fun inputBgFocus() = if (LocalIsDark.current) Color(0xFF2F2F45) else Color(0xFFFFF7F4)
@Composable fun borderColor() = if (LocalIsDark.current) Color(0xFF3D3D55) else Color(0xFFE5E7EB)
@Composable fun chipBg()     = if (LocalIsDark.current) Color(0xFF2A2A3E) else Color(0xFFF3F4F6)


// ── HomeScreen ────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavHostController, vm: BookingViewModel) {
    val fields by vm.fields.collectAsState()
    val isDark = LocalIsDark.current
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember(fields) {
        fields.map { it.Category }.filter { it.isNotEmpty() }.distinct().sorted()
    }
    val filteredFields = remember(fields, selectedCategory) {
        if (selectedCategory == "All") fields
        else fields.filter { it.Category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        bottomBar = { BottomNav(navController, "home") },
        containerColor = appBg()
    ) { p ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = p.calculateTopPadding() + 24.dp,
                bottom = p.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Sport Type", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary())
                        }
                        AnimatedContent(targetState = filteredFields.size, transitionSpec = { fadeIn() togetherWith fadeOut() }) { count ->
                            Box(
                                Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(OrangeMain.copy(0.1f))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text("$count fields", color = OrangeMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    CategoryFilterBar(selected = selectedCategory, categories = categories, onSelect = { selectedCategory = it })
                }
            }

            if (filteredFields.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(categoryEmoji(selectedCategory), fontSize = 56.sp)
                            Spacer(Modifier.height(14.dp))
                            Text("ไม่พบสนาม $selectedCategory", color = textSecondary(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                items(filteredFields, key = { it.id }) { field ->
                    AnimatedVisibility(visible = true, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }) {
                        FieldCard(
                            field = field,
                            onBookNow = { navController.navigate("fieldDetail/${field.id}") },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── FieldCard ─────────────────────────────────────────────────────────────────
@Composable
fun FieldCard(field: SportsField, onBookNow: () -> Unit, modifier: Modifier = Modifier) {
    val emoji = categoryEmoji(field.Category)
    val placeholderRes = when (field.Category) {
        "Badminton" -> R.drawable.build13
        "Basketball" -> R.drawable._4823460
        "Futsal"-> R.drawable.build21
        "Volleyball" -> R.drawable.screenshot_2026_03_06_201914
        "Petanque" -> R.drawable._11581344_9863040550460563_8116113566137963266_n
        else -> R.drawable.chatgpt_image_5____2569_21_52_10
    }
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg()),
        modifier = modifier.fillMaxWidth().clickable { onBookNow() }
    ) {
        Column {
            Box {
                if (field.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = field.imageUrl, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // แล้วใช้ placeholderRes ตรงนี้
                    Image(
                        painter = painterResource(id = placeholderRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    Modifier.fillMaxWidth().height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x55000000)), startY = 80f))
                )
                Box(
                    Modifier.padding(14.dp).align(Alignment.TopStart)
                        .clip(RoundedCornerShape(10.dp)).background(OrangeMain).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("$emoji ${field.Category}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.padding(18.dp)) {
                Text(field.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textPrimary())
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = OrangeMain, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(field.location, color = textSecondary(), fontSize = 13.sp)
                }
                if (field.description.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(cardBg2()).padding(10.dp, 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, null, tint = textSecondary(), modifier = Modifier.size(13.dp).padding(top = 1.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(field.description, color = textSecondary(), fontSize = 12.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = dividerColor())
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(SuccessGreen.copy(0.1f)).padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                        Spacer(Modifier.width(6.dp))
                        Text("open 18:00 - 22:00", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        Modifier.shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(RoundedCornerShape(14.dp)).background(GradientOrange)
                            .clickable { onBookNow() }.padding(horizontal = 22.dp, vertical = 11.dp)
                    ) {
                        Text("Book Now", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ── MyBookingsScreen ──────────────────────────────────────────────────────────
@Composable
fun MyBookingsScreen(navController: NavHostController, vm: BookingViewModel) {
    val bookings by vm.myBookings.collectAsState()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    val isDark = LocalIsDark.current
    val dialogBg = if (isDark) SurfaceCardDark else Color.White

    selectedBooking?.let { b ->
        AlertDialog(
            onDismissRequest = { selectedBooking = null },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.size(60.dp).shadow(12.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(CircleShape).background(GradientOrange),
                        contentAlignment = Alignment.Center
                    ) { Text(categoryEmoji(b.fieldName), fontSize = 26.sp) }
                    Spacer(Modifier.height(12.dp))
                    Text("รายละเอียดการจอง", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textPrimary())
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(SuccessGreen.copy(0.12f)).padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(Modifier.width(6.dp))
                            Text("Confirmed", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookingDetailRow(Icons.Default.SportsSoccer, "สนาม",     b.fieldName)
                    BookingDetailRow(Icons.Default.CalendarMonth, "วันที่",  b.date)
                    BookingDetailRow(Icons.Default.Schedule, "เวลา",        b.timeSlot)
                    BookingDetailRow(Icons.Default.Person, "ชื่อ",          b.userName)
                    BookingDetailRow(Icons.Default.School, "รหัสนิสิต",    b.studentId)
                    BookingDetailRow(Icons.Default.Phone, "เบอร์โทร",      b.userPhone)
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEE2E2)).border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                            .clickable { showCancelConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cancel, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ยกเลิกการจอง", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    TextButton(onClick = { selectedBooking = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("ปิด", color = textSecondary())
                    }
                }
            }
        )
    }

    if (showCancelConfirm && selectedBooking != null) {
        val b = selectedBooking!!
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFFEE2E2)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("ยืนยันการยกเลิก?", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textPrimary())
                    Text("การยกเลิกไม่สามารถเรียกคืนได้", color = textSecondary(), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF2A1F10) else Color(0xFFFFF7ED))
                        .border(1.dp, OrangeMain.copy(0.2f), RoundedCornerShape(14.dp)).padding(16.dp, 14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(b.fieldName, color = textPrimary(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = textSecondary(), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(b.date, color = textSecondary(), fontSize = 13.sp)
                            Spacer(Modifier.width(12.dp))
                            Icon(Icons.Default.Schedule, null, tint = OrangeMain, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(b.timeSlot, color = OrangeMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFFEF4444), spotColor = Color(0xFFEF4444))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
                            .clickable {
                                vm.cancelBooking(b.id,
                                    onSuccess = { showCancelConfirm = false; selectedBooking = null },
                                    onError   = { showCancelConfirm = false }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ยืนยันยกเลิก", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    TextButton(onClick = { showCancelConfirm = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("ไม่ยกเลิก", color = textSecondary(), textAlign = TextAlign.Center)
                    }
                }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNav(navController, "bookings") },
        containerColor = appBg()
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(24.dp))
            Text("My Bookings", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary())
            Spacer(Modifier.height(2.dp))
            Text("${bookings.size} total reservations", color = textSecondary(), fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            if (bookings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(90.dp).clip(CircleShape).background(cardBg2()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EventBusy, null, tint = textSecondary(), modifier = Modifier.size(44.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("ยังไม่มีการจอง", color = textPrimary(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("เริ่มจองสนามกีฬาที่คุณชอบได้เลย!", color = textSecondary(), fontSize = 13.sp)
                        Spacer(Modifier.height(20.dp))
                        Box(
                            Modifier.shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                                .clip(RoundedCornerShape(14.dp)).background(GradientOrange)
                                .clickable { navController.navigate("home") }.padding(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text("จองเลย", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bookings, key = { it.id }) { b ->
                        BookingCard(booking = b, onClick = { selectedBooking = b })
                    }
                }
            }
        }
    }
}

// ── BookingDetailRow ──────────────────────────────────────────────────────────
@Composable
private fun BookingDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cardBg2()).padding(12.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(OrangeMain.copy(0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = OrangeMain, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("$label: ", color = textSecondary(), fontSize = 13.sp)
        Text(value, color = textPrimary(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// ── BookingCard ───────────────────────────────────────────────────────────────
@Composable
private fun BookingCard(booking: Booking, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg()),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(OrangeMain.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmoji(booking.fieldName), fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.fieldName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = textPrimary(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = textSecondary(), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(booking.date, color = textSecondary(), fontSize = 12.sp)
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = OrangeMain, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(booking.timeSlot, color = OrangeMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(SuccessGreen.copy(0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Confirmed", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ดูรายละเอียด", color = textSecondary(), fontSize = 10.sp)
                    Icon(Icons.Default.ChevronRight, null, tint = textSecondary(), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ── ProfileScreen ─────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: BookingViewModel,
    themeVm: ThemeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val isDark by themeVm.isDarkMode.collectAsState()

    var nameField  by remember { mutableStateOf(vm.currentUser.value.name) }
    var phoneField by remember { mutableStateOf(vm.currentUser.value.phone) }
    var photoUri   by remember { mutableStateOf<Uri?>(null) }
    var isSaving     by remember { mutableStateOf(false) }
    var saveSuccess  by remember { mutableStateOf(false) }
    var saveError    by remember { mutableStateOf("") }
    var showLogoutDlg by remember { mutableStateOf(false) }
    val profileImageBase64 by vm.profileImageBase64.collectAsState()

    LaunchedEffect(vm.currentUser.value.name, vm.currentUser.value.phone) {
        nameField  = vm.currentUser.value.name
        phoneField = vm.currentUser.value.phone
    }

    val isDirty = nameField.trim() != vm.currentUser.value.name ||
            phoneField.trim() != vm.currentUser.value.phone || photoUri != null

    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoUri = it; isSaving = true; saveError = ""
            vm.uploadProfileImageBase64(context = context, imageUri = it,
                onSuccess = { isSaving = false; saveSuccess = true; photoUri = null },
                onError   = { err -> isSaving = false; saveError = err }
            )
        }
    }

    if (showLogoutDlg) {
        AlertDialog(
            onDismissRequest = { showLogoutDlg = false },
            containerColor = cardBg(),
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFFEE2E2)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Logout, null, tint = Color(0xFFEF4444), modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("ออกจากระบบ?", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textPrimary())
                    Text("คุณต้องการออกจากระบบใช่ไหม", color = textSecondary(), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = null,
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFFEF4444), spotColor = Color(0xFFEF4444))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
                            .clickable {
                                showLogoutDlg = false
                                authViewModel.logout()
                                themeVm.resetTheme()  // เพิ่มบรรทัดนี้
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ออกจากระบบ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    TextButton(onClick = { showLogoutDlg = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("ยกเลิก", color = textSecondary(), textAlign = TextAlign.Center)
                    }
                }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNav(navController, "profile") },
        containerColor = appBg()
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())) {

            // ── Hero ──────────────────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(OrangeDark, OrangeLight,
                        if (isDark) Color(0xFF1A1000) else Color(0xFFFFF3EC))))
                    .padding(top = 48.dp, bottom = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            Modifier.size(100.dp).shadow(16.dp, CircleShape)
                                .clip(CircleShape).background(if (isDark) CardDark else Color.White)
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageBase64.isNotEmpty()) {
                                val bitmap = remember(profileImageBase64) {
                                    val bytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                }
                                bitmap?.let {
                                    Image(bitmap = it, contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                }
                            } else {
                                Icon(Icons.Default.Person, null, tint = if (isDark) Color(0xFF6B7280) else Color(0xFFD1D5DB), modifier = Modifier.size(60.dp))
                            }
                        }
                        Box(
                            Modifier.size(32.dp).shadow(4.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
                                .clip(CircleShape).background(GradientOrange).clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(vm.currentUser.value.name.ifEmpty { "ชื่อผู้ใช้" }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text(vm.currentUser.value.email, color = Color.White.copy(0.82f), fontSize = 13.sp)
                    if (vm.currentUser.value.studentId.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text("ID: ${vm.currentUser.value.studentId}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                }
            }

            // ── Form ──────────────────────────────────────────────────────────
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ข้อมูลส่วนตัว", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textSecondary(), modifier = Modifier.padding(bottom = 2.dp))

                ProfileEditField(value = nameField, onValueChange = { nameField = it; saveSuccess = false; saveError = "" },
                    label = "ชื่อ-นามสกุล", icon = Icons.Default.Badge, editable = true)
                ProfileEditField(value = phoneField, onValueChange = { phoneField = it; saveSuccess = false; saveError = "" },
                    label = "เบอร์โทรศัพท์", icon = Icons.Default.Phone, editable = true, keyboardType = KeyboardType.Phone)

                Spacer(Modifier.height(4.dp))
                Text("ข้อมูลบัญชี", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textSecondary(), modifier = Modifier.padding(bottom = 2.dp))

                ProfileEditField(value = vm.currentUser.value.email, onValueChange = {}, label = "อีเมล", icon = Icons.Default.Email, editable = false)
                ProfileEditField(value = vm.currentUser.value.studentId, onValueChange = {}, label = "รหัสนิสิต", icon = Icons.Default.School, editable = false)

                Spacer(Modifier.height(4.dp))

                AnimatedVisibility(visible = saveError.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEE2E2)).padding(12.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(saveError, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                }
                AnimatedVisibility(visible = saveSuccess) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFECFDF5)).padding(12.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("บันทึกข้อมูลสำเร็จ", color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Save button
                Box(
                    Modifier.fillMaxWidth().height(54.dp)
                        .shadow(if (isDirty) 12.dp else 0.dp, RoundedCornerShape(16.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDirty) GradientOrange else Brush.horizontalGradient(listOf(if (isDark) Color(0xFF2D2D44) else Color(0xFFE5E7EB), if (isDark) Color(0xFF2D2D44) else Color(0xFFE5E7EB))))
                        .clickable(enabled = isDirty && !isSaving) {
                            if (nameField.isBlank())  { saveError = "กรุณากรอกชื่อ"; return@clickable }
                            if (phoneField.isBlank()) { saveError = "กรุณากรอกเบอร์โทร"; return@clickable }
                            isSaving = true; saveError = ""
                            val email = vm.currentUser.value.email
                            Firebase.firestore.collection("users").document(email)
                                .update(mapOf("name" to nameField.trim(), "phone" to phoneField.trim()))
                                .addOnSuccessListener {
                                    vm.currentUser.value = vm.currentUser.value.copy(
                                        name  = nameField.trim(),
                                        phone = phoneField.trim()
                                    )
                                    isSaving = false; saveSuccess = true; photoUri = null
                                }
                                .addOnFailureListener { e -> isSaving = false; saveError = e.message ?: "เกิดข้อผิดพลาด" }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, null, tint = if (isDirty) Color.White else if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("บันทึกข้อมูล", color = if (isDirty) Color.White else if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Dark Mode Switch
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF2A2A3E) else OrangeMain.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                null, tint = OrangeMain, modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary())
                            Text(if (isDark) "🌙 โหมดมืด" else "☀️ โหมดสว่าง", fontSize = 12.sp, color = textSecondary())
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { themeVm.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = Color.White,
                                checkedTrackColor   = OrangeMain,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = if (isDark) Color(0xFF3D3D55) else Color(0xFFD1D5DB)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Logout button
                Box(
                    Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF2A1515) else Color(0xFFFFF1F1))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))

                        .clickable { showLogoutDlg = true },
                    contentAlignment = Alignment.Center,

                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ออกจากระบบ", color = Color(0xFFEF4444), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── ProfileEditField ──────────────────────────────────────────────────────────
@Composable
private fun ProfileEditField(
    value: String, onValueChange: (String) -> Unit, label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, editable: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val isDark = LocalIsDark.current
    OutlinedTextField(
        value = value, onValueChange = { if (editable) onValueChange(it) },
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = if (editable) OrangeMain else textSecondary()) },
        trailingIcon = { if (!editable) Icon(Icons.Default.Lock, null, tint = textSecondary(), modifier = Modifier.size(16.dp)) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        singleLine = true, readOnly = !editable, enabled = editable,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = OrangeMain,
            unfocusedBorderColor    = if (editable) borderColor() else if (isDark) Color(0xFF2D2D44) else Color(0xFFF3F4F6),
            focusedContainerColor   = if (editable) inputBgFocus() else if (isDark) Color(0xFF1E1E2E) else Color(0xFFF9FAFB),
            unfocusedContainerColor = if (editable) inputBg() else if (isDark) Color(0xFF1E1E2E) else Color(0xFFF9FAFB),
            focusedLabelColor       = OrangeMain,
            unfocusedLabelColor     = if (editable) textSecondary() else if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB),
            focusedTextColor        = textPrimary(),
            unfocusedTextColor      = textPrimary(),
            disabledBorderColor     = if (isDark) Color(0xFF2D2D44) else Color(0xFFF3F4F6),
            disabledContainerColor  = if (isDark) Color(0xFF1E1E2E) else Color(0xFFF9FAFB),
            disabledLabelColor      = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB),
            disabledLeadingIconColor = textSecondary(),
            disabledTextColor       = textSecondary(),
        )
    )
}

// ── BottomNav ─────────────────────────────────────────────────────────────────
@Composable
fun BottomNav(navController: NavHostController, current: String = "") {
    NavigationBar(
        containerColor = navBg(),
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(20.dp)
    ) {
        data class NavItem(val route: String, val icon: ImageVector, val label: String)
        listOf(
            NavItem("home",     Icons.Default.Home,     "Home"),
            NavItem("bookings", Icons.Default.DateRange, "Bookings"),
            NavItem("profile",  Icons.Default.Person,   "Profile")
        ).forEach { item ->
            val selected = current == item.route
            NavigationBarItem(
                icon = {
                    Box(
                        if (selected) Modifier.clip(RoundedCornerShape(12.dp)).background(OrangeMain.copy(0.12f)).padding(horizontal = 14.dp, vertical = 5.dp)
                        else Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, null, tint = if (selected) OrangeMain else textSecondary())
                    }
                },
                label = {
                    Text(item.label, fontSize = 11.sp, color = if (selected) OrangeMain else textSecondary(),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                },
                selected = selected,
                onClick = { if (!selected) navController.navigate(item.route) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
fun categoryEmoji(category: String): String = when (category.lowercase()) {
    "badminton"  -> "🏸"
    "basketball" -> "🏀"
    "futsal"     -> "⚽"
    "volleyball" -> "🏐"
    "petanque"   -> "🎯"
    else         -> "🏟️"
}

@Composable
fun CategoryFilterBar(selected: String, categories: List<String>, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 0.dp)) {
        item { CategoryChip(label = "🏟️ All", isSelected = selected == "All", onClick = { onSelect("All") }) }
        items(categories) { cat ->
            CategoryChip(label = "${categoryEmoji(cat)} $cat", isSelected = selected == cat, onClick = { onSelect(cat) })
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(if (isSelected) 6.dp else 0.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) GradientOrange else Brush.horizontalGradient(listOf(chipBg(), chipBg())))
            .border(width = if (isSelected) 0.dp else 1.dp, color = if (isSelected) Color.Transparent else borderColor(), shape = RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isSelected) Color.White else textSecondary(), fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

