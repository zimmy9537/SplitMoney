package com.zimmy.splitmoney.models

data class Friend(var name:String,var isFemale:Boolean?,var phone:String?,var amount:Double?){

    constructor():this("",null,null,null)
}
