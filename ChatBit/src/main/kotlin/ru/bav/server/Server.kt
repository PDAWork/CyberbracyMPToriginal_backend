package ru.bav.server

import ObRequires
import ru.bav.entry.Main
import ru.bav.server.api.Endpoints
import ru.bav.server.db.SystemDB
import ru.bav.server.keywords.*
import ru.bav.server.schemas.Schedules
import ru.bav.server.user.UserCache
import ru.bav.server.user.UserDB
import java.io.File

lateinit var DATASET: ObRequires
lateinit var DATASET_SCHEDULES: Schedules

object Server {
    val map: KeywordsMap = KeywordsMap()
    val database = UserDB(File("${Main.getJarFolder()}/users"))
    val cache = UserCache(database)
    const val memoryVersion = 0
    private var id = 0

    fun identity(): String {
        val l = id
        id++
        return "_${l}"
    }

    fun asQuestion(question: String, scope: BranchScope.() -> Unit) {
        val bb = ChatSchemaBuilder(map)
        scope(bb)
        bb.asQuestion(question)
    }

    fun start() {
        println("Открытый контроль API")
        println("======================")
        CommandHandler.apply {
            setup()
            startThread()
        }
        println("Чтение БД...")
        SystemDB.read()
        SystemDB.orgs.forEach { it.schedule.initUpdateScheduler() }
        println("Загружено ${SystemDB.orgs.size} КНО")

        asQuestion("Записаться на консультацию") {
            answer {
                line("Да, вы можете записаться на консультацию.")
                line("<Нажимайте кнопку снизу.>")
                consultButton()
            }
        }
        asQuestion("Привет!") {
            singleForwardAnswer({ line("Приветствую вас! Задавайте мне вопросы.") }) {
                line("Нет необходимости приветствоваться вновь.")
            }
        }
        SystemDB.orgs.forEach {
            asQuestion("Полное название как называется ${it.name}") {
                answer {
                    line("${it.name.withFirstBigChar()} имеет короткое название")
                    line(it.lowName + ".")
                }
            }
            asQuestion("Сколько требований ${it.lowName} ${it.name}") {
                answer {
                    line("${it.name.withFirstBigChar()} включает в себя ${it.totalRequires} требований.")
                }
            }
            asQuestion("Кто управляет? ${it.lowName} ${it.name}") {
                answer {
                    line("Главой ${it.name.withFirstBigChar()} является:")
                    line("# ФИО")
                    line("<${it.headFio}>")
                    line("# Должность")
                    line("<${it.positionHead}>")
                    line("# Деятельность")
                    line("<${it.activity}>")
                }
            }
            asQuestion("Деятельность ${it.lowName} ${it.name}") {
                answer {
                    line("Деятельностью для <${it.name}> является <${it.activity}>.")
                }
            }
            it.controlTypes.controlTypes.forEach {
                it.allRequires.forEach {
                    asQuestion("Требование ${it.requireName}") {
                        floatAnswer({
                            line("Требование <${it.requireName.shrink()}> включает в себя")
                            line("Санкций: <${it.punishments.size}>")
                            line("НПА: <${it.targetsNPA.size}>")
                            line("Проверки: <${it.checkers.size}>")
                            line("Видов деятельности: <${it.formatedActivites.size}>")
                            line("Задавайте вопросы по этому требованию.")
                            line("Например: <Вывести полное название требования>")
                        }) {
                            answerIf("Полное название") {
                                answer {
                                    line("### Вывожу полное название")
                                    line(it.requireName)
                                }
                            }
                            answerIf("Виды деятельности") {
                                answer {
                                    line("Виды деятельности для ${it.requireName}")
                                    line("")
                                    it.formatedActivites.forEach {
                                        line("  ${it.code} ${it.text}")
                                    }
                                }
                            }
                            answerIf("Санкции") {
                                fun all(scope:BranchScope){
                                    scope.answer {
                                        hasTable()
                                        line("### Санкции для <${it.requireName}>")
                                        it.punishments.forEach {
                                            line("***")
                                            line(it.right)
                                            table {
                                                head("Тип санкции", "Штраф")
                                                it.items.forEach {pType->
                                                    pType.items.forEach { item->
                                                        row(item.header.withFirstBigChar(), item.amount)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (it.punishments.size > 5) {
                                    answer {
                                        approval({
                                            line("Слишком много санкций (${it.punishments.size})")
                                            line("Хотите вывести все?")
                                        },
                                            yes = {
                                                all(this)
                                            },
                                            no = {
                                                answer {
                                                    line("Хорошо, задавайте другой вопрос по этому требованию.")
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    all(this)
                                }
                            }
                            answerIf("НПА") {
                                answer {
                                    line(it.requireName.shrink())
                                    it.targetsNPA.forEach {
                                        line("### НПА")
                                        line("${it.desc}")
                                        line("***")
                                        line("### От: ${it.from} До: ${it.to}")
                                    }
                                }
                            }
                            answerIf("Проверки") {
                                answer {
                                    line("Проверки для ${it.requireName}")
                                    table {
                                        head("Орган проверки", "Документы", "Тип проверки")
                                        it.checkers.forEach {
                                            row("${it.orgWithInfo}", "${it.documentAccepts}", "${it.type}")
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
            asQuestion("Информация ${it.lowName} ${it.name}") {
                floatAnswer({
                    line("Контрольно-надзорный орган ${it.lowName}")
                    line("включает в себя следующее")
                    line("Норм. правов. актов: ${it.totalNPAs}")
                    line("Требования: ${it.totalRequires}")
                    line("Задавайте вопросы по этому КНО.")
                }) {
                    answerIf("Акты нпа") {
                        answer {
                            table {
                                head("НПА", "Файл", "От")
                                it.npas.forEach {
                                    row("${it.text}", "${it.fileInfo}", "${it.date}")
                                }
                            }
                        }
                    }
                    answerIf("Не понял как? почему зачем кому где как") {
                        answer {
                            line("Если у вас есть <вопросы> вы можете записаться к нам на <консультацию>.")
                            line("Нажимайте на кнопку ниже.")
                            consultButton()
                        }
                    }
                    answerIf("Какая должность у главы?") {
                        answer {
                            line("Должность у главы ${it.headFio}:")
                            line("")
                            line(it.positionHead)
                        }
                    }
                    answerIf("Кто управляет? Управляющий") {
                        answer {
                            line("Главой ${it.lowName} является ${it.headFio}")
                        }
                    }
                    answerIf("Сколько требований") {
                        answer {
                            line("В ${it.lowName} всего ${it.totalRequires}")
                        }
                    }
                }
            }
        }
        asQuestion("Плохо чат бот ответы") {
            approval(head = { line("Тебе нужно открыть вкладку с консультацией?") }, {
                answer {
                    line("Чтобы записаться на консультацию, нажмите на кнопку ниже.")
                    consultButton()
                }
            }, no = {
                answer {
                    line("Хорошо. Задавайте мне вопрос, постараюсь на него вам ответить!")
                }
            })
        }
        Endpoints.init()
    }
}