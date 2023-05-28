package ru.bav.server.db.req

import ru.bav.server.db.Organization
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient

class ControlTypes(@get:Transient val org:Organization) : MapSerializable { //Виды контроля

    var name:String = ""
    var controlTypes:MutableList<ControlType> = mutableListOf()

    override fun dataDeserialize(map: DataMap) {
        name = map.readString("n") ?: ""
        map.readMapList("cT", controlTypes) { ControlType(org) }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("n", name)
        map.writeMapList("cT", controlTypes)
    }

}