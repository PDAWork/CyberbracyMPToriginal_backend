package ru.bav.server.db.req

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Check : MapSerializable { //Проверка
    var type:String = ""
    var documentAccepts:String = ""
    var orgWithInfo:String? = null
    var ability:String? = null

    override fun dataDeserialize(map: DataMap) {
        type = map.readString("t") ?: ""
        documentAccepts = map.readString("do") ?: ""
        orgWithInfo = map.readString("o")
        ability = map.readString("a")
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("t", type)
        map.writeString("do", documentAccepts)
        orgWithInfo?.let { map.writeString("o", it) }
        ability?.let { map.writeString("a", it) }
    }
}