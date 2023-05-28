package ru.bav.server.schemas

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.URL

fun main(){
    fun a(kg:Float, rost:Float, age:Float) : Float{
        return 66.5f + (13.75f * kg) + (5.003f * rost) - (6.755f * age)
    }
    fun w(kg:Float, rost:Float, age:Float) : Float{
        return 55.1f + (9.563f * kg) + (1.850f * rost) - (4.676f * age)
    }
    println("a = ${a(115f, 191f, 19f)}")

//val knos = ParseSite().parseUser()
    /*File("C:\\Users\\beena\\Desktop\\Датасеты\\urls.txt").readLines().forEach {
        knos.openKNO(it)
    }*/
    /*val knosList = ParseSite.Knos().apply {
        items += knos
    }
    File("C:\\Users\\beena\\Desktop\\Датасеты\\outknos.json").writeText("${Json.encodeToString(knosList)}")*/
}
class ParseSite {

    fun openKNO(url:String){
        val to = url.split("?")[0] + "/(knoDetails:npa)"
        Desktop.getDesktop().browse(URI(to))
    }

    @Serializable
    class Knos {
        val items:MutableList<Kno> = mutableListOf()
    }

    @Serializable
    class Info {
        var label:String = ""
        var payload:MutableList<String> = mutableListOf()
    }

    @Serializable
    class Kno {
        var title:String = ""
        var fio:String = ""
        var pos:String = ""
        var act:String = ""
        var imgSrc:String = ""
        val infos = mutableListOf<Info>()
        val npas = mutableListOf<NPADoc>()
    }

    @Serializable
    class NPADoc {
        var text:String = ""
        var date:String = ""
        var fileInfo:String = ""
    }

    fun parseUser() : List<Kno> {
        val list = mutableListOf<Kno>()
        File("C:\\Users\\beena\\Desktop\\Датасеты\\dopinsites.txt").readText().split("%%NEXT").forEach {
            val doc = Jsoup.parse(it)
            val title = doc.select("div .title").text()
            val kno = Kno()
            kno.title = title
            val infoGrid = doc.select("div .common-info-grid")
            if(infoGrid.size > 0){
                for(el in infoGrid){
                    if(el.className().contains("caption"))continue
                    for(elInfo in el.childNodes()){
                        if(elInfo is Element){
                            val capt = elInfo.childNodes().mapNotNull { it as? Element }.firstOrNull { it.className().contains("caption") }
                            if(capt != null){
                                val allOthers = elInfo.childNodes() - capt
                                val item = Info()
                                val els = allOthers.map { it as? Element }
                                val caption = capt.text()
                                item.label = caption
                                els.forEach {
                                    item.payload += it?.textNodes()?.joinToString("\n") ?: "No!"
                                }
                                kno.infos += item
                                //println("DA")
                            }
                        }
                    }
                }
            }
            //println("DA: ${title}")
            list += kno
        }
        fun bytitle(tit:String) : Kno? = list.firstOrNull { it.title.equals(tit, true) }
        /*File("C:\\Users\\beena\\Desktop\\Датасеты\\urls.txt").readLines().forEach {
            openKNO(it)
        }*/
        File("C:\\Users\\beena\\Desktop\\Датасеты\\npasites.txt").readText().split("%%TEMP").forEach {
            val doc: Document = Jsoup.parse(it)
            val title = doc.select("div .title").text()
            val kno = bytitle(title) ?: return@forEach
            val items = doc.select("div .p-grid .padding-top-32").select("div .item")
            for(item in items){
                val text = item.select("div .npa-card-text").text()
                val date = item.select("div .npa-card-date").text()
                val fileSize = item.select("div .npa-card-file-info").text()
                kno.npas += NPADoc().apply {
                    this.text = text
                    this.date = date
                    this.fileInfo = fileSize
                }
            }
            println("NPA: ${kno.title}")
        }
        val uniq = mutableListOf<String>()
        File("C:\\Users\\beena\\Desktop\\Датасеты\\sites.txt").readText().split("%%NEXT").apply { println("SIZE: ${size}") }.forEach {

            val doc: Document = Jsoup.parse(it)
            val title = doc.select("div .title").text()
            val kno = bytitle(title)!!
            val fio = doc.select("div .fio").text()
            val img = doc.select("img").attr("src")
            if(uniq.contains(fio))return@forEach
            uniq += fio
            kno.fio = fio
            kno.imgSrc = "https://knd.mos.ru$img"
            val position = doc.select("div .position").text()
            kno.pos = position
            val act = doc.select("div .activity-div").text()
            kno.act = act
        }

        /*println("SIZE: ${list.size}")
        list.forEach {
            println("")
            println("[${it.title}]")
            println("Глава: ${it.fio}")
            println("   ${it.pos}")
            println("Деятельность: ${it.act}")
        }*/
        //URL(url).openStream().bufferedReader().readText()
        /*text = text.replace("<app-root ng-version=\"14.2.0\">", "")
        text = text.replace("</app-root>", "")*/
        return list
    }
}