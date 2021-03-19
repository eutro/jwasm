dependencies {
    implementation("org.ow2.asm:asm:9.1")
    implementation("org.ow2.asm:asm-commons:9.1")
    implementation("org.ow2.asm:asm-analysis:9.1")
    implementation("org.ow2.asm:asm-util:9.1")
    implementation(project(":jwasm-tree"))
}

sourceSets {
    create("runtime")
}
