package ru.bav.server.api.core

import ru.bav.server.*
import ru.bav.server.db.SystemDB
import ru.bav.server.db.schedule.*
import ru.bav.server.keywords.Message
import ru.bav.server.user.Role
import ru.bav.server.user.User

class UserController : IEndpoints {

    override val basePath: String
        get() = "/user"

    @Get("/roles")
    @Description("Получить системные роли")
    fun getRoles(): Any {
        return Role.values()
    }

    @Get("/consultants")
    @Description("Получить айдишники консультантов")
    fun getConsultants(): List<Long> {
        return SystemDB.consultants
    }

    @Post("/setRole")
    @Description("Установить роль юзеру")
    fun setRole(userId: Long, roleTag: String): Any {
        val usr = Server.cache.getCachedOrLoad(userId) ?: return ErrOut("User not found")
        usr.role = Role.byName(roleTag) ?: return ErrOut("No role found!")
        if (usr.role == Role.CONSULTANT) {
            SystemDB.consultants.addUnique(usr.dbId)
        } else {
            SystemDB.consultants.removeAll { it == usr.dbId }
        }
        return "Success"
    }

    @Get("/info")
    @Description("Получить инфу о юзере")
    fun info(id: Long): Any {
        return Server.cache.getCachedOrLoad(id) ?: ErrOut("User not found")
    }

    @Get("/maxpages")
    @Description("Макс пейдж")
    fun maxPage(id: Long): Any {
        val usr = Server.cache.getCachedOrLoad(id) ?: return ErrOut("User not found")
        return (usr.chat.messages.size / 20) + 1
    }

    @Get("/page")
    @Description("Страница последних сообщений пользователя")
    fun page(id: Long, page: Int): Any {
        val usr = Server.cache.getCachedOrLoad(id) ?: return ErrOut("User not found")
        return usr.chat.getPage(page - 1).map { toMessageOut(it) }
    }

    @Get("/consult/month")
    @Description("Получить все записи возможные")
    fun consultMonthList(lowName: String): Any {
        val list = mutableListOf<DaySlots>()
        val org = SystemDB.byLowName(lowName) ?: return ErrOut("Нет такой организации")
        val (from, to) = monthRange(moscowMillis())
        org.schedule.availableMonths.forEach { month ->
            month.getDays().filter { it.timestamp in from .. to}.forEach {
                if(!it.isDayBusy()){
                    list += it
                }
            }
        }
        return list
    }

    @Get("/consults")
    @Description("Все слоты в виде дней по id")
    fun consults(userId:Long) : Any {
        val usr = Server.cache.getCachedOrLoad(userId) ?: return ErrOut("No user!")
        return usr.getDays()
    }

    /*@Get("/consult/list")
    @Description("Получить все записи юзера (если isAll == true то lowName и dayLong не учитываются вообще, а выводятся сразу все слоты)")
    fun consultList(userId: Long, isAll: Boolean, lowName: String?, dayLong: Long?): Any {
        val (from, to) = dayRange(dayLong ?: 0L)
        val usr = Server.cache.getCachedOrLoad(userId) ?: return ErrOut("No user!")
        val toRet = mutableListOf<SlotID>()
        usr.slots.forEach {
            val daySlot = it.getDaySlot() ?: return@forEach
            if (isAll) {
                toRet += it
                return@forEach
            }
            if (it.orgLowName == lowName && daySlot.localDateTimeFrom in from..to) {
                toRet += it
            }
        }
        return toRet
    }*/

    @Post("/clearConsults")
    @Description("Удаляет все записи по айди юзера")
    fun clearConsults(userId: Long): Any {
        val usr = Server.cache.getCachedOrLoad(userId) ?: return ErrOut("No user!")
        usr.slots.toList().forEach {
            val day = it.getDaySlot() ?: return "No day!"
            day.clear()
            println("CLEARED")
        }
        usr.slots.clear()
        return "Success"
    }

    @Post("/confirmConsult")
    @Description("Подтверждение записи консультантом")
    fun confirmConsult(consultant: Long, lowName: String, from: Long): Any {
        /*val consultantUser = Server.cache.getCachedOrLoad(consultant) ?: return ErrOut("No user!")*/
        val org = SystemDB.byLowName(lowName)!!
        val slot = org.schedule.cachedSlots[from] ?: return ErrOut("No slot!")
        if (slot.idConsultantUser != consultant) return ErrOut("Не ваш слот")
        if (slot.status == SlotStatus.WAIT_CONFIRM) {
            slot.confirm()
            return "Success"
        } else {
            return ErrOut("Не ожидается подтверждения")
        }
    }

    @Post("/consultBook")
    @Description("Запись на консультацию")
    fun consultBook(userId: Long, lowName: String, idControl: Int, idRequire: Int, from: Long, question: String): Any {
        val usr = Server.cache.getCachedOrLoad(userId) ?: return ErrOut("No user!")
        val org = SystemDB.byLowName(lowName)!!
        val slot = org.schedule.cachedSlots[from] ?: return ErrOut("No slot!")
        if (slot.idToConsultUser == null) {
            val consultantUser = Server.cache.getCachedOrLoad(SystemDB.getFreeConsultant(from)!!)!!
            if (!slot.isBusy()) {
                slot.makeBusy(usr, consultantUser, question, idControl, idRequire)
                return "Success"
            } else {
                return ErrOut("Слот уже занят!")
            }
        } else {
            return ErrOut("Этот слот уже занят!")
        }
    }

    @Post("/makeuser")
    @Description("Создать юзера в БД с ФИО и Айди")
    fun makeUser(id: Long, email: String, roleTag: String): Any {
        println("MAKEUSER: ${id}")
        val usr = User()
        //val f = fio.split(" ")
        usr.dbId = id
        usr.firstName = "Test"
        usr.secondName = "Test"
        usr.thirdName = "Test"
        usr.email = email
        usr.role = Role.byName(roleTag) ?: return ErrOut("Такая роль не найдена.")
        Server.database.createUser(usr) ?: return ErrOut("User already created")
        return "Success"
    }

    class MessageOut {
        var isBot: Boolean = false
        var text: String = ""
        var timestamp: Long = 0L
        var isButton: Boolean = false
        var hasTable: Boolean = false
    }

    fun toMessageOut(msgRet: Message): MessageOut {
        val msgOut = MessageOut()
        msgOut.isBot = msgRet.isBot
        val textFirst = msgRet.text.joinToString("", "", "")
        msgOut.text = concatHighlights(parseHighlights(textFirst))
        msgOut.isButton = msgRet.isButton
        msgOut.hasTable = msgRet.hasTable
        /*
        if(!msgOut.isBot){
            msgOut.text = "${concatHighlights(parseHighlights(textFirst))}"
        }else{
            msgOut.text = "<div>${concatHighlights(parseHighlights(textFirst))}</div>"
        }
*/
        msgOut.timestamp = msgRet.timestamp
        return msgOut
    }

    @Description("Запрос на сообщение в чат бот")
    @Post("/onmessage")
    fun onMessage(userId: Long, message: String): Any {
        val usrSess = Server.cache.getCachedOrLoadSession(userId) ?: return ErrOut("Not found user!")
        val msgRet = usrSess.user.chat.onMessage(usrSess.user, message)
        usrSess.resetUnload()
        usrSess.user.save()
        return toMessageOut(msgRet)
    }
}