package ru.bav.server.keywords

import ru.bav.server.moscowMillis
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Message : MapSerializable, MessageScope {
    var isBot:Boolean = true
    var text:MutableList<String> = mutableListOf()
        private set
    var timestamp:Long = moscowMillis()
    var isButton = false
    var hasTable = false

    constructor(vararg text:String){
        this.text += text
    }

    constructor()

    fun toStr() : String = text.joinToString("\n", "", "")

    fun print(){
        text.forEach {
            println("[ChatBot/TEXT] ${it}")
        }
    }

    override fun dataDeserialize(map: DataMap) {
        text = map.readList<String>("text")?.toMutableList() ?: mutableListOf()
        isBot = map.readLogic("b") ?: true
        timestamp = map.readLong("time") ?: 0L
        isButton = map.readLogic("isbutton") ?: false
        hasTable = map.readLogic("hasTable") ?: false
    }

    override fun dataSerialize(map: DataMap) {
        map.writeList("text", text)
        map.writeLogic("b", isBot)
        map.writeLong("time", timestamp)
        map.writeLogic("isbutton", isButton)
        map.writeLogic("hasTable", hasTable)
    }

    override fun table(scope: TableScope.() -> Unit) {
        val table = SimpleTable()
        table.scope()
        text += table.head + "\n"
        text += table.cHead + "\n"
        table.rows.forEach {
            text += it + "\n"
        }
    }

    override fun line(line: String) {
        text += line + "\n\n"
    }

    override fun consultButton() {
        this.isButton = true
    }

    override fun hasTable() {
        this.hasTable = true
    }
}