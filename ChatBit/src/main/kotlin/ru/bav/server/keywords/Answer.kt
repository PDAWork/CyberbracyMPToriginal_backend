package ru.bav.server.keywords

import ru.bav.server.user.User

/*interface BranchHandler {
    var currentBranch:Branch?
}*/
class Answer(val msg: Message) : Branch {
    override val setNullBeforeDirectInput: Boolean
        get() = true

    override fun directInput(user: User, entered: Tokens): Message {
        return controlledInput(user, entered)
    }

    override fun controlledInput(user: User, entered: Tokens): Message {
        return noControlledInput(user)
    }

    override fun noControlledInput(user: User): Message {
        return msg
    }
}