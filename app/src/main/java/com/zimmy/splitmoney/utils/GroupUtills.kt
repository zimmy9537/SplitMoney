package com.zimmy.splitmoney.utils

import java.util.*
import kotlin.streams.asSequence

class GroupUtills {
    companion object {
        fun createGroupCode(uid: String, groupCount: Int): String {
            //TODO SERIOUS BULL SHIT HAPPENING HERE
            //change the method of generation of the group code
            val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
            return Random().ints(5, 0, source.length).asSequence()
                .map(source::get)
                .joinToString("")
        }
    }
}