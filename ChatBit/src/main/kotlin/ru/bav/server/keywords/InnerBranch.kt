package ru.bav.server.keywords

import ru.bav.server.user.User

open class InnerBranch() : Branch {
    var keys = KeywordsMap()
    override val setNullBeforeDirectInput: Boolean
        get() = true

    override fun directInput(user: User, entered: Tokens): Message {
        return controlledInput(user, entered)
    }

    override fun controlledInput(user: User, entered: Tokens): Message {
        val tks = keys.relevantKey(entered)
        return keys.map[tks]?.controlledInput(user, tks) ?: Message("Не знаю что ответить в этой ветке :(")
    }

    override fun noControlledInput(user: User): Message? {
        return null
    }
}