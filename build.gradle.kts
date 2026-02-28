plugins {
	java
	id("org.springframework.boot") version "3.5.11"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "5.0.0.4638"
	id("org.owasp.dependencycheck") version "9.2.0"
	id("com.adarshr.test-logger") version "4.0.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("commons-collections:commons-collections:3.2.1") // CVE-2015-6420, CVSS 7.5
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	outputs.upToDateWhen { false }
}

// ---------------------------------------------------------------------------
// Test Logger — rich terminal output per test (theme options: PLAIN, STANDARD,
// PLAIN_PARALLEL, STANDARD_PARALLEL, MOCHA, MOCHA_PARALLEL)
// ---------------------------------------------------------------------------

testlogger {
	theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
	showExceptions = true
	showCauses = true
	slowThreshold = 2000
	showSummary = true
	showPassed = true
	showSkipped = true
	showFailed = true
	showStandardStreams = false
	showFailedStandardStreams = true
}

// ---------------------------------------------------------------------------
// JaCoCo
// ---------------------------------------------------------------------------

jacoco {
	toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
		html.required = true
	}
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.jacocoTestReport)
	violationRules {
		rule {
			limit {
				minimum = "0.80".toBigDecimal()
			}
		}
	}
}

// ---------------------------------------------------------------------------
// SonarCloud
// ---------------------------------------------------------------------------

sonar {
	properties {
		property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY") ?: "YOUR_SONAR_PROJECT_KEY")
		property("sonar.organization", System.getenv("SONAR_ORG") ?: "YOUR_SONAR_ORG")
		property("sonar.host.url", "https://sonarcloud.io")
		property(
			"sonar.coverage.jacoco.xmlReportPaths",
			"${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml"
		)
	}
}

tasks.sonar {
	dependsOn(tasks.jacocoTestReport)
}

// ---------------------------------------------------------------------------
// OWASP Dependency-Check — fail on CVSS >= 7.0 (High)
// ---------------------------------------------------------------------------

dependencyCheck {
	failBuildOnCVSS = 7.0f
	formats = listOf("HTML", "XML")
	analyzers.assemblyEnabled = false
}
