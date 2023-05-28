package ru.bav.server

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

val BASE_ZONE_ID = ZoneId.of("Europe/Moscow") //Сервер и приложение работает по Московскому времени

fun getStandardTimeZone() : TimeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Moscow"))

fun <T> MutableList<T>.addUnique(t:T){
    if(contains(t))return
    add(t)
}

fun String.shrink(len:Int = 50) : String {
    return if(length > len) substring(0, len) + "..." else this
}

fun Long.dateFormat() : String{
    val formater = SimpleDateFormat("dd.MM.YYYY - HH:mm")
    formater.timeZone = getStandardTimeZone()
    return formater.format(Date(this))
}

fun moscowMillis(time:Long = System.currentTimeMillis()) : Long{
    val calendar = GregorianCalendar.getInstance()
    calendar.timeZone = getStandardTimeZone()
    calendar.time = Date(time)
    return calendar.timeInMillis
}

class Text {
    var payload:String = ""
    var highlight:Boolean = false
}

fun concatHighlights(list:List<Text>) : String {
    var app = ""
    list.forEach {
        if(it.highlight){
            app += "**${it.payload}**"
        }else{
            app += it.payload
        }
    }
    return app
}

fun parseHighlights(s:String) : List<Text>{
    val list = mutableListOf<Text>()
    var curText = Text()
    fun flush(){
        list += curText
    }
    s.forEach {
        if(it == '<'){
            flush()
            curText = Text().apply { highlight = true }
            return@forEach
        }else if(it == '>'){
            flush()
            curText = Text().apply { highlight = false }
            return@forEach
        }
        curText.payload += it
    }
    flush()
    return list
}
fun monthRange(inDay:Long) : Pair<Long, Long>{
    val cl = Calendar.getInstance(TimeZone.getTimeZone(BASE_ZONE_ID))
    cl.time = Date(inDay)
    val fromCopy = cl.timeInMillis
    cl.add(Calendar.MONTH, 1)
    val toCopy = cl.timeInMillis
    return fromCopy to toCopy
}

fun dayRange(inDay:Long) : Pair<Long, Long>{
    val cl = Calendar.getInstance(TimeZone.getTimeZone(BASE_ZONE_ID))
    cl.time = Date(inDay)
    cl.set(Calendar.HOUR_OF_DAY, 0)
    cl.set(Calendar.MINUTE, 0)
    val fromCopy = cl.timeInMillis
    cl.set(Calendar.HOUR_OF_DAY, 23)
    cl.set(Calendar.MINUTE, 59)
    val fromTo = cl.timeInMillis
    /*val day = Date(inDay).toInstant().atZone(BASE_ZONE_ID)
    val fromDay = day.withHour(0).toInstant().toEpochMilli()
    val toDay = day.withHour(23).toInstant().toEpochMilli()*/
    return fromCopy to fromTo
}
fun String.withFirstBigChar() : String {
    var outGen = ""
    var first = true
    forEach {
        if(first){
            outGen += it.uppercase()
            first = false
        }else{
            outGen += it
        }
    }
    return outGen
}
fun minMax(min:Int, max:Int, i:Int) : Int {
    if(i < min)return min
    if(i > max)return max
    return i
}
fun <T> List<T>.subListOrEmpty(from:Int, to:Int) : List<T> {
    val b = size
    return subList(minMax(0, b, from), minMax(0, b, to))
}