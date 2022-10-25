tasks.register<Zip>("zipTestSuite") {
    from(file("testsuite"))
    archiveFileName.set("testsuite.zip")
}

tasks.processResources {
    from(tasks["zipTestSuite"])
}
