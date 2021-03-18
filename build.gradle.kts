plugins {
    `java-library`
}

tasks.jar {
    enabled = false
}

subprojects {
    apply<JavaLibraryPlugin>()

    group = "io.github.eutro.jwasm"
    version = "${properties["ver_major"]}.${properties["ver_minor"]}.${properties["ver_patch"]}"

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testImplementation(project(":jwasm-test"))
        implementation("org.jetbrains:annotations:20.1.0")
        if (path != ":jwasm") {
            implementation(project(":jwasm"))
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}

project(":jwasm-test") {
    dependencies {
        implementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    }
}

val javadocModules = listOf(":jwasm", ":jwasm-tree")

tasks.register<Copy>("assembleDocs") {
    val javadocTasks = javadocModules.map { project(it).tasks.javadoc.get() }.toTypedArray()
    dependsOn(*javadocTasks)
    doFirst {
        delete("docs")
    }
    javadocTasks.forEach { jdoc ->
        from(jdoc) {
            into(jdoc.project.name)
        }
    }
    into("docs")
}

defaultTasks("build", "assembleDocs")
