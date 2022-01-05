
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.kotlin.dsl.sqldelight


buildscript {
    repositories {

        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }




}

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("kapt") version "1.5.31"
    id("com.squareup.sqldelight")
}

group = "me.uj347"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}
sqldelight {
    this.
    database("H2Database") {
        sourceFolders= listOf("sqldelighth2db")
        packageName ="com.uj.rcbackend.database"
        dialect = "mysql"
    }

}




dependencies {
    implementation("com.h2database:h2:2.0.202")
    implementation ("mysql:mysql-connector-java:8.0.26")
    implementation ("com.squareup.sqldelight:jdbc-driver:1.5.3")
    testImplementation(kotlin("test"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jmdns:jmdns:3.5.7")
    implementation("io.netty:netty-all:4.1.68.Final")
    implementation ("com.google.dagger:dagger:2.39.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    kapt ("com.google.dagger:dagger-compiler:2.39.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("commons-io:commons-io:2.9.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.6.1")
    implementation("org.apache.logging.log4j:log4j-core:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("io.github.hakky54:sslcontext-kickstart:7.0.2")
    implementation("io.github.hakky54:sslcontext-kickstart-for-pem:7.0.2")
    implementation("org.apache.commons:commons-compress:1.21")
    testImplementation("org.mockito:mockito-inline:4.1.0")
    testImplementation("org.mockito:mockito-junit-jupiter:2.23.0")
    implementation("com.zaxxer:HikariCP:5.0.0")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.1.1")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")

}

tasks.test {
    useJUnit()
}


tasks.processResources {
    duplicatesStrategy=org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

sourceSets {
    main {
        resources {
            srcDir ("/src/main/resources")
        }
    }
}

    tasks.jar {
        manifest {
            attributes["Main-Class"] = "com.uj.rcbackend.UjMain"

        }
//        destinationDirectory.set(File("c:\\ujtrash\\"))
//        archiveFileName.set("testone.jar")
        configurations["compileClasspath"].forEach { file: File ->
            from(zipTree(file.absoluteFile))
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }




tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}