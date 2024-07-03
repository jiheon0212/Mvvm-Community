package com.example.mvvmexample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mvvmexample.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthViewModel: ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    private val _userData = firebaseRepository.userData

    val userData: LiveData<FirebaseUser> get() = _userData

    fun getFirebaseUser(googleIdToken: String) {
        firebaseRepository.firebaseAuthWithGoogleIdToken(googleIdToken)
    }
}