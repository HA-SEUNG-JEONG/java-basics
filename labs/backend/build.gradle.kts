plugins {
    // 21~23주차(Spring) 프로젝트가 같은 버전을 쓰도록 여기서 한 번만 선언합니다.
    id("org.springframework.boot") version "3.5.16" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// 17~20주차 공통 설정. 21~23주차(Spring)는 각 폴더의 build.gradle.kts 를 봅니다.
subprojects {
    if (name in listOf("week17", "week18", "week19", "week20")) {
        apply(plugin = "java")

        repositories { mavenCentral() }

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }

        dependencies {
            "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
            "testImplementation"("org.junit.jupiter:junit-jupiter")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events("passed", "failed")
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
            }
        }
    }
}
