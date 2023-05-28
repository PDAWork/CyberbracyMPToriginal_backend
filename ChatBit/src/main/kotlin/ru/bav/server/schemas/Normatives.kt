import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class NormativesFile{
    lateinit var nm:Normatives
    var fileName:String = ""
}
@Serializable
class Normatives {
    val requirements:MutableList<DTRequirement> = mutableListOf()

    //18 - Start

}
//Требования
@Serializable
class DTRequirement {
    var header:String? = null
    var headerSection:String? = null
    var name:String = ""
    var nameSection:String = ""
    @Transient
    var rowStarts:Int = 0

    var typeReq:String = ""
    var checkMethod:String? = ""

    var ableToGetKNO:String? = null //Возможность получения КНО необходимых подтверждающих документов/сведений по межведомственному взаимодействию (да/нет)

    var lifetimeDocuments:String? = null //Срок действия подтверждающих документов (если применимо)

    //Санкции
    var typeOfPunish:String? = null //Вид ответственности (уголовная /административная/ гражданско-правовая/иная ответственность)

    val punishments:MutableList<DTPunish> = mutableListOf()
    val targetsNPA:MutableList<DTNPA> = mutableListOf()

    var organWithRightsToPunish:String? = null //Орган, уполномоченный на привлечение к ответственности

    var stepsToPrivlichenie:String? = null //Порядок привлечения к ответственности (ссылка на файл без авторизации)

    //Профилирование и доп инфа
    var typeOfDeyatelnostSubjectControl:String? = null //Виды деятельности субъектов контроля, на которые распространяется ОТ (по ОКВЭД 2)
    var utochnenieVidovDoing:String? = null //Уточнение вида деятельности (при необходимости)
    var totalQuestion:String? = null //Характеристика (для общего вопроса)

    var additionQuestion:String? = null //Характеристика (для уточняющего вопроса)

    var businessTotalQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для общего вопроса)

    var businessAdditionQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для уточняющего вопроса)

    val accepts = mutableListOf<DTAccept>()

    @Serializable
    class DTAccept(val type:String, val documentAccepts:String, val orgWithInfo:String?, val ability:String?)
    @Serializable
    class DTNPA(val desc:String, val from:String?, val to:String?)
    @Serializable
    class DTPunish(val zakon:String, val items:MutableList<DTPunishItem>)
    @Serializable
    class DTPunishItem(val type:String, val header:String, val amount:String, val vidNormy:String)
}