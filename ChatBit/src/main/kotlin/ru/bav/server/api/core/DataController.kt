package ru.bav.server.api.core

import ru.bav.server.api.CheckMethod
import ru.bav.server.api.EndpointModels
import ru.bav.server.dayRange
import ru.bav.server.db.SystemDB
import ru.bav.server.db.schedule.DaySlot
import ru.bav.server.withFirstBigChar

class DataController : IEndpoints {
    override val basePath: String
        get() = "/data"

    @Get("/org/npas")
    @Description("Получить НПА организации")
    fun getOrgNpas(lowName: String) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        return org.npas
    }

    @Get("/org/schedule/actual")
    @Description("Получить акутальные даты записи")
    fun getScheduleActual(lowName:String, dayLong:Long) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        val (from, to) = dayRange(dayLong)
        val available = mutableListOf<DaySlot>()
        org.schedule.availableMonths.forEach {
            it.days.forEach a@{slot->
                if(slot.idToConsultUser != null)return@a
                if(slot.localDateTimeFrom in from .. to){
                    available += slot
                }
            }
        }
        return available
    }

    @Get("/org/requires/body")
    @Description("Получить всю инфу о конкретном требовании")
    fun getRequireBody(lowName:String, idControl:Int, idRequire:Int) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        val typeControl = org.controlTypes.controlTypes.getOrNull(idControl) ?: return ErrOut("ControlType ${idControl} not found!")
        val require = typeControl.allRequires.getOrNull(idRequire) ?: return ErrOut("Require ${idRequire} not found!")
        val out = EndpointModels.RequireBody()
        out.requireName = require.requireName
        out.typeControl = require.controlType.name
        out.knoTitle = require.controlType.kno.name
        out.typeOfDeyatelnostSubjectControl = require.typeOfDeyatelnostSubjectControl ?: ""
        out.lifetimeDocuments = require.lifetimeDocuments ?: "Null"
        out.checkMethod = CheckMethod.from(require.checkMethod?.toIntOrNull() ?: 0)
        out.punishments = require.punishments
        out.checks = require.checkers
        out.NPAs = require.targetsNPA
        out.activities = require.formatedActivites
        out.fileLink = require.stepsToPrivlichenieLink ?: "null"
        out.punishType = require.typeOfPunish?.withFirstBigChar() ?: "null"
        out.organ = require.organWithRightsToPunish ?: "null"
        return out
    }

    @Get("/org/requires/list")
    @Description("Получить список требований")
    fun getRequiresList(lowName:String, idControl:Int) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        val typeControl = org.controlTypes.controlTypes.getOrNull(idControl) ?: return ErrOut("Org not found!")
        val list = mutableListOf<EndpointModels.RequireItem>()
        var idLocal = 0
        typeControl.allRequires.forEach {
            list += EndpointModels.RequireItem().apply {
                this.idRequire = idLocal
                this.name = it.requireName
                this.typeControl = typeControl.name
                this.activities += it.formatedActivites
                this.responsibility = it.formatTypeOfPunish
            }
            idLocal++
        }
        return list
    }

    @Get("/org/typec/list")
    @Description("Получить список вид КНО")
    fun getTypeControlsList(lowName:String) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        val list = mutableListOf<EndpointModels.ControlItem>()
        var idLocal = 0
        org.controlTypes.controlTypes.forEach {
            list += EndpointModels.ControlItem().apply {
                this.name = it.name
                this.idTypeControl = idLocal //Для быстрого получения последующей информации
                this.count = it.requiresSections.sumOf { it.requires.size }
            }
            idLocal++
        }
        return list
    }

    @Get("/org/head")
    @Description("Получить главу КНО")
    fun getHeadKNO(lowName:String) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org not found!")
        val obj = EndpointModels.Head()
        obj.fio = org.headFio
        obj.act = org.activity
        obj.pos = org.positionHead
        obj.imageUrl = org.headImageSrc
        obj.commonInfo = org.commonInfo
        return obj
    }

    @Get("/org/fullinfo")
    @Description("Получить всю инфу о КНО")
    fun getAllKNO(lowName:String) : Any {
        val org = getOrg(lowName) ?: return ErrOut("No kno!")
        return org
    }

    @Get("/org/list")
    @Description("Получить список КНО")
    fun getKNOList() : Any {
        val ls = mutableListOf<EndpointModels.OrgItem>()
        SystemDB.orgs.forEach {
            ls += EndpointModels.OrgItem().apply {
                this.name = it.origName
                this.lowName = it.lowName
                this.typeControls = it.controlTypes.controlTypes.size
                this.requirements = it.totalRequires
                this.NPAs = it.totalNPAs
            }
        }
        return ls
    }

    @Get("/org/schedule/list")
    @Description("Получить все даты")
    fun getAllDates(lowName:String) : Any {
        val org = getOrg(lowName) ?: return ErrOut("Org is null!")
        return org.schedule.availableMonths.map { it.days }
    }
}