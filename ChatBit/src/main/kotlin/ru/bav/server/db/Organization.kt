package ru.bav.server.db

import ru.bav.server.db.req.ControlTypes
import ru.bav.server.db.schedule.Schedule
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Organization : MapSerializable { //Контрольно надзорный орган

    var lowName: String = "" //МЖИ
    var name: String = "" // Длинное название
    val origName: String
        get() {
            if (name.contains("(").not()) return name
            val nm = name.split("(")[0]
            return nm.substring(0, nm.length - 1)
        }

    val totalRequires: Int
        get() = controlTypes.controlTypes.sumOf { it.requiresSections.sumOf { it.requires.size } }

    val totalNPAs: Int
        get() = npas.size

    var headFio: String = ""
    var positionHead: String = ""
    var headImageSrc: String = ""
    var activity: String = ""
    val commonInfo = mutableListOf<CommonInfo>()
    val npas = mutableListOf<NPAItem>()

    var schedule = Schedule(this) //Расписание
    var controlTypes: ControlTypes = ControlTypes(this)

    override fun dataDeserialize(map: DataMap) {
        lowName = map.readString("lowName") ?: ""
        name = map.readString("name") ?: ""
        headFio = map.readString("headFio") ?: ""
        positionHead = map.readString("positionHead") ?: ""
        activity = map.readString("activity") ?: ""
        headImageSrc = map.readString("headImageSrc") ?: ""
        map.readMap("s", schedule)
        map.readMap("c", controlTypes)
        map.readMapList("cinfo", commonInfo) { CommonInfo() }
        map.readMapList("npas", npas) { NPAItem() }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("name", name)
        map.writeString("lowName", lowName)
        map.writeString("headFio", headFio)
        map.writeString("positionHead", positionHead)
        map.writeString("activity", activity)
        map.writeString("headImageSrc", headImageSrc)
        map.writeMap("s", schedule)
        map.writeMap("c", controlTypes)
        map.writeMapList("cinfo", commonInfo)
        map.writeMapList("npas", npas)
    }
}