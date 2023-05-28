package ru.bav.server.api

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/*fun main(){
    RoomsAPI.postQuery("""
        {user: 5, title: "Cool Room", description: "Ывфв", typeOf: "OTA"}
    """.trimIndent()).apply {
        println(this)
    }
}*/

object RoomsAPI {

    fun login(){

    }

    fun postQuery(body:String) : String {
        val url = URL("http://46.243.201.240:8000/api/rooms/")
        val httpCon = url.openConnection() as HttpURLConnection
        httpCon.doOutput = true
        httpCon.requestMethod = "POST"
        httpCon.addRequestProperty("Authorization", "Bearer ")
        val os: OutputStream = httpCon.outputStream
        val osw = OutputStreamWriter(os, "UTF-8")
        osw.write(body)
        osw.flush()
        osw.close()
        os.close()

        httpCon.connect()

        val result: String

        val bis = BufferedInputStream(httpCon.inputStream)
        val buf = ByteArrayOutputStream()
        var result2 = bis.read()
        while (result2 != -1) {
            buf.write(result2.toByte().toInt())
            result2 = bis.read()
        }
        result = buf.toString()
        return String(result.toByteArray(), Charsets.UTF_8)
    }
}