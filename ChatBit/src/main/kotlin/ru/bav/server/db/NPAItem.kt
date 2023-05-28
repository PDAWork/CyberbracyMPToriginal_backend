package ru.bav.server.db

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class NPAItem : MapSerializable{
    var text:String = ""
    var date:String = ""
    var fileInfo:String = ""
    override fun dataDeserialize(map: DataMap) {
        text = map.readString("t") ?: ""
        date = map.readString("d") ?: ""
        fileInfo = map.readString("fi") ?: ""
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("t", text)
        map.writeString("d", date)
        map.writeString("fi", fileInfo)
    }
}