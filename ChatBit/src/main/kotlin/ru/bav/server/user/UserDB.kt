package ru.bav.server.user

import ru.beenaxis.uio.io.Files
import java.io.File

class UserDB(val folder:File) {

    init {
        if(folder.exists().not())
            folder.mkdirs()
    }

    fun getFile(dbId:Long) : File = File(folder, "${dbId}.usr")

    fun createUser(user:User) : User? {
        val inFile = read(user.dbId)
        if(inFile != null)return null
        write(user)
        return user
    }

    fun write(user:User){
        Files.writeData(getFile(user.dbId), user)
    }

    fun read(id:Long) : User? {
        val file = getFile(id)
        if(file.exists().not())return null
        val usr = User()
        Files.readData(file, usr)
        return usr
    }
}