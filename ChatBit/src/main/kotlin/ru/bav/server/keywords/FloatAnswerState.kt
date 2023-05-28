package ru.bav.server.keywords

import ru.bav.server.user.User

class FloatAnswer(val question:Tokens, val forward:Branch)
class FloatAnswerState(val head:Message,val branches:MutableList<FloatAnswer>, val keywordsMap: KeywordsMap) : StateBranch<String>("вф"), FloatAnswerScope {

    override val setNullBeforeDirectInput: Boolean
        get() = false

    override fun directInput(user: User, entered: Tokens): Message? {
        val activations = branches.map {
            it to it.question.compareActivation(entered)
        }.sortedByDescending { it.second }
        println("ANSWERS: ${activations}")
        val (answ, weight) = activations.firstOrNull() ?: return null
        println("FORWARD: ${answ.forward}")
        if(weight < 1f)return null
        return answ.forward.noControlledInput(user)
    }

    override fun controlledInput(user: User, entered: Tokens): Message {
        if(!hasState(user)){
            return noControlledInput(user)
        }
        return Message("Ne znayu")
    }

    override fun noControlledInput(user: User): Message {
        if(!hasState(user)){
            getControl()
            return head
        }
        return Message("Answered already!")
    }

    override fun answerIf(answerFromUser: String, forward: BranchScope.() -> Unit) {
        val forwardObj = ChatSchemaBuilder(keywordsMap)
        forward(forwardObj)
        branches += FloatAnswer(Tokens.str(answerFromUser), forwardObj.root!!)
    }
}