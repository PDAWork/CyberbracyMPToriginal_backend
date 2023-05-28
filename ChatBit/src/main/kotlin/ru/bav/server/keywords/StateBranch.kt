package ru.bav.server.keywords

import ru.bav.server.Server
import ru.bav.server.chat.states.BoolState
import ru.bav.server.chat.states.State
import ru.bav.server.user.User

open class SwitchStateBranch(id:String, val head:Message, val yes:Branch, val no:Branch) : StateBranch<Boolean?>(id){

    override fun directInput(user: User, entered: Tokens): Message? {
        val cur = Tokens.str("Нет нельзя отказ потом отрицать").compareActivation(entered)
        val state = cur < Tokens.str("Да ага есть можно нужно конечно хочу").compareActivation(entered)
        setState(user, BoolState(state), true)
        var toRet:Message? = null
        val old = state
        toRet = if(old)
            yes.noControlledInput(user)
        else
            no.noControlledInput(user)
        //setState(user, BoolState(null))
        return toRet
    }

    override fun controlledInput(user: User, entered: Tokens): Message {
        if(!hasState(user)){
            return noControlledInput(user)
        }
        return Message("Я уже знаю что на вопрос ${head.toStr()} вы выбрали ${if(getState<BoolState>(user)!!.state!!) "Да" else "Нет"}")
        //throw IllegalArgumentException("IMPOSSIBLE STATE!")
    }

    override fun noControlledInput(user: User): Message {
        if(!hasState(user)){
            getControl()
            return head
        }
        return Message("Answered already!")
    }

    override val setNullBeforeDirectInput: Boolean
        get() = true
}
open class SingleForwardAnswer(id:String, val first:Message, val already:Message) : StateBranch<Boolean?>(id){

    override fun controlledInput(user: User, entered: Tokens): Message {
        return noControlledInput(user)
    }

    override fun noControlledInput(user: User): Message {
        if(!hasState(user)){
            setState(user, BoolState().apply { state = true })
            return first
        }
        return if(getState<BoolState>(user)!!.state!!) already else first
    }

    override val setNullBeforeDirectInput: Boolean
        get() = true

    override fun directInput(user: User, entered: Tokens): Message {
        return controlledInput(user, entered)
    }
}
// With Remember
open class StateBranch<T>(val idMemory:String) : Branch {
    /*var state:T? = default
        protected set*/

    fun setState(user: User, state: State<*>, memorize:Boolean = false) = user.chat.setState(state.apply { id = idMemory }, memorize)
    fun hasState(user:User) : Boolean = user.chat.hasState(idMemory)
    inline fun <reified A> getState(user: User) : A? = user.chat.getState(idMemory)

    fun getControl(){
        Server.map.currentBranch = this
        println("SET CONTROL: ${this.javaClass.simpleName}")
    }

    override val setNullBeforeDirectInput: Boolean
        get() = true

    override fun directInput(user: User, entered: Tokens): Message? {
        user.chat.getState<BoolState>("test")
        return controlledInput(user, entered)
    }

    override fun controlledInput(user: User, entered: Tokens) : Message {
        return Message("Once controlled! ${javaClass.simpleName}")
    }

    override fun noControlledInput(user: User): Message? {
        return Message("Unknown states BRANCH!")
    }
}