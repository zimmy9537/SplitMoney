package com.zimmy.splitmoney.models

data class ExpenseGroup(
    var expenseCode: String,
    var expenseMap: HashMap<String, Double>?,
    var expenseName: String,
    var paidByMap: HashMap<String, Boolean>?,
    var amount: Double,
    var payment_time: String,
    var registered_time: Long
) {
    constructor() : this("", null, "", null, 0.00, "",0)
}
