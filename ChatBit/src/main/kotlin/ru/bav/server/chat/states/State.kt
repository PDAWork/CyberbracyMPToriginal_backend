package ru.bav.server.chat.states

import ru.beenaxis.uio.io.encoding.ByteSerializable
import ru.beenaxis.uio.io.encoding.io.DataIn
import ru.beenaxis.uio.io.encoding.io.DataOut

open class State<T> : ByteSerializable {
    var state:T? = null
    var id:String = ""
    var mustMemorize = false
    open val type:Byte = 0

    constructor(def:T){
        this.state = def
    }
    constructor()

    open fun serialState(out: DataOut){

    }

    open fun deserialState(input: DataIn){

    }

    final override fun deserialize(input: DataIn) {
        id = input.readString()
        mustMemorize = input.readBoolean()
        if(input.readByte() == 1.toByte()){
            deserialState(input)
        }
    }

    final override fun serialize(out: DataOut) {
        out.writeString(id)
        out.writeBoolean(mustMemorize)
        if(state == null){
            out.writeByte(0)
        }else{
            out.writeByte(1)
            serialState(out)
        }
    }

}