package ru.bav.server.db.req

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient

class RequireList(@get:Transient val controlType: ControlType) : MapSerializable { //1.
    var section:String = ""
    var header:String = ""
    var requires:MutableList<Require> = mutableListOf()

    override fun dataDeserialize(map: DataMap) {
        section = map.readString("s") ?: ""
        header = map.readString("h") ?: ""
        map.readMapList("r", requires) { Require(controlType) }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("s", section)
        map.writeString("h", header)
        map.writeMapList("r", requires)
    }
}