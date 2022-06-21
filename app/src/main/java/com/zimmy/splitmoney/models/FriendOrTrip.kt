package com.zimmy.splitmoney.models

data class FriendOrTrip(var imageUri: String, var title: String, var owe: String?, var amount: Double?,var phone:String?)
//todo phone will be null in case of a trip
