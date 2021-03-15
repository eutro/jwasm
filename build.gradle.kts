subprojects {
    apply<JavaLibraryPlugin>()

    group = "io.github.eutro.jwasm"
    version = "${properties["ver_major"]}.${properties["ver_minor"]}.${properties["ver_patch"]}"

    extensions.configure<JavaPluginExtension>("java") {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.6.2")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
        "testImplementation"(project(":jwasm-test"))
        if (path != ":jwasm") {
            "implementation"(project(":jwasm"))
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}

project(":jwasm-test") {
    dependencies {
        "implementation"("org.junit.jupiter:junit-jupiter-api:5.6.2")
    }
}
