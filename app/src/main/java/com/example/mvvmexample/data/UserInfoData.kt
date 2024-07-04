package com.example.mvvmexample.data

data class UserInfoData(
    val userProfileImage: String? = null, // needs default image setting
    val userNickname: String? = null, // default user uid
    val userSex: String = "undefined",
    val userBirthDate: String = "undefined",
)
