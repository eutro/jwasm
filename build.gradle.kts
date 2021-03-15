plugins {
    `java-library`
}

val libVersion = "${properties["ver_major"]}.${properties["ver_minor"]}.${properties["ver_patch"]}"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

sourceSets {
    val tree = create("tree") {
        compileClasspath += main.get().output
        runtimeClasspath += main.get().output
    }
    test {
        compileClasspath += tree.output
        runtimeClasspath += tree.output
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveVersion.set(libVersion)
}

tasks.register<Jar>("treeJar") {
    archiveAppendix.set("tree")
    archiveVersion.set(libVersion)
    from(sourceSets.named("tree").get().output)
}

tasks.build {
    dependsOn("treeJar")
}
