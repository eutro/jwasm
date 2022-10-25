dependencies {
    implementation(project(":jwasm"))
    implementation(project(":jwasm-attrs"))
    implementation(project(":jwasm-tree"))
    testImplementation(project(":jwasm-analysis"))
}

tasks.test {
    environment("WASM_TESTSUITE" to file("./testsuite"))
}
