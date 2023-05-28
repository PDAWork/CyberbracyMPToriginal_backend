package ru.bav.server.db.schedule

import ru.bav.server.db.SystemDB
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient

class SlotID : MapSerializable {
    // KNO
    var orgLowName: String = ""
    var idControl: Int = 0
    var idRequire: Int = 0
    var question: String = ""

    var from: Long = 0L

    @Transient
    fun getDaySlot(): DaySlot? = SystemDB.byLowName(orgLowName)?.schedule?.getSlot(from)

    override fun dataDeserialize(map: DataMap) {
        orgLowName = map.readString("org") ?: ""
        from = map.readLong("strt") ?: 0L
        idControl = map.readInt("idCtrl") ?: 0
        idRequire = map.readInt("idReq") ?: 0
        question = map.readString("qu") ?: ""
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("org", orgLowName)
        map.writeInt("idCtrl", idControl)
        map.writeInt("idReq", idRequire)
        map.writeLong("strt", from)
        map.writeString("qu", question)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SlotID

        if (orgLowName != other.orgLowName) return false
        if (from != other.from) return false

        return true
    }

    override fun hashCode(): Int {
        var result = orgLowName.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }

}
