plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok")
}

group = "me.catand"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/spring/")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.11.Final")

    // Fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.61")

    // Socket.io
    implementation("com.corundumstudio.socketio:netty-socketio:2.0.8")

    // BCrypt for password encoding
    implementation("org.springframework.security:spring-security-crypto:6.4.4")

    // Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
