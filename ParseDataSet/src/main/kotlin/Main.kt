import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

fun slots(){
    val consults = File("C:\\Users\\beena\\Desktop\\Датасеты\\Приложение_2_Слоты_для_записи_на_Консультирование.xlsx")
    val sch = Schedules(consults)
    sch.read()
    val out = Json.encodeToString(sch)
    File("C:\\Users\\beena\\Desktop\\Датасеты\\out").writeText(out)
}
fun dataSET(){

    val folder = File("C:\\Users\\beena\\Desktop\\Датасеты\\Приложение_1_Список_обязательных_требований_КНО_Москвы")
    val req = ObRequires(folder)
    req.read()
    File("C:\\Users\\beena\\Desktop\\Датасеты\\out.json").writeText(Json.encodeToString(req))
    /*val consults = File("C:\\Users\\beena\\Desktop\\Датасеты\\Приложение_1_Список_обязательных_требований_КНО_Москвы\\Главархив\\Архивное дело 38 ОТ\\38 ОТ.xlsx")
    val nm = NormativesFile(consults)
    nm.read()*/
}

class Bi(){
    fun exec(s:String){
        println("URA! $s")
    }
}
fun main(){
    dataSET()

}

fun readFromExcelFile(filepath: String) {
    val inputStream = FileInputStream(filepath)
    //Instantiate Excel workbook using existing file:
    var xlWb = WorkbookFactory.create(inputStream)

    //Row index specifies the row in the worksheet (starting at 0):
    val rowNumber = 0
    //Cell index specifies the column within the chosen row (starting at 0):
    val columnNumber = 0

    //Get reference to first sheet:
    val xlWs = xlWb.getSheetAt(0)
    println(xlWs.getRow(rowNumber).getCell(columnNumber))
}