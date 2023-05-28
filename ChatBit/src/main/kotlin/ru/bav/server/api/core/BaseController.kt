package ru.bav.server.api.core

import ru.bav.server.Server
import ru.bav.server.api.Endpoints
import ru.bav.server.db.SystemDB
import ru.bav.server.db.Organization

class BaseController : IEndpoints {
    override val basePath: String
        get() = ""

    @Get("/root")
    @Description("Корень БД")
    fun root() : List<Organization> {
        return SystemDB.orgs
    }

    @Get("/info")
    @Description("Штука которая выводит инфу всю")
    fun info(entry:String?) : String {
        val text = mutableListOf<String>()
        text.add("Здесь инфа о всех эндпоинтах что есть в апи прямо сейчас.")
        text.add("Этот список динамический, аргументы в скобках")
        text.add(" ======== POST =========")
        fun inc(it: EndpointBase.Inst){
            val params = it.getParamsNullable()
            text.add("  @${it.desc}")
            val paramsOut = params.joinToString(", ", "", "")
            text.add("  [${it.type.uppercase()}] http://${entry ?: ""}:3077${it.path} (${paramsOut})")
            text.add("")
        }
        Endpoints.insts.filter { it.type == "post" }.forEach {
            inc(it)
        }
        text.add(" ======== GET =========")
        Endpoints.insts.filter { it.type == "get" }.forEach {
            inc(it)
        }
        text.add("Другая инфа ======")
        text.add("Юзеров в кэше: ${Server.cache.cache.size}x")
        text.add("Версия памяти контекста чатов: ${Server.memoryVersion}")
        return text.joinToString("\n", "", "")
    }
}