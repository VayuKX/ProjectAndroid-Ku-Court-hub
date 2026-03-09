package com.example.mobileappproject

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun FieldDetailScreen(
    navController: NavHostController,
    vm: BookingViewModel,
    field: SportsField
) {
    val isDark = LocalIsDark.current
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var bookingError by remember { mutableStateOf("") }
    var bookingSuccess by remember { mutableStateOf(false) }

    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val fieldBookings by vm.fieldBookings.collectAsState()
    val myBookedHours by vm.myBookedHoursToday.collectAsState()
    val userAlreadyBooked = myBookedHours >= 1

    LaunchedEffect(field.id, dateStr) {
        vm.fetchFieldBookings(field.id, dateStr)
        vm.fetchMyBookedHoursToday(vm.currentUser.value.email, dateStr)
        selectedSlot = null
        bookingError = ""
    }

    val slotsStatus = remember(fieldBookings, dateStr) {
        vm.getSlotsStatus(field.id, dateStr)
    }

    val placeholderRes = when (field.Category) {
        "Badminton" -> R.drawable.build13
        "Basketball" -> R.drawable._4823460
        "Futsal"-> R.drawable.build21
        "Volleyball" -> R.drawable.screenshot_2026_03_06_201914
        "Petanque" -> R.drawable._11581344_9863040550460563_8116113566137963266_n
        else -> R.drawable.chatgpt_image_5____2569_21_52_10
    }

    val dialogBg = if (isDark) SurfaceCardDark else Color.White

    // ── Success Dialog ────────────────────────────────────────────────────────
    if (bookingSuccess) {
        val slotStart = selectedSlot ?: ""
        val endTime = AVAILABLE_SLOTS.firstOrNull { it.first == slotStart }?.second ?: ""
        AlertDialog(
            onDismissRequest = { bookingSuccess = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.size(72.dp)
                            .shadow(16.dp, CircleShape, ambientColor = SuccessGreen, spotColor = SuccessGreen)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF34D399), SuccessGreen))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("จองสำเร็จ! 🎉", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = textPrimary())
                    Text("ระบบได้รับการจองของคุณแล้ว", color = textSecondary(), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        Modifier.clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(0.08f)).padding(12.dp, 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SportsSoccer, null, tint = OrangeMain, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(field.name, color = OrangeMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF0D2010) else Color(0xFFF0FDF4))
                            .padding(16.dp, 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("วันที่", color = textSecondary(), fontSize = 11.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                                color = textPrimary(), fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                        Box(Modifier.width(1.dp).height(36.dp).background(if (isDark) Color(0xFF1A4020) else Color(0xFFD1FAE5)))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("เวลา", color = textSecondary(), fontSize = 11.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("$slotStart – $endTime", color = SuccessGreen, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(RoundedCornerShape(14.dp)).background(GradientOrange)
                            .clickable { bookingSuccess = false; navController.navigate("bookings") },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ดูการจองของฉัน", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { bookingSuccess = false; selectedSlot = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("ปิด", color = textSecondary())
                    }
                }
            }
        )
    }

    // ── Confirm Dialog ────────────────────────────────────────────────────────
    if (showConfirmDialog && selectedSlot != null) {
        val slotStart = selectedSlot!!
        val endTime = AVAILABLE_SLOTS.firstOrNull { it.first == slotStart }?.second ?: ""
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.size(56.dp)
                            .shadow(12.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(CircleShape).background(GradientOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EventAvailable, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("ยืนยันการจอง", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textPrimary())
                    Text("ตรวจสอบข้อมูลก่อนยืนยัน", color = textSecondary(), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookingInfoRow(Icons.Default.SportsSoccer, "สนาม", field.name)
                    BookingInfoRow(Icons.Default.CalendarMonth, "วันที่", selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
                    BookingInfoRow(Icons.Default.Schedule, "เวลา", "$slotStart – $endTime (1 ชม.)")
                    BookingInfoRow(Icons.Default.Person, "ชื่อ", vm.currentUser.value.name)
                    BookingInfoRow(Icons.Default.School, "รหัสนิสิต", vm.currentUser.value.studentId)
                    BookingInfoRow(Icons.Default.Phone, "เบอร์โทร", vm.currentUser.value.phone)
                    AnimatedVisibility(visible = bookingError.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEE2E2)).padding(12.dp, 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(bookingError, color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                            .clip(RoundedCornerShape(14.dp)).background(GradientOrange)
                            .clickable {
                                vm.bookField(field, dateStr, listOf(slotStart),
                                    onSuccess = { showConfirmDialog = false; bookingSuccess = true },
                                    onError   = { bookingError = it })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ยืนยันการจอง", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    TextButton(onClick = { showConfirmDialog = false; bookingError = "" }, modifier = Modifier.fillMaxWidth()) {
                        Text("ยกเลิก", color = textSecondary(), textAlign = TextAlign.Center)
                    }
                }
            }
        )
    }

    // ── Main Scaffold ─────────────────────────────────────────────────────────
    Scaffold(containerColor = appBg()) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {

            // ── Hero ──────────────────────────────────────────────────────────
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                if (field.imageUrl.isNotEmpty()) {
                    AsyncImage(model = field.imageUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    // แล้วใช้ placeholderRes ตรงนี้
                    Image(
                        painter = painterResource(id = placeholderRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0x22000000), Color(0xDD000000)), startY = 80f)
                    )
                )
                // Back button
                Box(
                    Modifier.padding(16.dp).align(Alignment.TopStart)
                        .size(42.dp).shadow(8.dp, CircleShape)
                        .clip(CircleShape).background(Color.Black.copy(0.4f))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                // Hero info
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(OrangeMain, OrangeDark)))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text("${categoryEmoji(field.Category)}  ${field.Category}",
                            color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(field.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = OrangeLight, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(field.location, color = Color.White.copy(0.85f), fontSize = 13.sp)
                        Spacer(Modifier.width(14.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                    }
                }
            }

            // ── Scrollable Content ────────────────────────────────────────────
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Info Chips
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickInfoChip(Icons.Default.Schedule, "เวลาเปิด",   "18:00–22:00",  Modifier.weight(1f))
                    QuickInfoChip(Icons.Default.Person,   "โควต้า/วัน", "1 ชม./บัญชี", Modifier.weight(1f))
                }

                // Description
                if (field.description.isNotEmpty()) {
                    Card(shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg())) {
                        Row(Modifier.padding(16.dp)) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(0.1f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, null, tint = OrangeMain, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(field.description, color = textSecondary(), fontSize = 13.sp,
                                lineHeight = 20.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Already booked banner
                AnimatedVisibility(visible = userAlreadyBooked, enter = fadeIn() + expandVertically()) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF2A1515) else Color(0xFFFEE2E2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                            .padding(16.dp, 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEF4444).copy(0.12f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Block, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("จองครบแล้ววันนี้", color = Color(0xFFEF4444), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Text("1 บัญชีจองได้สูงสุด 1 ชม./วัน", color = Color(0xFFEF4444).copy(0.75f), fontSize = 12.sp)
                        }
                    }
                }

                // ── Date Card ────────────────────────────────────────────────
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(0.1f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, null, tint = OrangeMain, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("เลือกวันที่", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
                                color = textPrimary(), modifier = Modifier.weight(1f))
                            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cardBg2()).padding(12.dp, 5.dp)) {
                                Text(today.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                                    color = textSecondary(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        val visibleDays = remember { (0..5).map { today.plusDays(it.toLong()) } }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            visibleDays.forEach { date ->
                                val isToday    = date == today
                                val isSelected = date == selectedDate
                                val thaiDow = when (date.dayOfWeek.value % 7) {
                                    0 -> "อา"; 1 -> "จ"; 2 -> "อ"; 3 -> "พ"
                                    4 -> "พฤ"; 5 -> "ศ"; else -> "ส"
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(if (isSelected) 6.dp else 0.dp, RoundedCornerShape(14.dp),
                                            ambientColor = OrangeMain, spotColor = OrangeMain)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            when {
                                                isSelected -> GradientOrange
                                                else -> Brush.linearGradient(listOf(chipBg(), chipBg()))
                                            }
                                        )
                                        .border(1.dp, if (isSelected) Color.Transparent else borderColor(), RoundedCornerShape(14.dp))
                                        .clickable(enabled = isToday) { }
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(thaiDow, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White.copy(0.85f) else textSecondary())
                                    Spacer(Modifier.height(3.dp))
                                    Text("${date.dayOfMonth}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else textSecondary())
                                }
                            }
                        }
                    }
                }

                // ── Time Slots Card ───────────────────────────────────────────
                Card(shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg()), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(0.1f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Schedule, null, tint = OrangeMain, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("เลือกช่วงเวลา", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = textPrimary())
                                Text("เลือกได้ 1 ช่วง • 1 บัญชีต่อ 1 วัน", color = textSecondary(), fontSize = 11.sp)
                            }
                            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(OrangeMain.copy(0.08f)).padding(12.dp, 5.dp)) {
                                Text(selectedDate.format(DateTimeFormatter.ofPattern("d MMM")),
                                    color = OrangeMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        slotsStatus.forEach { slot ->
                            val isSelected = slot.startTime == selectedSlot
                            val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
                            val slotMinutes = slot.startTime.split(":")[0].toInt() * 60 + slot.startTime.split(":")[1].toInt()
                            val isPast = selectedDate == today && slotMinutes <= nowMinutes
                            SlotItem(
                                slot = slot, isSelected = isSelected, isPast = isPast,
                                isUserBlocked = userAlreadyBooked,
                                onToggle = {
                                    if (slot.isBooked || isPast || userAlreadyBooked) return@SlotItem
                                    selectedSlot = if (isSelected) null else slot.startTime
                                    bookingError = ""
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                // ── Booking Summary ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = selectedSlot != null && !userAlreadyBooked,
                    enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                    exit    = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
                ) {
                    val slotStart = selectedSlot ?: ""
                    val endTime = AVAILABLE_SLOTS.firstOrNull { it.first == slotStart }?.second ?: ""
                    Card(shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg()), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text("สรุปการจอง", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = textPrimary())
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (isDark) listOf(Color(0xFF2A1F10), Color(0xFF251A0D))
                                            else listOf(Color(0xFFFFF7ED), Color(0xFFFFF1E0))
                                        )
                                    )
                                    .border(1.dp, OrangeMain.copy(0.15f), RoundedCornerShape(14.dp))
                                    .padding(16.dp, 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, tint = OrangeMain, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                                            color = textPrimary(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, tint = OrangeMain, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text("$slotStart – $endTime", color = OrangeMain, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    }
                                }
                                Box(
                                    Modifier.shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                                        .clip(RoundedCornerShape(12.dp)).background(GradientOrange)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("1 ชม.", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Box(
                                Modifier.fillMaxWidth().height(54.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = OrangeMain, spotColor = OrangeMain)
                                    .clip(RoundedCornerShape(16.dp)).background(GradientOrange)
                                    .clickable { showConfirmDialog = true; bookingError = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EventAvailable, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("จองเลย", color = Color.White, fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── SlotItem ──────────────────────────────────────────────────────────────────
@Composable
private fun SlotItem(
    slot: TimeSlot, isSelected: Boolean, isPast: Boolean,
    isUserBlocked: Boolean, onToggle: () -> Unit
) {
    val isDark = LocalIsDark.current
    val isDisabled = slot.isBooked || isPast || isUserBlocked

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 0.dp else 1.dp, RoundedCornerShape(16.dp),
                ambientColor = if (isSelected) OrangeMain else Color.Transparent,
                spotColor    = if (isSelected) OrangeMain else Color.Transparent)
            .clip(RoundedCornerShape(16.dp))
            .background(when {
                isSelected -> Brush.horizontalGradient(listOf(OrangeMain.copy(0.08f), OrangeMain.copy(0.12f)))
                isDisabled -> Brush.horizontalGradient(listOf(cardBg2(), cardBg2()))
                else       -> Brush.horizontalGradient(listOf(cardBg(), cardBg()))
            })
            // ใหม่ - บางลง และใช้ opacity น้อยลง
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) OrangeMain.copy(alpha = 0.6f) else borderColor(),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isDisabled) { onToggle() }
            .padding(16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(24.dp)
                .shadow(if (isSelected) 4.dp else 0.dp, CircleShape, ambientColor = OrangeMain, spotColor = OrangeMain)
                .clip(CircleShape)
                .background(if (isSelected) GradientOrange else Brush.radialGradient(listOf(chipBg(), chipBg())))
                .border(if (isSelected) 0.dp else 1.5.dp, borderColor(), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${slot.startTime} – ${slot.endTime}",
                color = when {
                    isDisabled -> textSecondary()
                    isSelected -> OrangeMain
                    else       -> textPrimary()
                },
                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                when {
                    slot.isBooked -> "ถูกจองแล้ว"
                    isPast        -> "เวลาผ่านแล้ว"
                    isUserBlocked -> "จองครบแล้ววันนี้"
                    else          -> "ว่าง"
                },
                color = when {
                    slot.isBooked || isPast -> Color(0xFFEF4444).copy(0.7f)
                    isUserBlocked           -> textSecondary()
                    else                    -> SuccessGreen
                },
                fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        Box(
            Modifier.clip(RoundedCornerShape(10.dp))
                .background(when {
                    slot.isBooked -> if (isDark) Color(0xFF2A1515) else Color(0xFFFEE2E2)
                    isPast        -> cardBg2()
                    isUserBlocked -> cardBg2()
                    isSelected    -> OrangeMain.copy(0.12f)
                    else          -> SuccessGreen.copy(0.1f)
                })
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                when {
                    slot.isBooked -> "เต็ม"
                    isPast        -> "หมดเวลา"
                    isUserBlocked -> "ใช้สิทธิไปแล้ว"
                    isSelected    -> "เลือก ✓"
                    else          -> "ว่าง"
                },
                color = when {
                    slot.isBooked || isPast -> Color(0xFFEF4444).copy(0.7f)
                    isUserBlocked           -> textSecondary()
                    isSelected              -> OrangeMain
                    else                    -> SuccessGreen
                },
                fontSize = 11.sp, fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ── QuickInfoChip ─────────────────────────────────────────────────────────────
@Composable
private fun QuickInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String, modifier: Modifier = Modifier
) {
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg()), modifier = modifier) {
        Column(Modifier.padding(10.dp, 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(0.1f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = OrangeMain, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, color = textPrimary(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(label, color = textSecondary(), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

// ── BookingInfoRow ────────────────────────────────────────────────────────────
@Composable
private fun BookingInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cardBg2()).padding(12.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(OrangeMain.copy(0.1f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = OrangeMain, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("$label: ", color = textSecondary(), fontSize = 13.sp)
        Text(value, color = textPrimary(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }

}