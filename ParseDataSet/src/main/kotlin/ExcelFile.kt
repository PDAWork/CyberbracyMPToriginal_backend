import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream

open class ExcelFile {
    @Transient lateinit var file: File

    @Transient lateinit var workBook:Workbook
    constructor(){

    }
    constructor(file: File){
        this.file = file
        this.workBook = readFromExcelFile()
    }

    open fun read(){

    }

    fun readFromExcelFile() : Workbook {
        val inputStream = FileInputStream(file)
        return WorkbookFactory.create(inputStream)
    }
}