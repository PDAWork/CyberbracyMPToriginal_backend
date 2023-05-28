package ru.bav.server

import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import ru.bav.server.db.SystemDB
import java.io.IOError
import java.time.ZoneId
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object CommandHandler {

    var reader: LineReader? = null
    var terminal: Terminal? = null
    var thread:Thread? = null
    var commonLogs:Boolean = true

    fun setup() {
        terminal = TerminalBuilder.terminal()
        reader = LineReaderBuilder.builder().terminal(terminal)
            .build()
    }

    fun startThread(){
        var isClosing = false
        val thread = object : Thread("Commander") {
            override fun run() {
                var line: String
                while (!interrupted()) {
                    try {
                        var pref = "[Server] "
                        line = reader!!.readLine(pref)
                        if (line == null) {
                            continue
                        }
                        CommandExecutor.executeRaw(line)
                    }catch (e: IOError) {
                        exitProcess(0)
                    } catch (e: UserInterruptException) {
                        if(isClosing) continue
                        isClosing = true
                        thread {
                            safePrintLn("&rВыключение...")
                            Server.cache.saveAll()
                            SystemDB.write()
                            safePrintLn("&gУспешно отключен. ")
                            exitProcess(0)
                        }
                    }
                }
            }
        }
        this.thread = thread
        thread.start()
    }

    fun stopThread(){
        thread?.stop()
    }

    fun numberToTimeFormat(time:Int) : String {
        var i = time.toString() + ""
        if (i.length == 1) {
            i = "0$time"
        }
        return i
    }

    fun toConsoleLine(text:String) : String {
        val calendar = GregorianCalendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Moscow"))
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val min = calendar[Calendar.MINUTE]
        val sec = calendar[Calendar.SECOND]
        val date = "${numberToTimeFormat(hour)}:${numberToTimeFormat(min)}:${numberToTimeFormat(sec)}"
        return "[$date] $text"
    }

    fun safePrintLn(s:String, common:Boolean = false){
        if(!commonLogs && common)return
        val x = toConsoleLine(s)
        val r = reader ?: return
        if(!r.isReading){
            r.terminal.writer().println(x)
            return
        }else{
            r.callWidget(LineReader.CLEAR)
            r.terminal.writer().println(x)
            r.callWidget(LineReader.REDRAW_LINE)
            r.callWidget(LineReader.REDISPLAY)
            r.terminal.writer().flush()
        }
    }
}