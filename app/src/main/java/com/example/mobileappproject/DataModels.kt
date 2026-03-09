package com.example.mobileappproject

// 1. ข้อมูลผู้ใช้
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val studentId: String = "",
    val profileImageBase64: String = ""  // เพิ่มตรงนี้
)

// 2. ข้อมูลสนามกีฬา
data class SportsField(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val Category: String = ""
)

// 3. ข้อมูลการจอง
data class Booking(
    val id: String = "",
    val fieldId: String = "",
    val fieldName: String = "",
    val date: String = "",           // format: "2024-02-12"
    val startTime: String = "",      // format: "18:00"
    val endTime: String = "",        // format: "20:00"
    val timeSlot: String = "",       // display: "18:00 - 20:00"
    val userEmail: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val studentId: String = "",
    val status: String = "confirmed"
)

// 4. Time Slot สำหรับแสดง UI
data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean = false,
    val bookedBy: String = ""
)

// Slots ที่เปิดให้จอง 18:00 - 22:00
val AVAILABLE_SLOTS = listOf(
    "18:00" to "19:00",
    "19:00" to "20:00",
    "20:00" to "21:00",
    "21:00" to "22:00",
    "22:00" to "23:00",
    "23:00" to "24:00"
)