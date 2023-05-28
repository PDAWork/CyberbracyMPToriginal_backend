package ru.bav.server.db.req

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class NPA : MapSerializable { //Нормативно правовой акт
    var desc:String = ""
    var from:String? = null
    var to:String? = null

    override fun dataDeserialize(map: DataMap) {
        desc = map.readString("desc") ?: ""
        from = map.readString("from")
        to = map.readString("to")
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("desc", desc)
        if(from != null){
            map.writeString("from", from!!)
        }
        if(to != null){
            map.writeString("to", to!!)
        }
    }
}
