package ru.bav.server.chat.states

import ru.beenaxis.uio.io.encoding.io.DataIn
import ru.beenaxis.uio.io.encoding.io.DataOut

class BoolState : State<Boolean?>{

    override val type: Byte
        get() = 1

    constructor(de:Boolean?){
        this.state = de
    }

    constructor()


    override fun serialState(out: DataOut) {
        out.writeBoolean(state!!)
    }

    override fun deserialState(input: DataIn) {
        state = input.readBoolean()
    }
}