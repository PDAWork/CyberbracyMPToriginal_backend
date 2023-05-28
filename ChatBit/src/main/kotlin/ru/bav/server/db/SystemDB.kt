package ru.bav.server.db

import ObRequires
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.bav.entry.Main
import ru.bav.server.Server
import ru.bav.server.db.req.*
import ru.bav.server.db.schedule.DaySlot
import ru.bav.server.db.schedule.Month
import ru.bav.server.db.schedule.Schedule
import ru.bav.server.schemas.ParseSite
import ru.bav.server.schemas.Schedules
import ru.beenaxis.uio.io.DataMap
import ru.beenaxis.uio.io.Files
import ru.beenaxis.uio.io.MapSerializable
import java.io.File

object SystemDB : MapSerializable {

    private val file:File = File("${Main.getJarFolder()}/org.db.bin")
    val orgs:MutableList<Organization> = mutableListOf()
    var consultants:MutableList<Long> = mutableListOf()

    fun getFreeConsultant(from:Long) : Long? {
        val free = mutableListOf<Long>()
        consultants.shuffled().forEach {
            val usr = Server.cache.getCachedOrLoad(it) ?: return@forEach
            if(!usr.isBusyAt(from)){
                free += usr.dbId
            }
        }
        return free.randomOrNull()
    }

    fun byLowName(lowName:String) : Organization? = orgs.firstOrNull { it.lowName == lowName }

    fun write(){
        Files.writeData(file, this)
    }

    fun read(){
        if(file.exists().not())return
        orgs.clear()
        Files.readData(file, this)
    }

    fun loadFromJson(slots:Schedules, requires:ObRequires){
        orgs.clear()
        val knos = Json.decodeFromString<ParseSite.Knos>(File("${Main.getJarFolder()}/outknos.json").readText())
        fun bytitle(title:String) : ParseSite.Kno? {
            return knos.items.firstOrNull {
                it.title.equals(title, true) || it.act.uppercase().contains(title.uppercase())
            }
        }
        requires.organs.forEach {
            val schRef = it.sOrg
            val org = Organization()
            org.lowName = schRef.lowName
            org.name = schRef.name
            val kno = bytitle(org.origName)
            if(kno != null){
                org.headImageSrc = kno.imgSrc
                org.headFio = kno.fio
                org.activity = kno.act
                org.positionHead = kno.pos
                kno.npas.forEach {
                    org.npas += NPAItem().apply {
                        this.text = it.text
                        this.date = it.date
                        this.fileInfo = it.fileInfo
                    }
                }
                kno.infos.forEach { info->
                    val cInfo = CommonInfo()
                    cInfo.caption = info.label
                    info.payload.forEach {
                        cInfo.items += it
                    }
                    org.commonInfo += cInfo
                }
            }else{
                println("KNO FOR ${org.origName} is NULL!")
            }

            val schedule = Schedule(org)
            schedule.subOrganizations += schRef.descs
            schRef.months.forEach { it ->
                val month = Month(org)
                month.name = it.name
                it.days.forEach {
                    val sl = DaySlot(month)
                    sl.range = it.range
                    sl.dayTimestamp = it.timestamp
                    month.days += sl
                }
                schedule.availableMonths += month
            }
            org.schedule = schedule
            val types = ControlTypes(org)
            org.controlTypes = types
            orgs += org
            var ci = 0
            it.sects.forEach {
                val cType = ControlType(org)
                cType.fileName = it.name
                cType.name = schRef.descs[ci]
                types.controlTypes += cType
                it.files.forEach {
                    val nm = it.nm
                    var reqList:RequireList? = null
                    nm.requirements.forEach {
                        if(it.headerSection != null){
                            reqList = RequireList(cType)
                            reqList!!.header = it.header!!
                            reqList!!.section = it.headerSection!!
                            cType.requiresSections += reqList!!
                        }
                        val require = Require(cType)
                        require.section = it.nameSection
                        require.checkMethod = it.checkMethod
                        require.requireName = it.name
                        require.typeReq = it.typeReq
                        require.ableToGetKNO = it.ableToGetKNO
                        require.lifetimeDocuments = it.lifetimeDocuments
                        require.typeOfPunish = it.typeOfPunish
                        require.organWithRightsToPunish = it.organWithRightsToPunish
                        try {
                            it.stepsToPrivlichenie?.let {
                                if(it.isBlank()){
                                    return@let
                                }
                                val split = it.split(" (")
                                if(split.size > 1){
                                    require.stepsToPrivlichenie = split[0]
                                    val id = split[1].replace(")", "")
                                    require.stepsToPrivlichenieLink = "https://knd.mos.ru/api/files/${id}"
                                }else{
                                    require.stepsToPrivlichenie = it
                                }
                            }
                        }catch (e:Exception){
                            println("Error: ${it.stepsToPrivlichenie}")
                        }
                        //require.stepsToPrivlichenie = it.stepsToPrivlichenie
                        require.typeOfDeyatelnostSubjectControl = it.typeOfDeyatelnostSubjectControl
                        require.utochnenieVidovDoing = it.utochnenieVidovDoing
                        require.totalQuestion = it.totalQuestion
                        require.additionQuestion = it.additionQuestion
                        require.businessTotalQuestion = it.businessTotalQuestion
                        require.businessAdditionQuestion = it.businessAdditionQuestion
                        it.accepts.forEach {
                            val check = Check()
                            check.ability = it.ability
                            check.orgWithInfo = it.orgWithInfo
                            check.documentAccepts = it.documentAccepts
                            check.type = it.type
                            require.checkers += check
                        }
                        it.targetsNPA.forEach {
                            val npa = NPA()
                            npa.from = it.from
                            npa.to = it.to
                            npa.desc = it.desc
                            require.targetsNPA += npa
                        }
                        it.punishments.forEach {
                            val punish = Punish()
                            punish.right = it.zakon
                            var cur:Punish.PunishType? = null
                            it.items.forEach {
                                val pI = Punish.PunishItem()
                                if(it.type.isNotBlank()){
                                    cur = Punish.PunishType()
                                    cur!!.type = it.type
                                    punish.items += cur!!
                                }
                                pI.header = it.header
                                pI.amount = it.amount
                                pI.vidNormy = it.vidNormy
                                cur!!.items += pI
                            }
                            require.punishments += punish
                        }
                        reqList!!.requires += require
                    }
                }
                ci++
            }
        }
    }

    override fun dataDeserialize(map: DataMap) {
        map.readMapList("orgs", orgs){ Organization() }
        consultants = map.readList<Long>("consultants")?.toMutableList() ?: mutableListOf()
    }

    override fun dataSerialize(map: DataMap) {
        map.writeMapList("orgs", orgs)
        map.writeList("consultants", consultants)
    }

}