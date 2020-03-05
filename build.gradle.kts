import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.0")
    implementation("io.vertx:vertx-core:3.8.5")
    implementation("io.vertx:vertx-web:3.8.5")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    testImplementation("junit:junit:4.12")
    testImplementation("io.vertx:vertx-unit:3.8.5")
    testImplementation("io.rest-assured:rest-assured:4.2.0")
}

val mainClass = "io.vertx.core.Launcher"
val mainVerticle = "pack.web.AppController"
application {
    mainClassName = mainClass
}

tasks {
    getByName<JavaExec>("run") {
        args = listOf("run", mainVerticle)
    }

    withType<ShadowJar> {
//        classifier = "fat"
        manifest {
            attributes["Main-Class"] = mainClass
            attributes["Main-Verticle"] = mainVerticle
        }
    }
}
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = mainClass
        attributes["Main-Verticle"] = mainVerticle
    }
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
