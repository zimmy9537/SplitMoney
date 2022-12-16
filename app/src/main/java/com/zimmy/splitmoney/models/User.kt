package com.zimmy.splitmoney.models

data class User(var name: String,
                var email: String?,
                var promocode: String?,
                var phoneNumber: String) {

    constructor(

    ) : this("",null,null,"")
}