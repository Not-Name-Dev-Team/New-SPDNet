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
    implementation("com.alibaba.fastjson2:fastjson2:2.0.48")

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

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
    
    manifest {
        attributes(
            "Main-Class" to "me.catand.spdnetserver.SpdNetServerApplication"
        )
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val libDir = "lib"
val distributionDir = layout.buildDirectory.dir("distribution")

tasks.register<Copy>("copyDependencies") {
    group = "build"
    description = "复制所有依赖到 lib 目录"
    
    from(configurations.runtimeClasspath.get())
    into(distributionDir.map { it.dir(libDir) })
}

tasks.register<Copy>("copyJar") {
    group = "build"
    description = "复制主 jar 到 distribution 目录"
    
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile)
    into(distributionDir)
}

tasks.register("createStartScripts") {
    group = "build"
    description = "创建启动脚本"
    
    dependsOn("copyJar", "copyDependencies")
    
    doLast {
        val distDir = distributionDir.get().asFile
        val jarName = "${project.name}-${project.version}.jar"
        
        val classpath = File(distDir, libDir).listFiles()
            ?.filter { it.extension == "jar" }
            ?.joinToString(File.pathSeparator) { "$libDir/${it.name}" }
            ?: "$libDir/*"
        
        val winScript = """
@echo off
cd /d "%~dp0"
java -cp "$jarName;$classpath" me.catand.spdnetserver.SpdNetServerApplication
pause
        """.trimIndent()
        
        val unixScript = """
#!/bin/bash
cd "$(dirname "$0")"
java -cp "$jarName:lib/*" me.catand.spdnetserver.SpdNetServerApplication
        """.trimIndent()
        
        File(distDir, "start.bat").writeText(winScript)
        val unixFile = File(distDir, "start.sh")
        unixFile.writeText(unixScript)
        unixFile.setExecutable(true)
    }
}

tasks.register("buildDistribution") {
    group = "build"
    description = "构建分发包（主jar + lib目录 + 启动脚本）"
    
    dependsOn("copyJar", "copyDependencies", "createStartScripts")
}
