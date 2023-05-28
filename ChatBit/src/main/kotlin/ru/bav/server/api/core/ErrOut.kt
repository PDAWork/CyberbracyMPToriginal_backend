package ru.bav.server.api.core

data class ErrOut(val msg:String, val status:Int = 404)
data class UserErrOut(val msg:String)