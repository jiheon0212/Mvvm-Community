package com.example.mvvmexample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.repository.FirebaseRepository

class FirebaseDBViewModel: ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    private val _message = firebaseRepository.message

    val message: LiveData<MutableList<MessageData>> get() = _message

    fun sendMessage(chatValue: String) {
        firebaseRepository.sendMessage(chatValue)
    }

    fun getMessage() {
        firebaseRepository.getMessage()
    }
}