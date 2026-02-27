plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "me.catand"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/spring/")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    // Shiro
    implementation("com.mikuac:shiro:2.3.7")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.11.Final")

    // Fastjson
    implementation("com.alibaba:fastjson:2.0.48")

    // Socket.io
    implementation("com.corundumstudio.socketio:netty-socketio:2.0.8")

    // email
    implementation("javax.mail:mail:1.4.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
