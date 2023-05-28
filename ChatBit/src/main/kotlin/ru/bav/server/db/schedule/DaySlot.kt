package ru.bav.server.db.schedule

import ru.bav.server.BASE_ZONE_ID
import ru.bav.server.Server
import ru.bav.server.dateFormat
import ru.bav.server.moscowMillis
import ru.bav.server.user.User
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient
import java.util.*

class DaySlot(@get:Transient val month: Month) : MapSerializable {

    fun makeID(idControl:Int, idRequire:Int, question: String = ""): SlotID = SlotID().apply {
        this.orgLowName = month.org.lowName
        this.from = localDateTimeFrom
        this.idControl = idControl
        this.idRequire = idRequire
        this.question = question
    }

    private val THIRTY_MIN = 1000 * 60 * 30
    private val DAY = 1000 * 60 * 60 * 24

    fun update() {
        if (!isBusy()) return
        when (status) {
            SlotStatus.RUNNING->{
                if(moscowMillis() > localDateTimeTo){
                    status = SlotStatus.CLOSED
                    println("${idToConsultUser} is closed!")
                    broadcast(
                        toConsult = "Консультация завершена",
                        consultant = "Консультация завершена"
                    )
                }
            }
            SlotStatus.NOTIFIED_DAY -> {
                val diff = localDateTimeFrom - moscowMillis()
                if (diff < THIRTY_MIN) {
                    status = SlotStatus.NOTIFIED
                    broadcast(
                        toConsult = "Через 30 минут начнётся консультация с ${consultantUser()?.fullName}",
                        consultant = "Через 30 минут начнётся консультация с ${toConsultUser()?.fullName}"
                    )
                    println("${idToConsultUser} is notified!")
                }
            }
            SlotStatus.NOTIFIED -> {
                if(moscowMillis() > localDateTimeFrom){
                    status = SlotStatus.RUNNING
                    broadcast(
                        toConsult = "Консультация началась! Нажмите чтобы зайти.",
                        consultant = "Консультация началась! Нажмите чтобы зайти."
                    )
                    println("${idToConsultUser} is running!")
                }
            }
            SlotStatus.BOOKED -> {
                val diff = localDateTimeFrom - moscowMillis()
                if (diff < DAY) {
                    status = SlotStatus.NOTIFIED_DAY
                    broadcast(
                        toConsult = "Через сутки начнётся консультация с ${consultantUser()?.fullName}",
                        consultant = "Через сутки начнётся консультация с ${toConsultUser()?.fullName}"
                    )
                    println("${idToConsultUser} is notified day!")
                }
            }

            else -> {}
        }
    }

    fun broadcast(consultant:String, toConsult:String){
        consultantUser()?.sendNotify(consultant)
        toConsultUser()?.sendNotify(toConsult)
    }

    @Transient
    fun consultantUser(): User? {
        idConsultantUser?.let { id ->
            return Server.cache.getCachedOrLoad(id)
        }
        return null
    }

    @Transient
    fun toConsultUser(): User? {
        idToConsultUser?.let { id ->
            return Server.cache.getCachedOrLoad(id)
        }
        return null
    }

    fun isBusy(): Boolean {
        return status != SlotStatus.AVAILABLE
    }

    fun clear(){
        toConsultUser()?.deleteSlots(this)
        consultantUser()?.deleteSlots(this)
        idToConsultUser = null
        idConsultantUser = null
        status = SlotStatus.AVAILABLE
    }

    fun confirm(){
        toConsultUser()?.sendNotify("Ваша запись подтверждена!")
        status = SlotStatus.BOOKED
    }

    fun makeBusy(toConsult: User, consultant: User, question:String, idControl: Int, idRequire: Int){
        val newId = makeID(idControl, idRequire, question)
        idToConsultUser = toConsult.dbId
        idConsultantUser = consultant.dbId
        toConsult.addSlot(newId)
        consultant.addSlot(newId)
        consultant.sendNotify("Запрос на запись консультации от ${toConsult.fullName}, по времени ${localDateTimeTo.dateFormat()}")

        status = SlotStatus.WAIT_CONFIRM
    }

    var dayTimestamp: Long = 0L
    var range: String = ""

    var status: SlotStatus = SlotStatus.AVAILABLE

    val localDateTimeFrom: Long
        get() = getLocalDate(range.split("-")[0]) ?: 0L

    val localDateTimeTo: Long
        get() = getLocalDate(range.split("-")[1]) ?: 0L

    fun isContainsIn(time: Long): Boolean = time in (localDateTimeFrom + 1) until localDateTimeTo

    fun getLocalDate(withTime: String): Long {
        val cl = Calendar.getInstance()
        cl.time = Date(dayTimestamp)
        cl.timeZone = TimeZone.getTimeZone(BASE_ZONE_ID)
        val f = withTime.split(":")
        val hour = f[0].toInt()
        val min = f[1].toInt()
        cl[Calendar.HOUR_OF_DAY] = hour
        cl[Calendar.MINUTE] = min
        return cl.timeInMillis
    }

    var idToConsultUser: Long? = null //Занят кем то
        private set


    var idConsultantUser: Long? = null

    override fun dataDeserialize(map: DataMap) {
        dayTimestamp = map.readLong("ts") ?: 0L
        range = map.readString("rg") ?: ""
        idToConsultUser = map.readLong("sel")
        idConsultantUser = map.readLong("cuser")
        status = map.readEnum<SlotStatus>("stts") ?: SlotStatus.AVAILABLE
    }

    override fun dataSerialize(map: DataMap) {
        idToConsultUser?.let {
            map.writeLong("sel", it)
        }
        idConsultantUser?.let {
            map.writeLong("cuser", it)
        }
        map.writeLong("ts", dayTimestamp)
        map.writeString("rg", range)
        map.writeEnum("stts", status)
    }

}