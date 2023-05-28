package ru.bav.server.user

enum class Role {
    USER, CONSULTANT;

    companion object {
        fun byName(s:String) : Role? {
            return values().firstOrNull { it.name.equals(s, true) }
        }
    }
}