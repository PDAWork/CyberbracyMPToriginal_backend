package ru.bav.server.keywords

import ru.bav.server.user.User

interface Branch {
    val setNullBeforeDirectInput:Boolean
    fun directInput(user: User, entered: Tokens) : Message?
    fun controlledInput(user: User, entered: Tokens) : Message
    fun noControlledInput(user: User) : Message?
}