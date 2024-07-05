package com.example.mvvmexample.data

import android.graphics.Bitmap

interface ModifyCallback {
    fun nicknameModify(nickname: String)
    fun imageModify(bitmap: Bitmap)
    fun sexModify(sex: String)
    fun birthDateModify(date: String)
}