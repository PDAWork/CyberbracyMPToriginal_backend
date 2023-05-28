package ru.bav.server.chat.states

import ru.beenaxis.uio.io.encoding.io.DataIn
import ru.beenaxis.uio.io.encoding.io.DataOut

class StringState : State<String?> {
    override val type: Byte
        get() = 2

    constructor(de:String?){
        this.state = de
    }

    constructor()


    override fun serialState(out: DataOut) {
        out.writeString(state!!)
    }

    override fun deserialState(input: DataIn) {
        state = input.readString()
    }

}