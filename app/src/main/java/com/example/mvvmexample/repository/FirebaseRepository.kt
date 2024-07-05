package com.example.mvvmexample.repository

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.data.UserInfoData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FirebaseRepository() {
    private val database = Firebase.database
    // 지금은 날짜 기준으로 분기를 해서 채팅 데이터를 가져오는 방식이지만 조금 더 효율적인 방법을 찾아보자
    private val databaseRef = database.reference.child("text_message").child(getCurrentDayAndTime().first)
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    // msg 관련
    private val _message = MutableLiveData<MutableList<MessageData>>()
    val message: LiveData<MutableList<MessageData>> get() = _message

    // user 관련
    private val _userData = MutableLiveData<FirebaseUser>()
    private val _userInformation = MutableLiveData<UserInfoData>()
    val userData: LiveData<FirebaseUser> get() = _userData
    val userInformation: LiveData<UserInfoData> get() = _userInformation

    // userInformation값을 auth를 통과한 사용자는 default로 setting
    suspend fun getUserInformation() {
        val userUid = auth.uid!!
        val userInfoData = UserInfoData(userNickname = userUid)

        val snapshot = db.collection("userInformation").document(userUid)
            .get().await()

        if (snapshot.exists()) {
            val userInfoResult = snapshot.toObject<UserInfoData>()!!
            _userInformation.postValue(userInfoResult)
        } else {
            db.collection("userInformation").document(userUid)
                .set(userInfoData).await()
            _userInformation.postValue(userInfoData)
        }
    }

    // userInformation을 update 해주는 method
    // dataType을 지정해 해당 항목만 db에서 업데이트 해준다.
    // storage 업로드가 비동기로 이뤄지면서 ui 업데이트 이후에 사진이 업로드되어 변경되지 않는 문제 발생
    // -> 기존 google로그인 process 구성과 차이점
    // -> 기존에는 successlistener에 callback으로 해결했으나 callback이 5개가 겹치며 가독성이 떨어짐 -> 코루틴으로 변경
    // -> 각각의 task await()을 호출하여 값을 기다리고 그 값을 사용한다.

    // todo - 향후 코드에서는 coroutine을 활용해서 return 값을 제공받아 viewmodel에서 데이터를 가공할 수 있도록 구현해보기
    suspend fun updateUserInformation(modifyData: Any, dataType: Int) {
        val documentRef = db.collection("userInformation").document(auth.uid!!)
        when (dataType) {
            1 -> {
                val storageRef = storage.reference.child("images/${auth.uid}.jpg")

                val byteOutputStream = ByteArrayOutputStream()
                (modifyData as Bitmap).compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)
                val data = byteOutputStream.toByteArray()
                storageRef.putBytes(data).await()

                val uri = storageRef.downloadUrl.await()
                documentRef.update("userProfileImage", uri).await()
            }
            2 -> {
                documentRef.update("userNickname", modifyData).await()
            }
            3 -> {
                documentRef.update("userSex", modifyData).await()
            }
            4 -> {
                documentRef.update("userBirthDate", modifyData).await()
            }
        }
    }

    // realtime db에 메세지 업로드
    fun sendMessage(chatValue: String) {
        databaseRef.push().setValue(MessageData(
            time = getCurrentDayAndTime().second,
            message = chatValue,
            sender = auth.uid!!
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