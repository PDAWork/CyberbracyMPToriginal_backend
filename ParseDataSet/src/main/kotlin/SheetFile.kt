import kotlinx.serialization.Serializable
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import java.util.*

open class SheetFile {
    @Transient lateinit var sheet: Sheet
    constructor(sheet: Sheet){
        this.sheet = sheet
    }
    constructor()
    fun cellAt(row:Int, col:Int) : Cell? {
        return sheet.getRow(row)?.getCell(col)
    }

    fun readDate(row:Int, col:Int) : Date {
        val any = readAny(row, col) ?: throw IllegalArgumentException("Fail to read Date at [$row][$col]")
        if(any is Double){
            val javaDate: Date = DateUtil.getJavaDate(any)
            return javaDate
        }
        throw IllegalArgumentException("Fail to read Date at [$row][$col] (Need double!)")
    }

    fun notBlank(row:Int, col:Int) : Boolean {
        return (readAny(row, col) as? String)?.isNotBlank() ?: false
    }

    fun notNull(row:Int, col:Int) : Boolean = readAny(row, col) != null

    fun readString(row:Int, col:Int) : String = readAny(row, col) as? String ?: throw IllegalArgumentException("Ожидалась строка на [$row][${col}]")

    fun readStringNullable(row:Int, col:Int) : String? = readAny(row, col) as? String

    fun readAny(row:Int, col:Int) : Any? {
        val cell = cellAt(row, col) ?: return null
        return when(cell.cellType){
            CellType.NUMERIC->return cell.numericCellValue
            CellType.BOOLEAN->return cell.booleanCellValue
            CellType.BLANK->return null
            CellType.FORMULA->throw IllegalArgumentException("not support formulas")
            CellType.STRING->return cell.richStringCellValue.string
            else->{IllegalArgumentException("Not support cell read!")}
        }
    }
}