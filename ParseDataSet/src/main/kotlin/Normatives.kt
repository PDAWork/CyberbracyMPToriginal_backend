import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.apache.poi.ss.usermodel.Sheet
import java.io.File

@Serializable
class NormativesFile : ExcelFile {
    lateinit var nm:Normatives
    var fileName:String = ""

    constructor() : super()
    constructor(file: File) : super(file){
        fileName = file.nameWithoutExtension
    }


    override fun read() {
        workBook.getSheetAt(0).let {sheet->
            nm = Normatives(sheet)
            nm.read()
        }
    }
}
@Serializable
class Normatives : SheetFile {
    val requirements:MutableList<Requirement> = mutableListOf()

    constructor(sheet: Sheet) : super(sheet)
    constructor() : super()


    //18 - Start

    fun read(){
        var i = 3

        while(i < sheet.physicalNumberOfRows){
            if(notBlank(i, 0)){

                val req = Requirement(sheet, i)
                req.read()
                requirements += req
            }
            i++
        }
    }

}
//Требования
@Serializable
class Requirement : SheetFile {
    var header:String? = null
    var headerSection:String? = null
    var name:String = ""
    var nameSection:String = ""
    @Transient
    var rowStarts:Int = 0




    var typeReq:String = ""
    //var periodNPAFrom:String? = null
    //var periodNPATo:String? = null

    //var checkMethod:String? = null //Метод проверки соответствия
    //var documentsAccepts:String? = null //Документы, подтверждающие  соответствие субъекта/объекта контроля ОТ (если применимо)
    //var OGV:String? = null //ОГВ (ОМСУ), организации, в распоряжении которых находятся необходимые сведения (уполномоченные на выдачу подтверждающих документов)

    var checkMethod:String? = "" //Метод проверки
    var ableToGetKNO:String? = null //Возможность получения КНО необходимых подтверждающих документов/сведений по межведомственному взаимодействию (да/нет)

    var lifetimeDocuments:String? = null //Срок действия подтверждающих документов (если применимо)

    //Санкции
    var typeOfPunish:String? = null //Вид ответственности (уголовная /административная/ гражданско-правовая/иная ответственность)

    val punishments:MutableList<Punish> = mutableListOf()
    val targetsNPA:MutableList<NPA> = mutableListOf()

    var organWithRightsToPunish:String? = null //Орган, уполномоченный на привлечение к ответственности

    var stepsToPrivlichenie:String? = null //Порядок привлечения к ответственности (ссылка на файл без авторизации)

    //Профилирование и доп инфа
    var typeOfDeyatelnostSubjectControl:String? = null //Виды деятельности субъектов контроля, на которые распространяется ОТ (по ОКВЭД 2)
    var utochnenieVidovDoing:String? = null //Уточнение вида деятельности (при необходимости)
    var totalQuestion:String? = null //Характеристика (для общего вопроса)

    var additionQuestion:String? = null //Характеристика (для уточняющего вопроса)

    var businessTotalQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для общего вопроса)

    var businessAdditionQuestion:String? = null //Содержание вопроса бизнесу для профилирования (для уточняющего вопроса)

    val accepts = mutableListOf<Accepts>()

    constructor(sheet: Sheet, rowStarts: Int) : super(sheet) {
        this.rowStarts = rowStarts
    }

    constructor() : super()

    @Serializable
    class Accepts(val type:String, val documentAccepts:String, val orgWithInfo:String?, val ability:String?)
    @Serializable
    class NPA(val desc:String, val from:String?, val to:String?)
    @Serializable
    class Punish(val zakon:String, val items:MutableList<PunishItem>)
    @Serializable
    class PunishItem(val type:String, val header:String, val amount:String, val vidNormy:String)

    fun parseAccepts(height: IntRange){
        var c = height.first
        while(c in height){
            if(notBlank(c, 9)){
                try{
                    val type = readString(c, 8)
                    val documentAccepts = readString(c, 9)
                    val orgWithInfo = readStringNullable(c, 10)
                    val ability = readStringNullable(c, 11)
                    accepts += Accepts(type, documentAccepts, orgWithInfo, ability)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            c++
        }
    }

    fun parseDescs(height: IntRange){
        var c = height.first
        while(c in height && notBlank(c, 5)){
            val cur = readString(c, 5)
            val from = readStringNullable(c, 6)
            val to = readStringNullable(c, 7)
            targetsNPA += NPA(cur, from, to)
            c++
        }
    }
    //13

    private fun parseItems(start:Int, height:IntRange, punish: Punish) {
        var c = start
        var firstNotBlank = true
        while(c in height) {
            fun read(){
                val cur = readAny(c, 13)
                val subject = readString(c, 14)
                val sanction = readString(c, 15)
                val amount = readString(c, 16)
                val vidNormy = readString(c, 17)
                punish.items += PunishItem(subject, sanction, amount, vidNormy)
            }
            if(firstNotBlank){
                if(notBlank(c, 13)){
                    read()
                }
                firstNotBlank = false
            }else{
                if(!notBlank(c, 18)){
                    read()
                } else{
                    break
                }
            }
            c++
            /*try {
                val cur = readAny(c, 13)
                val subject = readString(c, 14)
                val sanction = readString(c, 15)
                val amount = readString(c, 16)
                punish.items += PunishItem(subject, sanction, amount)
                c++
            } catch (e: Exception) {
                e.printStackTrace()
            }*/
        }
    }

    fun parsePunishments(height:IntRange){
        var c = height.first
        while(c in height){
            try{
                if(notBlank(c, 18)){
                    val cur = readAny(c, 18)
                    val items = mutableListOf<PunishItem>()
                    val itemsObj = Punish(cur as String, items)
                    parseItems(c, height, itemsObj)
                    punishments += itemsObj
                }
                c++
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun read(){
        val header = readAny(rowStarts - 1, 2)
        val headerNum = readAny(rowStarts - 1, 1)
        if(header != null && notBlank(rowStarts - 1, 2)){
            this.header = header as String
            this.headerSection = headerNum as String
            println("$headerNum ${header}")
        }
        val treb = readString(rowStarts, 18) //Repeats?

        typeReq = readString(rowStarts, 4)
        //periodNPAFrom = readStringNullable(rowStarts, 6)
        //periodNPATo = readStringNullable(rowStarts, 3)
        checkMethod = readStringNullable(rowStarts, 8)
        //documentsAccepts = readStringNullable(rowStarts, 9)
        //OGV = readStringNullable(rowStarts, 10)
        ableToGetKNO = readStringNullable(rowStarts, 11)
        lifetimeDocuments = readStringNullable(rowStarts, 12)
        typeOfPunish = readStringNullable(rowStarts, 13)
        organWithRightsToPunish = readStringNullable(rowStarts, 19)
        stepsToPrivlichenie = readStringNullable(rowStarts, 20)
        typeOfDeyatelnostSubjectControl = readStringNullable(rowStarts, 21)
        utochnenieVidovDoing = readStringNullable(rowStarts, 22)
        totalQuestion = readStringNullable(rowStarts, 23)
        additionQuestion = readStringNullable(rowStarts, 24)
        businessTotalQuestion = readStringNullable(rowStarts, 25)
        businessAdditionQuestion = readStringNullable(rowStarts, 26)

        val trebHead = readAny(rowStarts, 3)
        val trebHeadNum = readAny(rowStarts, 1)
        if(notBlank(rowStarts, 3) && notBlank(rowStarts, 1)){
            this.name = trebHead as String
            this.nameSection = trebHeadNum as String
            println("   ${trebHeadNum} ${trebHead}")
        }

        var c = rowStarts + 1
        while(!notBlank(c, 0) && c < sheet.physicalNumberOfRows){
            c++
        }
        c--
        val sizeHeight = rowStarts..c
        parseDescs(sizeHeight)
        parsePunishments(sizeHeight)
        parseAccepts(sizeHeight)
        println(" ::: NPASize: ${targetsNPA.size}; Punishments: ${punishments.size} Accepts: ${accepts.size}")
    }
}