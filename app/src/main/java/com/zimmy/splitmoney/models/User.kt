package com.zimmy.splitmoney.models

data class User(var name: String,
                var email: String?,
                var promocode: String?,
                var phoneNumber: String,
                var isFemale: Boolean?) {

    constructor(

    ) : this("",null,null,"",null) {

    }
}