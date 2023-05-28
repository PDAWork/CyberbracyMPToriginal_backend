package ru.bav.server.keywords

import ru.bav.server.chat.Group

class Tokens {
    companion object {

        fun str(str:String) : Tokens = Tokens(*str.split(" ").toTypedArray())
    }
    val keywords:MutableList<String> = mutableListOf()

    constructor(s:String){
        keywords += s
    }

    constructor(vararg s:String){
        keywords += s
    }

    fun similarCoeff(input:String, comparable:String) : Float{
        val cleanInput = Group.SEPARATORS.clean(input)
        val cleanComp = Group.SEPARATORS.clean(comparable)
        var points = 0
        cleanInput.forEachIndexed { index, c ->
            val toCmp = cleanComp.getOrNull(index) ?: return@forEachIndexed
            if(Character.toLowerCase(toCmp) === Character.toLowerCase(c)){
                points++
            }
        }
        return points.toFloat()
    }

    //Similar: 0f - 1f
    fun compareActivation(other: Tokens) : Float {
        var count = 0f
        other.keywords.forEach { lWord->
            val contained = keywords.firstOrNull { similarable ->
                similarCoeff(lWord, similarable) > lWord.length * (50.0 / 100.0)
            }
            if(contained != null){
                val coeff = similarCoeff(lWord, contained)
                count += coeff
            }
        }
        //println("${other.toStr()} = ${count}")
        /*keywords.forEach {first->
            other.keywords.forEach {second->
                val coeff = similarCoeff(first, second)
                if(coeff > )
            }
            if(other.keywords.contains(it.lowercase())){
                count++
            }
        }*/
        /*var s = (other.keywords.size + keywords.size).toFloat()
        s *= s
        s = 4f / s.toFloat()*/
        return count.toFloat()
    }

    fun toStr() : String = keywords.joinToString(" ", "", "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tokens

        if (keywords != other.keywords) return false

        return true
    }

    override fun hashCode(): Int {
        return keywords.hashCode()
    }
}