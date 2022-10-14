dependencies {
    implementation(project(":jwasm"))
    implementation(project(":jwasm-tree"))
}

tasks.test {
    environment("WASM_TESTSUITE" to file("./testsuite"))
}
