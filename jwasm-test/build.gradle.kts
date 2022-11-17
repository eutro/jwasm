fun computeManifest(file: File, entries: MutableList<File>): MutableList<File> {
    if (file.isFile) {
        entries.add(file)
    } else {
        file.listFiles()!!
            .filter { !it.name.startsWith(".") }
            .sortedWith(compareBy({ it.isFile }, { it.name }))
            .forEach { computeManifest(it, entries) }
    }
    return entries
}

tasks.processResources {
    val suiteDir = file("testsuite")
    into("testsuite") {
        from(
            resources.text.fromString(computeManifest(suiteDir, mutableListOf())
                .joinToString(separator = "\n") { it.toRelativeString(suiteDir) })
                .asFile("utf-8")
        ) {
            rename { "manifest.txt" }
        }
        from(suiteDir)
    }
}
