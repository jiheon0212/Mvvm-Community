package com.example.mvvmexample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.data.UserChat
import com.example.mvvmexample.repository.FirebaseRepository
import kotlinx.coroutines.launch

class FirebaseDBViewModel: ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    private val _message = firebaseRepository.message
    val message: LiveData<MutableList<MessageData>> get() = _message

    private val _user = MutableLiveData<UserChat>()
    val user: LiveData<UserChat> get() = _user

    fun sendMessage(chatValue: String, userChat: UserChat) {
        firebaseRepository.sendMessage(chatValue, userChat)
    }

    fun getMessage() {
        firebaseRepository.getMessage()
    }

    fun foundUser() {
        viewModelScope.launch {
            val user = firebaseRepository.foundUserInformation()
            _user.postValue(user)
        }
    }
}