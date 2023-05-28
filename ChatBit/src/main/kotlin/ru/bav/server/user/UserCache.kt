package ru.bav.server.user

class UserCache(val database:UserDB) {

    companion object {
        val UNLOAD_TIME = 120 //TODO
    }

    class Session(val user:User){
        var toUnload = UNLOAD_TIME
    }

    val cache:MutableMap<Long, Session> = mutableMapOf()
    fun getSession(id:Long) : Session? = cache[id]

    fun getCachedOrLoad(id:Long) : User?{
        val sess = getSession(id)
        if(sess != null){
            return sess.user
        }
        return loadSession(id)?.user
    }

    fun loadSession(id:Long) : Session? {
        val inCache = getSession(id)
        if(inCache != null){
            inCache.toUnload = UNLOAD_TIME
            return inCache
        }
        val usrToLoad = database.read(id) ?: return null
        val newSess = Session(usrToLoad)
        cache[id] = newSess
        return newSess
    }

    fun saveAll() {
        cache.forEach { (t, u) ->
            database.write(u.user)
        }
    }
}