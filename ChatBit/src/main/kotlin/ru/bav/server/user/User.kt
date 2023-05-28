package ru.bav.server.user

import ru.bav.server.Server
import ru.bav.server.api.OnesignalUtils
import ru.bav.server.chat.Chat
import ru.bav.server.db.schedule.DaySlot
import ru.bav.server.db.schedule.Month
import ru.bav.server.db.schedule.SlotID
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

/*
    Controls by I/O + Cache
 */
class User : MapSerializable {

    var dbId:Long = 0L
    var firstName:String = ""
    var secondName:String = ""
    var thirdName:String = ""
    var role:Role = Role.USER
    var email:String = ""

    val fullName:String
        get() = "$firstName $secondName $thirdName"

    //ONLY READ!
    val slots:MutableList<SlotID> = mutableListOf() //Записи на консультирование

    fun sendNotify(text:String){
        OnesignalUtils.send(text, email)
    }

    fun addSlot(slot:SlotID){
        slots += slot
    }

    fun deleteSlots(slot: DaySlot){
        slots.removeAll { it.from == slot.localDateTimeFrom }
    }

    fun isBusyAt(time:Long) : Boolean {
        slots.forEach sl@{
            val cc = it.getDaySlot() ?: return@sl
            if(time in cc.localDateTimeFrom .. cc.localDateTimeTo){
                return true
            }
        }
        return false
    }

    var chat:Chat = Chat()

    override fun dataDeserialize(map: DataMap) {
        dbId = map.readLong("dbId") ?: 0
        firstName = map.readString("fNm") ?: ""
        secondName = map.readString("sNm") ?: ""
        thirdName = map.readString("tNm") ?: ""
        email = map.readString("email") ?: ""
        map.readMap("chat", chat)
        map.readMapList("slots", slots){SlotID()}
        role = map.readEnum<Role>("role") ?: Role.USER
        //forgetIfInvalidateMemory()
    }

    override fun dataSerialize(map: DataMap) {
        map.writeLong("dbId", dbId)
        map.writeString("fNm", firstName)
        map.writeString("sNm", secondName)
        map.writeString("tNm", thirdName)
        map.writeString("email", email)
        map.writeMap("chat", chat)
        map.writeMapList("slots", slots)
        map.writeEnum("role", role)
    }

    fun forgetIfInvalidateMemory() {
        if (chat.statesVersion != Server.memoryVersion) {
            chat.statesVersion = Server.memoryVersion
            chat.forgetAnyMemory()
        }
    }

    fun save() {
        Server.database.write(this)
    }
}