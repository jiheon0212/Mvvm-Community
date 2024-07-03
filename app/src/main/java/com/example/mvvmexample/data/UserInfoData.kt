package com.example.mvvmexample.data

data class UserInfoData(
    val userProfileImage: String, // needs default image setting
    val userNickname: String, // default user uid
    val userSex: String = "undefined",
    val userBirthDate: String = "undefined",
)
