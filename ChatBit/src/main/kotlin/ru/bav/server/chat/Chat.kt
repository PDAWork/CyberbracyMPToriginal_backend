package ru.bav.server.chat

import ru.bav.server.Server
import ru.bav.server.chat.states.State
import ru.bav.server.chat.states.States
import ru.bav.server.keywords.Message
import ru.bav.server.keywords.Tokens
import ru.bav.server.moscowMillis
import ru.bav.server.subListOrEmpty
import ru.bav.server.user.User
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable

class Chat : MapSerializable {

    var statesVersion = 0

    private val states = mutableListOf<State<*>>()
    val mapStates: MutableMap<String, State<*>> = mutableMapOf()
    val messages: MutableList<Message> = mutableListOf()

    fun hasState(id: String): Boolean = mapStates.containsKey(id)
    fun getStateRaw(id: String): State<*>? = mapStates[id]

    fun forget(predicate: (State<*>) -> Boolean) {
        states.toList().forEach {
            if (predicate(it)) {
                states -= it
                mapStates.remove(it.id)
                //println("REMOVED: ${it.id}")
            }
        }
    }

    fun forgetAnyMemory() = forget { true }
    fun forgetWeakMemory() = forget { !it.mustMemorize }

    fun setState(state: State<*>, memory: Boolean = false) {
        states += state
        state.mustMemorize = memory
        mapStates[state.id] = state
    }

    inline fun <reified T> getState(id: String): T? = getStateRaw(id) as T

    fun addMessage(msg: Message) {
        messages.add(msg)
    }

    private fun mapping() {
        mapStates.clear()
        states.forEach {
            mapStates[it.id] = it
        }
    }

    //0 = last 0->10, 1 = last 10->20
    fun getPage(page: Int, pageSize: Int = 20): List<Message> {
        val offset = messages.size - (page + 1) * pageSize
        return messages.subListOrEmpty(offset, offset + pageSize).toList()
    }

    fun onMessage(user: User, msg: String): Message {
        val millis = moscowMillis()
        addMessage(Message(msg).apply {
            isBot = false
            timestamp = millis
        })
        val msgOut = onChatMessage(user, msg)
        addMessage(msgOut.apply {
            timestamp = millis
        })
        return msgOut
    }

    private fun onChatMessage(user: User, msg: String): Message {
        val rel = Server.map.relevantKey(Tokens.str(msg))
        Server.map.currentBranch?.let { br ->
            if (br.setNullBeforeDirectInput) {
                Server.map.currentBranch = null
            }
            val branched = br.directInput(user, Tokens.str(msg))
            if (branched != null) {
                return branched
            }
        }
        val toSay = Server.map.map[rel] ?: return Message("Я вас не понял!")
        return toSay.controlledInput(user, rel)
    }

    override fun dataDeserialize(map: DataMap) {
        statesVersion = map.readInt("stVer") ?: 0
        map.readMapList("messages", messages) { Message() }
        map.readObj("states") { inn ->
            repeat(inn.readVarInt()) {
                val type = inn.readByte()
                val newInst = States.map[type] ?: throw IllegalArgumentException("Not found type ${type} state!")
                val inst = newInst()
                inst.deserialize(inn)
                states += inst
            }
        }
        forgetWeakMemory()
        mapping()
    }

    override fun dataSerialize(map: DataMap) {
        map.writeInt("stVer", statesVersion)
        map.writeObj("states") { out ->
            out.writeVarInt(states.size)
            states.forEach {
                out.writeByte(it.type.toInt())
                it.serialize(out)
            }
        }
        map.writeMapList("messages", messages)
    }
}