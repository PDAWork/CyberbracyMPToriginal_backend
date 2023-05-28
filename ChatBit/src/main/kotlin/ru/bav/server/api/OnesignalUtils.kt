package ru.bav.server.api

import com.onesignal.client.ApiClient
import com.onesignal.client.Configuration
import com.onesignal.client.api.DefaultApi
import com.onesignal.client.auth.HttpBearerAuth
import com.onesignal.client.model.Notification
import com.onesignal.client.model.StringMap
import java.util.concurrent.Executors


object OnesignalUtils {
    private const val appId = "8ced7149-ca5f-471c-b828-23677f82feb7"
    private const val appKeyToken = "ZjMzZTEwYmItZmUyNi00ZTY2LTk1NzQtMzBiODE5MmNkODIx"
    private const val userKeyToken = "M2FjNGQ2Y2MtNWVmNi00M2U5LTg2ZDYtOWUxYWM5ZmE0OGU5\n"

    val executor = Executors.newFixedThreadPool(4)

    private fun createNotification(text:String, email:String): Notification {
        //50678735-44b6-4e4c-825b-a704a016934d
        val notification = Notification()
        notification.appId = appId
        notification.includeExternalUserIds = listOf(email)
        //notification.includePlayerIds = players
        notification.isAndroid = true
        //notification.includedSegments = listOf("Subscribed Users")
        val contentStringMap = StringMap()
        contentStringMap.en(text)
        notification.contents = contentStringMap
        return notification
    }

    fun send(text:String, email:String){
        executor.execute {
            try{
                println("Отправка уведомления ${text} -> ${email}")
                val defaultClient: ApiClient = Configuration.getDefaultApiClient()
                val appKey: HttpBearerAuth = defaultClient.getAuthentication("app_key") as HttpBearerAuth
                appKey.setBearerToken(appKeyToken)
                val userKey: HttpBearerAuth = defaultClient.getAuthentication("user_key") as HttpBearerAuth
                userKey.setBearerToken(userKeyToken)
                val api = DefaultApi(defaultClient)
                val notification = createNotification(text, email)
                val response = api.createNotification(notification)
                println("SENDED NOTIFICATION: ${response.errors}")
            }catch (e:Exception){
                println("Не удалось отправить уведомление ${email}: ${e.message}")
            }
        }
    }
}