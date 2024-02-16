import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.graalvm.buildtools.native") version "0.9.28"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.22"

    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.noarg") version "1.9.22"
}

group = "org.javamaster"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    maven(url = "https://maven.aliyun.com/nexus/content/groups/public/")
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.data:spring-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.mybatis:mybatis:3.5.6")
    implementation("org.eclipse.aether:aether-connector-basic:1.0.2.v20150114")
    implementation("org.eclipse.aether:aether-transport-wagon:1.0.2.v20150114")
    implementation("org.apache.maven.wagon:wagon-http:2.9")
    implementation("org.apache.maven.wagon:wagon-provider-api:2.9")
    implementation("org.apache.maven.wagon:wagon-http-lightweight:2.9")
    implementation("org.apache.maven:maven-embedder:3.3.3")
    implementation("com.alibaba:easyexcel:2.2.3")
    implementation("com.alibaba:dubbo:2.5.6")
    implementation("com.alibaba:fastjson:1.2.73")
    implementation("com.alibaba:druid:1.2.16")
    implementation("redis.clients:jedis:5.1.0")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("org.apache.commons:commons-dbcp2:2.7.0")
    implementation("org.apache.zookeeper:zookeeper:3.9.1")
    implementation("com.github.sgroschupf:zkclient:0.1")
    implementation("com.caucho:hessian:4.0.38")
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("com.itextpdf:itextpdf:5.5.13.3")
    implementation("com.itextpdf:html2pdf:4.0.3")
    implementation("com.itextpdf:itext-asian:5.2.0")
    implementation("com.itextpdf.tool:xmlworker:5.5.13.3")
    implementation("org.jetbrains:annotations:24.0.1")
    runtimeOnly("mysql:mysql-connector-java:5.1.49")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.jcraft:jsch:0.1.54")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

noArg {
    annotation("org.javamaster.invocationlab.admin.annos.NoArg")
}

allOpen {
    annotation("org.javamaster.invocationlab.admin.annos.AllOpen")
}