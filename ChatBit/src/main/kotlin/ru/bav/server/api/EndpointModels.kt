package ru.bav.server.api

import ru.bav.server.db.*
import ru.bav.server.db.req.Check
import ru.bav.server.db.req.NPA
import ru.bav.server.db.req.Punish

object EndpointModels {

    class Head {
        var fio:String = ""
        var pos:String = ""
        var act:String = ""
        var imageUrl:String = ""
        var commonInfo = mutableListOf<CommonInfo>()
    }

    class OrgItem {
        var name:String = ""
        var lowName:String = ""
        var typeControls:Int = 0
        var requirements:Int = 0
        var NPAs:Int = 0
    }

    class ControlItem {
        var idTypeControl:Int = 0
        var name:String = ""
        var count:Int = 0
    }

    class RequireItem {
        var idRequire:Int = 0
        var name:String = ""
        var typeControl:String = ""
        var activities:MutableList<Activity> = mutableListOf()
        var responsibility:String = ""
    }

    class RequireBody {
        var requireName:String = ""
        var typeOfDeyatelnostSubjectControl:String = ""
        var typeControl:String = ""
        var knoTitle:String = ""
        var lifetimeDocuments:String = ""
        var checkMethod:String = ""
        var punishments:MutableList<Punish> = mutableListOf()
        var checks:MutableList<Check> = mutableListOf()
        var activities:MutableList<Activity> = mutableListOf()
        var NPAs:MutableList<NPA> = mutableListOf()
        var fileLink:String = ""
        var organ = ""
        var punishType = ""
    }
}