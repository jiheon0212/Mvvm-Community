package com.example.mvvmexample.ui.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmexample.data.UserInfoData
import com.example.mvvmexample.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseAuthViewModel: ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    private val _userData = firebaseRepository.userData
    private val _userInformation = firebaseRepository.userInformation

    val userData: LiveData<FirebaseUser> get() = _userData
    val userInformation: LiveData<UserInfoData> get() = _userInformation

    fun getFirebaseUser(googleIdToken: String) {
        firebaseRepository.firebaseAuthWithGoogleIdToken(googleIdToken)
    }
    
    // userInformation을 auth.uid와 일치하는 데이터를 firestore에서 찾아 가져오는 method 구현하기

    // userInformation을 default 값에서 수정하여 저장하는 method
    fun getUserInformation() {
        viewModelScope.launch {
            // 가벼운 작업은 default 값인 main 디스패쳐에서 진행해준다.
            firebaseRepository.getUserInformation()
        }
    }
    fun updateUserInformation(modifyData: Any, dataType: Int) {
        viewModelScope.launch {
            // IO부분에서 코루틴을 진행시켜 메인 디스패쳐와 분리해주며 비동기적코드를 순차적으로 진행할 수 있게되어 storage 업로드 이후에
            // ui 업데이트가 진행된다.
            try {
                withContext(Dispatchers.IO) {
                    firebaseRepository.updateUserInformation(modifyData, dataType)
                }
                withContext(Dispatchers.IO) {
                    firebaseRepository.getUserInformation()
                }
            } catch (e:Exception) {
                Log.e(TAG, "coroutine error", e)
            }
        }
    }
}