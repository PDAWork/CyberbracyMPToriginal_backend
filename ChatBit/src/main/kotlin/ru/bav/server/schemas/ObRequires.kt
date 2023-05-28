import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.bav.server.schemas.Schedules
import java.io.File

@Serializable
class ObRequires {
    @Transient
    lateinit var folder: File

    constructor(folder:File){
        this.folder = folder
    }
    constructor()

    val organs:MutableList<ObOrgan> = mutableListOf()

    @Serializable
    class ObOrgan(val name:String, val sects:MutableList<ObSection>){
        @Transient
        @kotlin.jvm.Transient
        lateinit var sOrg:Schedules.ScheduleOrg
    }
    @Serializable
    class ObSection(val name:String, val files:MutableList<NormativesFile>)
}