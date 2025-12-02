import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.loadingking"
version = "0.0.1-SNAPSHOT"
description = "loading_king"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

configure<SpotlessExtension> {
    java {
        // 적용 대상 파일 지정
        target("**/*.java")

        //  기타 규칙
        removeUnusedImports() // 사용하지 않는 import 제거
        trimTrailingWhitespace() // 각 줄 끝의 공백 제거
        endWithNewline() // 파일 끝에 개행 문자 추가

        // (선택) import 순서 정의: java -> javax -> jakarta -> org -> com -> 나머지 -> static
        importOrder("java", "javax", "jakarta", "org", "com", "", "\\#")
    }
}

dependencies {
    // Core
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spatial (GIS)
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.locationtech.jts:jts-core:1.18.2")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2") // 테스트용

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // === 테스트 관련 (Spring Boot 4.0.0 호환) ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // 필요 시 JUnit 4 제외 (기본적으로 JUnit 5 사용)
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    // JPA 테스트를 위한 명시적 의존성 추가
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.named("compileJava") {
    dependsOn("spotlessApply")
}
