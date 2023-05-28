package ru.bav.server.db.schedule

import ru.bav.server.db.Organization
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Month(@get:java.beans.Transient val org: Organization) : MapSerializable {

    //Июнь/3143413445354/18-00:19:00
    var name: String = ""
    val days: MutableList<DaySlot> = mutableListOf()

    fun getDays() : Collection<DaySlots> {
        val map = mutableMapOf<Long, DaySlots>()
        days.forEach {
            if(map.containsKey(it.dayTimestamp)){
                map[it.dayTimestamp]!!.slots += it
            }else{
                map[it.dayTimestamp] = DaySlots(it.dayTimestamp, mutableListOf(it))
            }
        }
        return map.values
    }

    class DaySlots(val timestamp:Long, val slots:MutableList<DaySlot>){
        fun isDayBusy() : Boolean {
            slots.forEach {
                if(!it.isBusy()){
                    return false
                }
            }
            return true
        }
    }

    override fun dataDeserialize(map: DataMap) {
        map.readMapList("d", days) { DaySlot(this) }
        name = map.readString("n") ?: ""
    }

    override fun dataSerialize(map: DataMap) {
        map.writeMapList("d", days)
        map.writeString("n", name)
    }
}