package ru.bav.server.keywords

import ru.bav.server.Server

class ChatSchemaBuilder(val keywordsMap: KeywordsMap) : BranchScope {

    var root: Branch? = null

    fun asQuestion(question: String) {
        keywordsMap.branch(question, root!!)
    }

    override fun approval(
        head: MessageScope.() -> Unit,
        yes: BranchScope.() -> Unit,
        no: BranchScope.() -> Unit,
    ) {
        val yesObj = ChatSchemaBuilder(keywordsMap)
        val noObj = ChatSchemaBuilder(keywordsMap)
        yes(yesObj)
        no(noObj)
        root = SwitchStateBranch(
            Server.identity(), buildMsg(head), yesObj.root!!, noObj.root!!
        )
    }

    override fun singleForwardAnswer(head: MessageScope.() -> Unit, forwarded: MessageScope.() -> Unit) {
        root = SingleForwardAnswer(
            Server.identity(), buildMsg(head), buildMsg(forwarded)
        )
    }

    override fun answer(scope: MessageScope.() -> Unit) {
        root = Answer(buildMsg(scope))
    }

    override fun floatAnswer(head: MessageScope.() -> Unit, fas: FloatAnswerScope.() -> Unit) {
        val fasObj = FloatAnswerState(buildMsg(head), mutableListOf(), keywordsMap)
        fas.invoke(fasObj)
        root = fasObj
    }


    fun buildMsg(scope: MessageScope.() -> Unit): Message {
        val msg = Message()
        scope(msg)
        return msg
    }
}

interface FloatAnswerScope {
    fun answerIf(answerFromUser:String, forward: BranchScope.() -> Unit)
}

interface BranchScope {

    fun singleForwardAnswer(head: MessageScope.() -> Unit, forwarded: MessageScope.() -> Unit)
    fun answer(scope: MessageScope.() -> Unit)
    fun floatAnswer(head: MessageScope.() -> Unit, fas:FloatAnswerScope.()->Unit)
    fun approval(
        head: MessageScope.() -> Unit,
        yes: BranchScope.() -> Unit,
        no: BranchScope.() -> Unit,
    )
}
class SimpleTable : TableScope {
    var head = ""
    var cols = 0
    var cHead = ""
    var rows = mutableListOf<String>()
    override fun head(vararg headers: String) {
        head = headers.joinToString("|", "|", "|")
        cols = head.count { it == '|' } - 1
        val clist = mutableListOf<String>()
        repeat(cols){
            clist += "-"
        }
        cHead = clist.joinToString("|", "|", "|")
    }

    override fun row(vararg cols: String) {
        if(this.cols != cols.size)
            throw IllegalArgumentException("${this.cols} != ${cols.size} Columns!")
        rows += cols.joinToString("|", "|", "|")
    }

}

interface TableScope {
    fun head(vararg headers:String)
    fun row(vararg cols:String)
}

interface MessageScope {
    fun table(scope: TableScope.()->Unit)
    fun line(line: String)
    fun consultButton()
    fun hasTable()
}