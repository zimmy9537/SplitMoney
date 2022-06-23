package com.zimmy.splitmoney.models

data class Group(var imageUri: String, var groupCode:String, var groupTitle: String, var owe: String, var amount: Double) {

    constructor() : this("", "","","",0.00)
}
