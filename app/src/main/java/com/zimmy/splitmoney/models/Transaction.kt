package com.zimmy.splitmoney.models

data class Transaction(var friend: String?, var amount: Double) {
    constructor() : this(null, 0.0)
}
