plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok")
}

group = "me.catand"
// SPDNet: 此版本号仅用于产物标识（决定 jar 名 server-<version>.jar 与 start.sh 的 -cp），固定为 0.0.1 不改。
// 协议门禁是另一回事，见 application.yml 的 spd.netVersion——客户端握手版本必须与它一致，改门禁请只改那一处。
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

// SPDNet: 共享协议源目录（Actions/Events 单一事实来源，与客户端 core 共用）
sourceSets {
    main {
        java {
            srcDir(rootDir.resolve("spdnet-protocol/src"))
        }
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
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:7.3.0.CR2")

    // Fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.61")

    // Socket.io
    implementation("com.corundumstudio.socketio:netty-socketio:2.0.14")

    // BCrypt for password encoding
    implementation("org.springframework.security:spring-security-crypto:7.0.3")

    // JWT (jjwt): 用于管理员接口鉴权
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// SPDNet: 前端构建接入。
// server/web 是独立的 Vite 工程，产物直接输出到 src/main/resources/static/。
// 此前没有任何构建任务驱动它，static/ 只能靠手工 npm run build 更新，
// 改了 .vue 却忘记构建就会静默发布旧页面。
val webDir = layout.projectDirectory.dir("web")
val webDistDir = layout.projectDirectory.dir("src/main/resources/static")

val isWindows = System.getProperty("os.name").lowercase().contains("windows")

// 依赖输入：仅在源码/配置变化时重跑 vite build
val webSourceFiles = fileTree(webDir) {
    include("src/**", "index.html", "vite.config.js", "package.json", "package-lock.json", "public/**")
    exclude("node_modules/**", "dist/**")
}

// 使用 node_modules/.bin 下的本地可执行文件，避免依赖全局 PATH
val npmExecutable = if (isWindows) "npm.cmd" else "npm"

val installWebDeps by tasks.registering(Exec::class) {
    group = "build"
    description = "安装前端依赖（node_modules 不存在时）"
    workingDir = webDir.asFile
    commandLine(npmExecutable, "install")
    // node_modules 已存在时跳过，避免每次构建都跑一遍 install
    onlyIf { !webDir.dir("node_modules").asFile.exists() }
}

val buildWeb by tasks.registering(Exec::class) {
    group = "build"
    description = "构建前端并输出到 src/main/resources/static"
    dependsOn(installWebDeps)
    workingDir = webDir.asFile
    commandLine(npmExecutable, "run", "build")
    inputs.files(webSourceFiles).withPropertyName("webSources")
    outputs.dir(webDistDir).withPropertyName("webDist")
}

// 打包前确保前端产物是最新的
tasks.named("processResources") {
    dependsOn(buildWeb)
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
