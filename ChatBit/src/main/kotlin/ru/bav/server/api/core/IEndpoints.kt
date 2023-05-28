package ru.bav.server.api.core

import ru.bav.server.db.SystemDB
import ru.bav.server.db.Organization

interface IEndpoints {
    val basePath:String

    fun getOrg(lowName:String) : Organization? = SystemDB.byLowName(lowName)
}