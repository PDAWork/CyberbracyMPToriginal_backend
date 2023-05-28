package ru.bav.server.db.schedule

import ru.bav.server.db.Organization
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Schedule(@get:java.beans.Transient val org:Organization) : MapSerializable {

    var subOrganizations = mutableListOf<String>() //Какие то подогранизации, я не понял :(
        private set
    val availableMonths:MutableList<Month> = mutableListOf()

    val cachedSlots:MutableMap<Long, DaySlot> = Collections.synchronizedMap(mutableMapOf())
    @get:Transient
    val scheduler = Executors.newScheduledThreadPool(1)

    fun initUpdateScheduler(){
        scheduler.scheduleWithFixedDelay(Runnable {
            try{
                update()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }, 1L, 1L, TimeUnit.SECONDS)
    }

    fun update(){
        synchronized(cachedSlots.values){
            cachedSlots.values.forEach {
                it.update()
            }
        }
    }

    fun cacheSlots(){
        availableMonths.forEach {
            it.days.forEach { slot->
                cachedSlots[slot.localDateTimeFrom] = slot
            }
        }
    }

    fun getSlot(from:Long) : DaySlot? {
        return cachedSlots[from]
    }

    override fun dataDeserialize(map: DataMap) {
        subOrganizations = map.readList<String>("sO")?.toMutableList() ?: mutableListOf()
        map.readMapList("m", availableMonths){ Month(org) }
        availableMonths.firstOrNull()?.let {
            it.days += DaySlot(it).apply {
                this.range = "17:25-19:00"
                this.dayTimestamp = 1685208321000
            }
        }
        cacheSlots()
    }

    override fun dataSerialize(map: DataMap) {
        map.writeList("sO", subOrganizations)
        map.writeMapList("m", availableMonths)
    }
}