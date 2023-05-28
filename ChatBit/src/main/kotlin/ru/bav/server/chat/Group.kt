package ru.bav.server.chat

class Group(all: String) {

    companion object{
        val SEPARATORS = Group("@#\$%^&*()-=+_'\\[],.! ")
    }

    val chars: MutableList<Char> = mutableListOf()
    init {
        all.lowercase().toCharArray().forEach {
            if(chars.contains(it))return@forEach
            chars += it
        }
    }
    fun clean(inp:String) : String {
        var out = inp
        chars.forEach {
            out = out.replace("${it}", "")
        }
        return out
    }

    fun isContains(c:Char) : Boolean {
        val cc = Character.toLowerCase(c)
        chars.forEach {
            //if(it === c)return true
            if(Character.toLowerCase(it) === cc)return true

            //if(c.equals(it, true))return true
        }
        return false
    }
}