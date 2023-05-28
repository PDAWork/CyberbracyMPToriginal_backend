import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    class ObOrgan(val name:String, val sects:MutableList<ObSection>)
    @Serializable
    class ObSection(val name:String, val files:MutableList<NormativesFile>)

    fun read(){
        folder.list()?.forEach {mainFolder->
            val sects = mutableListOf<ObSection>()
            val organ = ObOrgan(mainFolder, sects)
            val subFolder = File("${folder.absolutePath}/${mainFolder}")
            subFolder.list()?.forEach { sub->
                val files = mutableListOf<NormativesFile>()
                val obSection = ObSection(sub, files)
                val otFiles = File("${folder.absolutePath}/${mainFolder}/${sub}")
                otFiles.walkTopDown().forEach {
                    if(it.extension == "xlsx"){
                        val file = NormativesFile(it)
                        file.read()
                        files += file
                        println("Loaded! ${mainFolder}/${sub}/${it.name}")
                    }
                }
                sects += obSection
            }
            organs += organ
        }
    }
}