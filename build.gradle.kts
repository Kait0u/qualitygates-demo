plugins {
	java
	id("org.springframework.boot") version "3.5.11"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "6.0.1.5171"
	id("com.adarshr.test-logger") version "4.0.0"
	id("org.cyclonedx.bom") version "1.10.0"
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
// CycloneDX SBOM — generates build/reports/bom.json for Trivy to scan
// ---------------------------------------------------------------------------

tasks.cyclonedxBom {
	setIncludeConfigs(listOf("runtimeClasspath"))
	destination = file("${layout.buildDirectory.get()}/reports")
	outputName = "bom"
	outputFormat = "json"
}

