package com.zimmy.splitmoney.models

data class Transaction(var friendPhone: String?, var amount: Double) {
    constructor() : this(null, 0.0)
}
