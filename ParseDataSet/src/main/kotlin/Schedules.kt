import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.apache.poi.ss.usermodel.Sheet
import java.io.File

@Serializable
class Schedules : ExcelFile {

    constructor(file: File) : super(file)
    constructor() : super()
    val organizations = mutableListOf<ScheduleOrg>()

    @Serializable
    class ScheduleOrg : SheetFile {



        var name = ""
        var lowName = ""
        val descs = mutableListOf<String>()
        val months = mutableListOf<Month>()

        constructor(sheet:Sheet) : super(sheet){

        }

        constructor() : super()

        @Serializable
        class Month : SheetFile {
            var name:String = ""

            @Transient
            var row:Int = 0
            @Transient
            var stride:Int = 0

            constructor(name:String, row:Int, stride:Int, sheet:Sheet) : super(sheet){
                this.name = name
                this.row = row
                this.stride = stride
            }
            constructor() : super()
            val days:MutableList<MPair> = mutableListOf()

            @Serializable
            class MPair{
                var timestamp:Long = 0L
                var range:String = ""

                constructor(timestamp: Long, range: String) {
                    this.timestamp = timestamp
                    this.range = range
                }
            }

            fun read(){
                var dateRows = row + 1
                try{
                    while(readAny(dateRows, stride) is Double){
                        val cur = readDate(dateRows, stride)
                        if(readAny(dateRows, stride + 1) is Double){
                            println("Error date at schedule ${name}")
                            dateRows++
                            continue
                        }
                        val range = readString(dateRows, stride + 1)
                        days += MPair(cur.time, range)
                        dateRows++
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }

        fun read(){
            name = readString(0, 0)
            var c = 1
            while(readAny(c, 0) != null){
                descs += readString(c, 0)
                c++
            }
            var tr = 0
            while(readAny(c, 0) == null && tr < 50){
                c++
                tr++
            }
            if(tr > 49){
                throw IllegalArgumentException("Stackoverflow")
            }
            val slots = readString(c, 0)
            if(slots.contains("Слоты")){
                c++
                val size = 3
                var col = 0
                while(readAny(c, col) != null){
                    val month = readString(c, col)
                    val mObj = Month(month, c, col, sheet)
                    mObj.read()
                    months += mObj
                    col += size
                }
            }else{
                throw IllegalArgumentException("Ожидалась строка Слоты")
            }
        }
    }

    override fun read() {
        for(sheet in 0 until workBook.numberOfSheets){
            parse(sheet)
        }
    }

    fun parse(i:Int){
        val sheet = workBook.getSheetAt(i)
        if(sheet.sheetName == "Общая информация")return
        val sOrg = ScheduleOrg(sheet)
        sOrg.lowName = sheet.sheetName
        sOrg.read()
        organizations += sOrg
    }
}