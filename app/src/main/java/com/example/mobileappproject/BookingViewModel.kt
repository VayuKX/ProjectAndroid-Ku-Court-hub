package com.example.mobileappproject


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class BookingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var currentUser = mutableStateOf(UserProfile())


    private val _fields = MutableStateFlow<List<SportsField>>(emptyList())
    val fields: StateFlow<List<SportsField>> = _fields

    private val _profileImageBase64 = MutableStateFlow("")
    val profileImageBase64: StateFlow<String> = _profileImageBase64

    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings: StateFlow<List<Booking>> = _myBookings

    // Bookings ของสนามที่กำลังดูอยู่ (สำหรับเช็ค slot)
    private val _fieldBookings = MutableStateFlow<List<Booking>>(emptyList())
    val fieldBookings: StateFlow<List<Booking>> = _fieldBookings

    // ต่อจาก _fieldBookings
    private val _myBookedHoursToday = MutableStateFlow(0)
    val myBookedHoursToday: StateFlow<Int> = _myBookedHoursToday

    // สถานะการจอง
    private val _bookingResult = MutableStateFlow<BookingResult>(BookingResult.Idle)
    val bookingResult: StateFlow<BookingResult> = _bookingResult

    sealed class BookingResult {
        object Idle : BookingResult()
        object Loading : BookingResult()
        object Success : BookingResult()
        data class Error(val message: String) : BookingResult()
    }

    init {
        fetchFields()
    }

    private fun fetchFields() {
        db.collection("sportfields").addSnapshotListener { value, _ ->
            _fields.value = value?.toObjects(SportsField::class.java) ?: emptyList()
        }
    }

    fun fetchMyBookings(email: String) {
        db.collection("bookings")
            .whereEqualTo("userEmail", email)
            .addSnapshotListener { value, _ ->
                _myBookings.value = value?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    // ดึง bookings ของสนามนั้นๆ ในวันที่เลือก
    fun fetchFieldBookings(fieldId: String, date: String) {
        db.collection("bookings")
            .whereEqualTo("fieldId", fieldId)
            .whereEqualTo("date", date)
            .whereEqualTo("status", "confirmed")
            .addSnapshotListener { value, _ ->
                _fieldBookings.value = value?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    fun fetchMyBookedHoursToday(email: String, date: String) {
        db.collection("bookings")
            .whereEqualTo("userEmail", email)
            .whereEqualTo("date", date)
            .whereEqualTo("status", "confirmed")
            .addSnapshotListener { value, _ ->
                _myBookedHoursToday.value = value?.size() ?: 0
            }
    }

    // คำนวณว่า slot ไหนถูกจองแล้วบ้าง
    fun getSlotsStatus(fieldId: String, date: String): List<TimeSlot> {
        val bookedSlots = _fieldBookings.value
        return AVAILABLE_SLOTS.map { (start, end) ->
            val booking = bookedSlots.find { b ->
                isSlotOverlapping(start, end, b.startTime, b.endTime)
            }
            TimeSlot(
                startTime = start,
                endTime = end,
                isBooked = booking != null,
                bookedBy = booking?.userName ?: ""
            )
        }
    }

    // เช็คว่า slot ซ้อนทับกันไหม
    private fun isSlotOverlapping(
        newStart: String, newEnd: String,
        existStart: String, existEnd: String
    ): Boolean {
        val ns = timeToMinutes(newStart)
        val ne = timeToMinutes(newEnd)
        val es = timeToMinutes(existStart)
        val ee = timeToMinutes(existEnd)
        return ns < ee && ne > es
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun bookField(
        field: SportsField,
        date: String,
        selectedSlots: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (selectedSlots.isEmpty()) { onError("กรุณาเลือกช่วงเวลา"); return }
        if (selectedSlots.size > 1) { onError("จองได้ 1 ชม./วันเท่านั้น"); return }

        val startTime = selectedSlots.first()
        val endIndex = AVAILABLE_SLOTS.indexOfFirst { it.first == startTime }
        if (endIndex < 0) { onError("เวลาไม่ถูกต้อง"); return }
        val endTime = AVAILABLE_SLOTS[endIndex].second

        // ① เช็คว่า user จองวันนี้ครบแล้วไหม (ทุกสนาม)
        db.collection("bookings")
            .whereEqualTo("userEmail", currentUser.value.email)
            .whereEqualTo("date", date)
            .whereEqualTo("status", "confirmed")
            .get()
            .addOnSuccessListener { userSnap ->
                if (userSnap.size() >= 1) {
                    onError("คุณจองครบ 1 ชม./วันแล้ว ไม่สามารถจองเพิ่มได้")
                    return@addOnSuccessListener
                }

                // ② เช็ค slot ซ้ำในสนามนั้น
                db.collection("bookings")
                    .whereEqualTo("fieldId", field.id)
                    .whereEqualTo("date", date)
                    .whereEqualTo("status", "confirmed")
                    .get()
                    .addOnSuccessListener { fieldSnap ->
                        val existing = fieldSnap.documents.mapNotNull { it.toObject(Booking::class.java) }
                        val conflict = existing.any { b ->
                            val ns = b.startTime.split(":")[0].toInt() * 60 + b.startTime.split(":")[1].toInt()
                            val ne = b.endTime.split(":")[0].toInt() * 60 + b.endTime.split(":")[1].toInt()
                            val s = startTime.split(":")[0].toInt() * 60 + startTime.split(":")[1].toInt()
                            val e = endTime.split(":")[0].toInt() * 60 + endTime.split(":")[1].toInt()
                            s < ne && e > ns
                        }
                        if (conflict) { onError("ช่วงเวลานี้ถูกจองแล้ว"); return@addOnSuccessListener }

                        // ③ บันทึก
                        val booking = hashMapOf(
                            "fieldId" to field.id,
                            "fieldName" to field.name,
                            "date" to date,
                            "startTime" to startTime,
                            "endTime" to endTime,
                            "timeSlot" to "$startTime - $endTime",
                            "userEmail" to currentUser.value.email,
                            "userName" to currentUser.value.name,
                            "userPhone" to currentUser.value.phone,
                            "studentId" to currentUser.value.studentId,
                            "status" to "confirmed"
                        )
                        db.collection("bookings").add(booking)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onError(e.message ?: "เกิดข้อผิดพลาด") }
                    }
                    .addOnFailureListener { e -> onError(e.message ?: "เกิดข้อผิดพลาด") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "เกิดข้อผิดพลาด") }
    }

    // อัปเดต fetchUserProfile ให้โหลด Base64 ด้วย
    fun fetchUserProfile(email: String) {
        db.collection("users").document(email).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    currentUser.value = UserProfile(
                        name             = doc.getString("name") ?: "User",
                        email            = email,
                        phone            = doc.getString("phone") ?: "",
                        studentId        = doc.getString("studentId") ?: "",
                        profileImageBase64 = doc.getString("profileImageBase64") ?: ""
                    )
                    _profileImageBase64.value = currentUser.value.profileImageBase64
                    fetchMyBookings(email)
                }
            }
    }
    fun cancelBooking(
        bookingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (bookingId.isEmpty()) { onError("ไม่พบรหัสการจอง"); return }
        db.collection("bookings").document(bookingId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "เกิดข้อผิดพลาด") }
    }



    fun uploadProfileImageBase64(
        context: Context,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. เปิดรูปและ Compress ให้เล็กลง
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // 2. Resize ให้เหลือแค่ 200x200 px (เพียงพอสำหรับ profile)
                val resized = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)

                // 3. Compress เป็น JPEG คุณภาพ 70%
                val outputStream = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val imageBytes = outputStream.toByteArray()

                // 4. แปลงเป็น Base64
                val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                // ตรวจขนาด (ต้องไม่เกิน ~700KB หลัง encode)
                val sizeKB = base64String.length / 1024
                if (sizeKB > 700) {
                    withContext(Dispatchers.Main) { onError("รูปใหญ่เกินไป กรุณาเลือกรูปอื่น") }
                    return@launch
                }

                // 5. บันทึกลง Firestore
                db.collection("users").document(currentUser.value.email)
                    .update("profileImageBase64", base64String)
                    .addOnSuccessListener {
                        _profileImageBase64.value = base64String
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) {
                            onError(e.message ?: "บันทึกไม่สำเร็จ")
                        }
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "เกิดข้อผิดพลาด") }
            }
        }
    }
}