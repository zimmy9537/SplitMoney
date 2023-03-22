package com.zimmy.splitmoney.models

data class TransactionResult(var receiver: String?, var sender: String?, var amount: Double) {
    constructor() : this(null, null, 0.0)
}
