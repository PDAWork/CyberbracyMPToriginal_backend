package ru.bav.server

import ObRequires
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.bav.entry.Main
import ru.bav.entry.TestNative
import ru.bav.server.api.Endpoints
import ru.bav.server.api.OnesignalUtils
import ru.bav.server.chat.Chat
import ru.bav.server.db.SystemDB
import ru.bav.server.db.schedule.Month
import ru.bav.server.db.schedule.SlotID
import ru.bav.server.schemas.ParseSite
import ru.bav.server.schemas.Schedules
import java.io.File


object CommandExecutor {

    var testChat = Chat()

    fun loadJson(){
        val file = File("${Main.getJarFolder()}/out.json")
        val fileSlots = File("${Main.getJarFolder()}/slots.json")
        DATASET = Json.decodeFromString<ObRequires>(file.readText())
        DATASET_SCHEDULES = Json.decodeFromString<Schedules>(fileSlots.readText())

        DATASET_SCHEDULES.organizations.forEach {org->
            val curSet = DATASET.organs.firstOrNull { it.name.equals(org.lowName, true) } ?: let {
                println("[!] FAILED MAP ${org.lowName} ${org.name}")
                return@forEach
            }
            org.organ = curSet
            curSet.sOrg = org
            println("MAPPING: ${org.lowName} -> ${curSet.name} (${org.name})")
        }
        println("Mapping done.")
    }

    fun executeRaw(line: String) {
        try{
            val args = line.split(" ")
            when(args[0]){
                "conslist"->{
                    val list = Endpoints.USER.consultMonthList(args[1]) as List<Month.DaySlots>
                    list.forEach {
                        println("DAY ENTRY ${it.timestamp.dateFormat()}")
                        it.slots.forEach {
                            println("   SLOT: ${it.localDateTimeFrom.dateFormat()} -> ${it.localDateTimeTo.dateFormat()}")
                        }
                        println()
                    }
                }
                "confirm"->{
                    Endpoints.USER.confirmConsult(args[1].toLong(), "ГИН", 1685197521000)
                        .apply { println(this) }
                }
                "zlist"->{
                    val list = Endpoints.USER.consultList(args[1].toLong(), true, "ГИН", 0) as List<SlotID>
                    list.forEach {
                        println("- ${it.from} ${it.question}")
                    }
                }
                "setrole"->{
                    val user = Server.cache.getCachedOrLoad(args[1].toLong())
                    Endpoints.USER.setRole(user!!.dbId, args[2]).apply { println(this) }
                }
                "clear"->{
                    Endpoints.USER.clearConsults(args[1].toLong()).apply { println(this) }
                }
                "list"->{
                    println("RES: ${Endpoints.USER.consultBook(args[1].toLong(), "ГИН", 0, 0, 1685197521000, "Вниманине Вапрос")}")
                    println("CREATED!")
                }
                "not"->{
                    //AppID = 8ced7149-ca5f-471c-b828-23677f82feb7
                    OnesignalUtils.send(args.drop(2).joinToString { it.replace("\\n", "\n") }, args[1])
                }
                "test"->{
                    val c = System.currentTimeMillis()
                    val res = TestNative.similarityTest(args.joinToString(" ", "", ""))
                    println("Result: ${res} (${System.currentTimeMillis()-c}ms)")
                }
                "parse"->{
                    ParseSite().parseUser()
                }
                "loadsave"->{
                    loadJson()
                    SystemDB.loadFromJson(DATASET_SCHEDULES, DATASET)
                    SystemDB.write()
                    println("DONE!")
                }
                "a"->{
                    while(true){
                        print(">")
                        val next = readln()
                        if(next == "quit")break
                        val ret = testChat.onMessage(Server.cache.getCachedOrLoad(args[1].toLong())!!, next)
                        println("")
                        ret.print()
                        //println("[ChatBot] ${ret.toStr()}")
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}
