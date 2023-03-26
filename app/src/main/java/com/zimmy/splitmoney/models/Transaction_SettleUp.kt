package com.zimmy.splitmoney.models

import java.io.Serializable

data class Transaction_SettleUp(val friendName:String,val friendEmail:String?,val amount:Double,val friendPhone:String):Serializable
