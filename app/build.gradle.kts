plugins {
    application
    kotlin("jvm")
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.guava)
    implementation(libs.picocli)
    implementation(libs.jspecify)
    implementation(libs.sqlite.jdbc)
    implementation(libs.dotenv.java)
    implementation(libs.guice)

    annotationProcessor(libs.picocli.codegen)

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

application {
    mainClass = "com.ctntc.flock.App"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") { workingDir = rootProject.projectDir }

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
