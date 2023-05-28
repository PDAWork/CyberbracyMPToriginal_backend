package ru.bav.server.db.req

import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Punish : MapSerializable { //Санкция
    var right:String = "" //указание на НПА
    var items:MutableList<PunishType> = mutableListOf()

    override fun dataDeserialize(map: DataMap) {
        right = map.readString("ri") ?: ""
        map.readMapList("itT", items){PunishType() }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("ri", right)
        map.writeMapList("itT", items)
    }

    class PunishType : MapSerializable{
        var type:String = ""
        var items:MutableList<PunishItem> = mutableListOf()
        override fun dataDeserialize(map: DataMap) {
            type = map.readString("t") ?: ""
            map.readMapList("i", items){ PunishItem() }
        }

        override fun dataSerialize(map: DataMap) {
            map.writeString("t", type)
            map.writeMapList("i", items)
        }
    }

    class PunishItem : MapSerializable {
        var header:String = ""
        var amount:String = ""
        var vidNormy:String = ""

        override fun dataDeserialize(map: DataMap) {
            header = map.readString("h") ?: ""
            amount = map.readString("a") ?: ""
            vidNormy = map.readString("v") ?: ""
        }

        override fun dataSerialize(map: DataMap) {
            map.writeString("h", header)
            map.writeString("a", amount)
            map.writeString("v", vidNormy)
        }
    }
}
