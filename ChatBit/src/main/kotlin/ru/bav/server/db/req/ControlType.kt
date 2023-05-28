package ru.bav.server.db.req

import ru.bav.server.db.Organization
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient

class ControlType(@get:Transient val kno:Organization) : MapSerializable { //Вид контроля

    var fileName:String = ""
    var name:String = ""
    var requiresSections:MutableList<RequireList> = mutableListOf()
    val allRequires:List<Require>
        get() {
            val list = mutableListOf<Require>()
            requiresSections.forEach {
                list += it.requires
            }
            return list
         }

    fun fullName() {

    }

    override fun dataDeserialize(map: DataMap) {
        fileName = map.readString("fN") ?: ""
        name = map.readString("name") ?: ""
        map.readMapList("rS", requiresSections) { RequireList(this) }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("fN", fileName)
        map.writeString("name", name)
        map.writeMapList("rS", requiresSections)
    }

}