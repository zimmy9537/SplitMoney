package com.zimmy.splitmoney.models

import java.io.Serializable

data class Friend(var name:String,var isFemale:Boolean?,var phone:String?,var amount:Double?):Serializable{

    constructor():this("",null,null,null)
    constructor(name:String, phone:String):this("",null,null,null)
}
