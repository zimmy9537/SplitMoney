package com.zimmy.splitmoney.models

data class Expense_Group(
    var expenseCode: String,
    var expenseMap: HashMap<String, Double>?,
    var expenseName: String,
    var paidByMap: HashMap<String, Boolean>?,
    var amount: Double,
    var timestamp: String
) {
    constructor() : this("", null, "", null, 0.00, "")
}
