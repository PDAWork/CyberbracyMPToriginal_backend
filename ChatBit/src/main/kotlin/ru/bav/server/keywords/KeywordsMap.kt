package ru.bav.server.keywords

import ObRequires
import ru.bav.server.Server

class KeywordsMap {
    //Карта Слов -> Активность
    // - Активность Например вывод ответа
    // - Вывод вопроса и прослушивание другой карты
    val map: MutableMap<Tokens, Branch> = mutableMapOf()
    var currentBranch: Branch? = null

    fun branch(quest: String, branch: Branch) {
        map[Tokens.str(quest)] = branch
    }

    fun signQuestion(quest: String, header:String, yes: Branch, no: Branch) {
        branch(quest, SwitchStateBranch(Server.identity(), Message(header), yes, no))
    }

    fun signEndpoint(quest: String, answer: Message) {
        branch(quest, Answer(answer))
    }

    /**
     * TODO Загрузка схемы
     */
    fun load(obj: ObRequires) {
        println("Загрузка карты сообщений...")
        obj.organs.forEach {
            signEndpoint(
                it.name, Message(
                    "Вы выбрали орг: ${it.name}",
                    "У него есть вот что:",
                    "${it.sects.joinToString { it.name }}",
                )
            )
            it.sects.forEach {
                map[Tokens.str("Информация о секциях")]
                signEndpoint(
                    it.name, Message(
                        "Секция ${it.name}: ",
                        "${it.files.joinToString { it.fileName }}",
                    )
                )
                it.files.forEach {
                    val nm = it.nm
                    signEndpoint(
                        "Требования", Message(
                            "Да, вы можете узнать требования",
                            "Пишите раздел Например Лекарственные",
                            "средства!",
                        )
                    )
                    nm.requirements.forEach { req ->
                        signEndpoint(
                            "Требование ${req.name}", Message(
                                "Требование ${req.name}",
                                "Accepts: ${req.accepts.size}",
                                "Punishes: ${req.punishments.size}",
                            )
                        )
                        req.punishments.forEach {
                            signEndpoint(
                                "Наказания для ${req.name}", Message(
                                    "Ну вот они их ${it.items.size}шт",
                                    *it.items.map { "${it.header};${it.amount};${it.type};${it.vidNormy}" }
                                        .toTypedArray()
                                )
                            )
                        }
                    }
                }
            }
        }
        println("Карта сообщений загружена! ${map.size}x")
    }

    fun relevantKey(message: Tokens): Tokens {
        var current: Tokens? = null
        var costCur: Float = 0f
        map.forEach { (t, u) ->
            val act = t.compareActivation(message)
            if (act == 0f) return@forEach
            if (current == null) {
                current = t
                costCur = act
                return@forEach
            } else {
                if (act > costCur) {
                    current = t
                    costCur = act
                }
            }
        }
        //println("COST: ${costCur}")
        //costs.toSortedMap(Comparator { o1, o2 -> o2.compareTo(o1) })
        return current
            ?: Tokens.str("Извините, я вас не понял :(")//costs.entries.firstOrNull()?.value ?: Tokens.str("Не понял чё тебе надо :(")
    }
}
