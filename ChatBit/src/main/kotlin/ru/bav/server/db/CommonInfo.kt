package ru.bav.server.db

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class CommonInfo : MapSerializable {
    var caption:String = ""
    var items:MutableList<String> = mutableListOf()

    override fun dataDeserialize(map: DataMap) {
        caption = map.readString("c") ?: ""
        items = map.readList<String>("l")?.toMutableList() ?: mutableListOf()
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("c", caption)
        map.writeList("l", items)
    }
}
