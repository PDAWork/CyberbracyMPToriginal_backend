package ru.bav.server.schemas

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.io.File

@Serializable
class Schedules {

    val organizations = mutableListOf<ScheduleOrg>()

    @Serializable
    class ScheduleOrg {

        var name = ""
        var lowName = ""
        val descs = mutableListOf<String>()
        val months = mutableListOf<Month>()

        /*override fun dataDeserialize(map: DataMap) {
            name = map.readString("name") ?: ""
            lowName = map.readString("lName") ?: ""
            map.readMapList("months", months){ Month() }
        }

        override fun dataSerialize(map: DataMap) {
            map.writeString("name", name)
            map.writeString("lName", lowName)
            map.writeList("descs", descs)
            map.writeMapList("months", months)
        }*/

        @kotlin.jvm.Transient
        @Transient
        lateinit var organ: ObRequires.ObOrgan

        @Serializable
        class Month {

            var name: String = ""

            @Transient
            var row: Int = 0

            @Transient
            var stride: Int = 0

            val days: MutableList<MPair> = mutableListOf()

            @Serializable
            class MPair {
                var timestamp: Long = 0L

                var range: String = ""

                constructor(timestamp: Long, range: String) {
                    this.timestamp = timestamp
                    this.range = range
                }

                constructor()

                /*override fun dataDeserialize(map: DataMap) {
                    timestamp = map.readLong("timestamp") ?: 0L
                    range = map.readString("range") ?: ""
                }
                override fun dataSerialize(map: DataMap) {
                    map.writeLong("timestamp", timestamp)
                    map.writeString("range", range)
                }*/
            }

            /*override fun dataDeserialize(map: DataMap) {
                map.readMapList("days", days){ MPair() }
                name = map.readString("name") ?: ""
            }
            override fun dataSerialize(map: DataMap) {
                map.writeMapList("days", days)
                map.writeString("name", name)
            }*/
        }

    }
}