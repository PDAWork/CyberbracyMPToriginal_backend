package ru.bav.server.db.req

import ru.bav.server.db.Activities
import ru.bav.server.db.Activity
import ru.bav.server.withFirstBigChar
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.MapSerializable
import java.beans.Transient

class Require(@get:Transient val controlType: ControlType) : MapSerializable { //1.2

    var requireName:String = ""
    var section:String = ""
    var typeReq:String = ""
    var checkMethod:String? = ""
    var ableToGetKNO:String? = null //Возможность получения КНО необходимых подтверждающих документов/сведений по межведомственному взаимодействию (да/нет)

    var lifetimeDocuments:String? = null //Срок действия подтверждающих документов (если применимо)

    //Санкции
    var typeOfPunish:String? = null //Вид ответственности (уголовная /административная/ гражданско-правовая/иная ответственность)

    val formatTypeOfPunish:String
        get() {
            val o = typeOfPunish?.withFirstBigChar() ?: return "Не указано"
            if(o.isEmpty())return "Не указано"
            return o
        }

    var organWithRightsToPunish:String? = null //Орган, уполномоченный на привлечение к ответственности

    var stepsToPrivlichenie:String? = null //Порядок привлечения к ответственности (ссылка на файл без авторизации)
    var stepsToPrivlichenieLink:String? = null

    //Профилирование и доп инфа
    var typeOfDeyatelnostSubjectControl:String? = null //Виды деятельности субъектов контроля, на которые распространяется ОТ (по ОКВЭД 2)
    var utochnenieVidovDoing:String? = null //Уточнение вида деятельности (при необходимости)
    var totalQuestion:String? = null //Характеристика (для общего вопроса)

    var additionQuestion:String? = null //Характеристика (для уточняющего вопроса)

    var businessTotalQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для общего вопроса)

    var businessAdditionQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для уточняющего вопроса)

    val checkers = mutableListOf<Check>()
    val punishments:MutableList<Punish> = mutableListOf()
    val targetsNPA:MutableList<NPA> = mutableListOf()

    val formatedActivites:MutableList<Activity>
        get(){
            val toRet = mutableListOf<Activity>()
            val acts = Activities.parseAll(typeOfDeyatelnostSubjectControl ?: "Все")
            if(acts.isEmpty()){
                toRet += Activity("Все", "Все")
            }else{
                toRet += acts
            }
            return toRet
        }

    override fun dataDeserialize(map: DataMap) {
        requireName = map.readString("reNa") ?: ""
        section = map.readString("se") ?: ""
        typeReq = map.readString("tR") ?: ""
        ableToGetKNO = map.readString("KNO")
        lifetimeDocuments = map.readString("lD")
        typeOfPunish = map.readString("tOfP")
        organWithRightsToPunish = map.readString("oWiRToP")
        stepsToPrivlichenie = map.readString("sToPr")
        stepsToPrivlichenieLink = map.readString("soPL")
        typeOfDeyatelnostSubjectControl = map.readString("OfDetSutC")
        utochnenieVidovDoing = map.readString("uViDo")
        totalQuestion = map.readString("toQ")
        additionQuestion = map.readString("addQ")
        businessTotalQuestion = map.readString("bToQ")
        businessAdditionQuestion = map.readString("buAdQue")
        checkMethod = map.readString("checkM")

        map.readMapList("chs", checkers) { Check() }
        map.readMapList("pus", punishments) { Punish() }
        map.readMapList("NPAs", targetsNPA) { NPA() }
    }

    override fun dataSerialize(map: DataMap) {
        map.writeString("reNa", requireName)
        map.writeString("se", section)
        map.writeString("tR", typeReq)
        ableToGetKNO?.let { map.writeString("KNO", it) }
        checkMethod?.let { map.writeString("checkM", it) }
        lifetimeDocuments?.let { map.writeString("lD", it) }
        typeOfPunish?.let { map.writeString("tOfP", it) }
        organWithRightsToPunish?.let { map.writeString("oWiRToP", it) }
        stepsToPrivlichenie?.let { map.writeString("sToPr", it) }
        stepsToPrivlichenieLink?.let { map.writeString("soPL", it) }
        typeOfDeyatelnostSubjectControl?.let { map.writeString("OfDetSutC", it) }
        utochnenieVidovDoing?.let { map.writeString("uViDo", it) }
        totalQuestion?.let { map.writeString("toQ", it) }
        additionQuestion?.let { map.writeString("addQ", it) }
        businessTotalQuestion?.let { map.writeString("bToQ", it) }
        businessAdditionQuestion?.let { map.writeString("buAdQue", it) }

        map.writeMapList("chs", checkers)
        map.writeMapList("pus", punishments)
        map.writeMapList("NPAs", targetsNPA)
    }
}