package ru.bav.server.chat.states

object States {
    val map:MutableMap<Byte, ()-> State<*>> = mutableMapOf()

    init {
        map[1] = { BoolState() }
        map[2] = { StringState() }
    }
}
