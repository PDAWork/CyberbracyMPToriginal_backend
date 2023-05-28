package ru.bav.server.notify

import ru.bav.server.user.User

class RemoteNotification()
//TODO Future notification, пингуем фронтент каждую минуту и запрос в пост, затем берём месседж, и отображаем его, и удаляем с кэше
object NotificationMap {

    private val map:MutableMap<Long, RemoteNotification> = mutableMapOf()

    fun putNotification(user:User, notification: RemoteNotification){
        map[user.dbId] = notification
    }

    fun popNotification(id:Long) : RemoteNotification? {
        val inCache = map[id] ?: return null
        map.remove(id)
        return inCache
    }
}