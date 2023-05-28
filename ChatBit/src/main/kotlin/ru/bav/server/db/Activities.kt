package ru.bav.server.db

import java.util.regex.Pattern

class Activity(val code:String, val text:String) {

}
object Activities {
    val PAT = Pattern.compile("(([0-9]{1,2}.[0-9]{1,2}.[0-9]{1,2}) ([А-Яа-я ,\\-\\(\\)]+))|(([0-9]{1,2}.[0-9]{1,2}) ([А-Яа-я ,\\-\\(\\)]+))")

    fun parseAll(input:String) : List<Activity> {
        val mat = PAT.matcher(input)
        val list = mutableListOf<Activity>()
        while(mat.find()){

            val gtcode = mat.group(2) ?: null
            val gttext = mat.group(3) ?: null

            val gcode = mat.group(5) ?: null
            val gtext = mat.group(6) ?: null
            if(gtcode != null && gttext != null){
                list += Activity(gtcode, gttext)
            }
            if(gcode != null && gtext != null){
                list += Activity(gcode, gtext)
            }
        }
        return list
    }
}