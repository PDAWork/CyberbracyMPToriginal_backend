package ru.bav.server.user

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UserCache(val database:UserDB) {

    val unloader = Executors.newScheduledThreadPool(1)

    companion object {
        val UNLOAD_TIME = 1000 * 120 //120 секунд
    }

    init {
        unloader.scheduleWithFixedDelay(Runnable {
            try{
                val toUnload = mutableListOf<Session>()
                cache.forEach { (id, session) ->
                    if(session.toUnload < System.currentTimeMillis()){
                        toUnload += session
                    }
                }
                toUnload.forEach {
                    it.user.save()
                    cache.remove(it.user.dbId)
                    println(" [-] ${it.user.dbId}")
                }
                toUnload.clear()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }, 5L, 5L, TimeUnit.MINUTES)
    }

    class Session(val user:User){
        var toUnload = System.currentTimeMillis() + UNLOAD_TIME

        //Активность любая
        fun resetUnload(){
            toUnload = System.currentTimeMillis() + UNLOAD_TIME
        }
    }

    val cache:MutableMap<Long, Session> = mutableMapOf()
    fun getSession(id:Long) : Session? = cache[id]

    fun getCachedOrLoadSession(id:Long) : Session?{
        val sess = getSession(id)
        if(sess != null){
            return sess
        }
        return loadSession(id)
    }

    fun getCachedOrLoad(id:Long) : User?{
        return getCachedOrLoadSession(id)?.user
        /*val sess = getSession(id)
        if(sess != null){
            return sess.user
        }
        return loadSession(id)?.user*/
    }

    fun loadSession(id:Long) : Session? {
        val inCache = getSession(id)
        if(inCache != null){
            inCache.toUnload = System.currentTimeMillis() + UNLOAD_TIME
            return inCache
        }
        val usrToLoad = database.read(id) ?: return null
        val newSess = Session(usrToLoad)
        cache[id] = newSess
        println("   [+] ${id}")
        return newSess
    }

    fun saveAll() {
        cache.forEach { (t, u) ->
            database.write(u.user)
        }
    }
}