package com.example.mvvmexample.repository

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mvvmexample.data.MessageData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirebaseRepository() {
    private val database = Firebase.database
    // 지금은 날짜 기준으로 분기를 해서 채팅 데이터를 가져오는 방식이지만 조금 더 효율적인 방법을 찾아보자
    private val databaseRef = database.reference.child("text_message").child(getCurrentDayAndTime().first)

    private val _message = MutableLiveData<MutableList<MessageData>>()
    val message: LiveData<MutableList<MessageData>> get() = _message

    private val auth = Firebase.auth

    private val _userData = MutableLiveData<FirebaseUser>()
    val userData: LiveData<FirebaseUser> get() = _userData

    // realtime db에 메세지 업로드
    fun sendMessage(chatValue: String) {
        databaseRef.push().setValue(MessageData(
            time = getCurrentDayAndTime().second,
            message = chatValue
        ))
    }

    // realtime db에 value값이 변화되면 실행되는 method
    fun getMessage() {
        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valueList = mutableListOf<MessageData>()
                // 해당 Ref에 소속된 모든 메세지가 snapshot에 담긴다.
                // children 호출로 foreach 확장 함수를 호출하고 개별 snapshot으로 쪼개어 리스트에 담는다.
                snapshot.children.forEach { dataSnapshot ->
                    val postValue = dataSnapshot.getValue(MessageData::class.java)
                    valueList.add(postValue!!)
                }
                _message.postValue(valueList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "$error")
            }
        })
    }

    // 현재 날짜를 yyyyMMdd 형식 & 현재 대한민국 시간을 기준으로 a hh:mm 형식으로 반환하주는 method
    private fun getCurrentDayAndTime(): Pair<String, String> {
        val currentDate = Date()
        val currentZone = TimeZone.getTimeZone("Asia/Seoul")
        val dayFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        val timeFormat = SimpleDateFormat("a hh:mm", Locale.KOREA)

        dayFormat.timeZone = currentZone
        timeFormat.timeZone = currentZone

        val dayValue = dayFormat.format(currentDate)
        val timeValue = timeFormat.format(currentDate)

        return Pair(dayValue, timeValue)
    }

    // google id token을 사용해서 firebase auth를 진행하는 method
    fun firebaseAuthWithGoogleIdToken(googleIdToken: String) {
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _userData.postValue(auth.currentUser)
                } else {
                    Log.e(TAG, "${it.exception}")
                }
            }
    }
}