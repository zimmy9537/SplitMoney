package com.zimmy.splitmoney.models

data class Expense(
    var expenseIn: Boolean,
    var expenseCode: String,
    var personPhone: String,
    var expenseName: String,
    var personName: String,
    var amount: Double,
    var timestamp: Long
) {
    constructor() : this(true, "", "", "", "", 0.0, 0)
}
