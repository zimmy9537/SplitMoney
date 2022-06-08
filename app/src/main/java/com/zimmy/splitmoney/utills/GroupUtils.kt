package com.zimmy.splitmoney.utills

class GroupUtils {
    companion object {
        fun createGroupCode(uid: String, groupCount: Int): String {
            var groupCode = uid.substring(0, 5)
            groupCode += groupCount.toString()
            return groupCode
        }
    }
}