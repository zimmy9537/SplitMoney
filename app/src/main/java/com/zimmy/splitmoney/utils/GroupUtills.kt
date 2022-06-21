package com.zimmy.splitmoney.utils

class GroupUtills {
    companion object {
        fun createGroupCode(uid: String, groupCount: Int): String {
            //TODO SERIOUS BULL SHIT HAPPENNING HERE
            //change the method of generation of the group code
            var groupCode = uid.substring(0, 5)
            groupCode += groupCount.toString()
            return groupCode
        }
    }
}