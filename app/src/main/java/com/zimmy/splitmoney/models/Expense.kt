package com.zimmy.splitmoney.models

data class Expense(
    var expenseIn: Boolean,
    var expenseCode: String,
    var expenseMap: HashMap<String, Double>?,
    var expenseName: String,
    var paidByMap: HashMap<String, Boolean>?,
    var amount: Double,
    var timestamp: String
) {
    constructor() : this(true, "", null, "", null, 0.0, "")

    constructor(
        expenseCode: String,
        expenseMap: HashMap<String, Double>?,
        expenseName: String,
        paidByMap: HashMap<String, Boolean>?,
        amount: Double,
        timestamp: String
    ) : this(true, "", null, "", null, 0.0, "")
}
