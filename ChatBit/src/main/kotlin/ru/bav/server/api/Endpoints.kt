package ru.bav.server.api

import io.javalin.Javalin
import io.javalin.http.Context
import ru.bav.server.api.core.BaseController
import ru.bav.server.api.core.DataController
import ru.bav.server.api.core.EndpointBase
import ru.bav.server.api.core.UserController

object Endpoints : EndpointBase(){

    val BAD_REQ = 400
    val CREATED = 201
    val OK = 200
    val ALREADY_HAS = 226

    val BASE = BaseController()
    val USER = UserController()
    val DATA = DataController()

    fun rightArgs(ctx:Context, vararg args:String) : Boolean {
        fun err(text:String){
            ctx.status(BAD_REQ)
            val map = mutableMapOf<String, String>()
            map["Ошибка"] = text
            map["Аргументы"] = args.joinToString(", ", "", "")

            ctx.json(map)
        }
        fun all(){
            ctx.json(args.map { "Требуется" to it })
            ctx.status(BAD_REQ)
        }
        args.forEach {
            val out = ctx.queryParamMap()[it]
            if(out == null){
                all()
                err("Требуется аргумент ${it}")
                return false
            }else{
                if(out.size > 1){
                    err("Может быть только один аргумент! ${it}")
                }
            }
        }
        return true
    }

    fun init(){
        val app = Javalin.create().start(3077)
        app.before {
            println("~${it.path()}")
        }
        loadEndpoints(BASE)
        loadEndpoints(USER)
        loadEndpoints(DATA)
        bind(app)
        //list.forEach { it.setup(app) }
    }
}