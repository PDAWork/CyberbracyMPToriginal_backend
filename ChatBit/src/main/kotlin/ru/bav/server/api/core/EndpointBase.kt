package ru.bav.server.api.core

import io.javalin.Javalin
import io.javalin.http.Context
import ru.bav.server.Server
import ru.bav.server.user.User
import kotlin.reflect.KCallable

open class EndpointBase {

    class Inst(val bind: IEndpoints, val path:String, val func:KCallable<*>, val type:String, val desc:String){

        fun getParamsNullable() : List<String> {
            val paramsErr = mutableListOf<String>()
            func.parameters.forEach {
                if(it.name == null)return@forEach
                paramsErr += it.name + if(it.type.isMarkedNullable) "?" else ""
            }
            return paramsErr
        }

        fun getParams() : Map<String, String> {
            val paramsErr = mutableMapOf<String, String>()
            func.parameters.forEach {
                val paramName = it.name ?: return@forEach
                paramsErr[paramName] = it.type.classifier.toString() + if(it.type.isMarkedNullable) "?" else ""
            }
            return paramsErr
        }

        fun bind(app: Javalin){
            println("BIND ${bind.javaClass.simpleName}")
            when(type){
                "get"->{
                    println("GET ${path}")
                    app.get(path){
                        tryInvoke(it)
                    }
                }
                "post"->{
                    println("POST ${path}")
                    app.post(path){
                        tryInvoke(it)
                    }
                }
            }
        }

        fun tryInvoke(ctx: Context) : Boolean {
            try {
                val build = mutableListOf<Any?>()
                //build += bind
                func.parameters.forEach {
                    val paramName = it.name ?: return@forEach
                    if(!it.type.isMarkedNullable){
                        if (!ctx.queryParamMap().containsKey(paramName)) {
                            ctx.status(404)
                            val list = mutableListOf<String>()
                            list.add("Требуется параметр ${paramName}!")
                            list.add("Все параметры: ")
                            getParams().forEach { (t, u)->
                                list.add(" [$u => ${t}]")
                            }
                            ctx.result(list.joinToString("\n", "", ""))
                            /*ctx.result("Not found parameter $paramName\nRequires: ${func.parameters.subList(1, func.parameters.size).map { it.type.classifier}}")*/
                            return false
                        }
                    }

                    if (it.type.classifier == Int::class) {
                        build += ctx.queryParam(paramName)?.toIntOrNull()
                    } else if (it.type.classifier == Long::class) {
                        build += ctx.queryParam(paramName)?.toLongOrNull()
                    } else if (it.type.classifier == String::class) {
                        build += ctx.queryParam(paramName)
                    } else if (it.type.classifier == Boolean::class) {
                        build += ctx.queryParam(paramName)?.toBooleanStrictOrNull()
                    } else if (it.type.classifier == Float::class) {
                        build += ctx.queryParam(paramName)?.toFloatOrNull()
                    } else {
                        build += ctx.queryParam(paramName)
                    }
                }
                println("USER/IN -> [${type.uppercase()}] ${path}(${build.joinToString(", ", "", "")}})")
                val toRet = func.call(bind, *build.toTypedArray())
                if(toRet is ErrOut){
                    ctx.status(toRet.status)
                    ctx.result("Ошибка: ${toRet.msg}")
                    println("USER/ERROR ${toRet.msg}")
                    return true
                }
                println("USER/OUT <- ${toRet}")
                ctx.json(toRet ?: "Null")
                return true
            }catch (e:Exception){
                ctx.result(e.stackTraceToString())
                return false
            }
        }
    }

    val insts = mutableListOf<Inst>()

    fun user(ctx:Context, id:Long) : User? {
        return Server.cache.getCachedOrLoad(id) ?: let { ctx.result("No result"); return null}
    }

    fun loadEndpoints(iEndpoints: IEndpoints){
        iEndpoints::class.members.forEach {
            val post = it.annotations.firstOrNull {
                it is Post
            } as? Post
            val get = it.annotations.firstOrNull {
                it is Get
            } as? Get
            val desc = it.annotations.firstOrNull {
                it is Description
            } as? Description
            if(post != null && get != null)throw IllegalArgumentException("Не надо GET и POST одновременно")
            val descOut = desc?.desc ?: "Нет документации :("
            if(post != null){
                insts += Inst(iEndpoints, iEndpoints.basePath + post.path, it, "post", descOut)
            }
            if(get != null){
                insts += Inst(iEndpoints, iEndpoints.basePath + get.path, it, "get", descOut)
            }
        }
    }

    fun bind(app:Javalin){
        insts.forEach { it.bind(app) }
    }
}