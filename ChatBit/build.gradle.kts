import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "ru.bav.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("C:\\MiniGames\\outputs\\UIO.jar"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha3")
    implementation("org.jline:jline:3.20.0")
    implementation("io.javalin:javalin:4.0.1")

    implementation( "org.jsoup:jsoup:1.14.3")

    implementation( "io.gsonfire:gson-fire:1.8.5")
    implementation( "com.google.code.gson:gson:2.9.0")
    implementation( "com.squareup.okhttp3:okhttp:4.9.3")
    implementation( "com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation( "org.openapitools:jackson-databind-nullable:0.2.2")

    // https://mvnrepository.com/artifact/com.onesignal/OneSignal
    implementation(files("C:\\Users\\beena\\Desktop\\onesignal-java-api-main\\build\\libs\\onesignal-java-client-1.2.1.jar"))


    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

tasks.withType<Jar>{
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    configurations["runtimeClasspath"].forEach { file->
        if(file.name.contains("kotlin"))return@forEach
        //println(file.absolutePath)
        from(zipTree(file.absoluteFile))
    }
    manifest{
        archiveBaseName.set("Server")
        archiveVersion.set("")
        attributes(mapOf(
            "Main-Class" to "ru.bav.entry.Main",
        ))
    }
    destinationDir = file("D:\\hackaton")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}