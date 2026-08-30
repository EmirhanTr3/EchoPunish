import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    kotlin("jvm") version "2.4.20-Beta2"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

group = "cat.emir"
version = "1.0.3"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    paperLibrary("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("net.luckperms:api:5.5")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.12.0")

    // for echolib
    implementation("cat.emir:EchoLib:1.2.0")
    paperLibrary("org.spongepowered:configurate-yaml:4.2.0")
    paperLibrary("org.spongepowered:configurate-extra-kotlin:4.2.0")
    paperLibrary("io.github.classgraph:classgraph:4.8.179")
    paperLibrary("com.h2database:h2:2.3.232")
    paperLibrary("com.zaxxer:HikariCP:7.0.2")
    paperLibrary("org.jetbrains.exposed:exposed-core:1.3.1")
    paperLibrary("org.jetbrains.exposed:exposed-jdbc:1.3.1")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
        downloadPlugins {
            modrinth("luckperms", "v5.5.17-bukkit")
        }
    }

    jar.get().enabled = false

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("cat.emir.echolib", "cat.emir.echopunish.echolib")
        relocate("com.github.stefvanschie.inventoryframework", "cat.emir.echopunish.inventoryframework")
        archiveClassifier = ""

        // remove all versions until 26.1.2 since we only support 26.1.2+
        setOf(
            "1_16_5",
            "1_17_1",
            "1_18_2",
            "1_19_4",
            "1_20_0", "1_20_1", "1_20_2", "1_20_3", "1_20_3-4", "1_20_5", "1_20_6",
            "1_21_0", "1_21_1", "1_21_2_3", "1_21_2-3", "1_21_4", "1_21_5", "1_21_6_8", "1_21_6-8",
            "1_21_9_10", "1_21_11"
        ).forEach {
            exclude("com/github/stefvanschie/inventoryframework/nms/v$it/**")
            exclude("META-INF/maven/com.github.stefvanschie.inventoryframework/$it/**")
        }

        // removing all IF classes that we don't need
        exclude {
            (it.path.startsWith("com/github/stefvanschie/inventoryframework/")
                    && (it.name.endsWith("Gui.class") || it.name.endsWith("Pane.class"))
                    && it.name != "ChestGui.class"
                    && it.name != "Gui.class"
                    && it.name != "NamedGui.class"
                    && it.name != "MergedGui.class"
                    && it.name != "Pane.class"
                    && it.name != "PaginatedPane.class"
                    && it.name != "StaticPane.class"
                    && it.name != "OutlinePane.class"
                    && it.name != "PositionedPane.class"
                    ) ||
                    (it.path.startsWith("com/github/stefvanschie/inventoryframework/pane/component")
                            && it.name != "PagingButtons.class"
                            )
        }

        exclude("com/github/stefvanschie/inventoryframework/nms/v*/*InventoryImpl*.class")
        exclude("fonts/**")
        exclude("com/github/stefvanschie/inventoryframework/font/**")
        exclude("com/github/stefvanschie/inventoryframework/abstraction/**")
        exclude("META-INF/**")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

// Configuring paper-plugin.yml
paper {
    authors = listOf("EmirhanTr3")
    description = "The greatest punishment plugin that's ever existed."
    website = "https://github.com/EmirhanTr3/EchoPunish"
    main = "cat.emir.echopunish.EchoPunish"
    loader = "cat.emir.echopunish.echolib.load.LibraryLoader"
    apiVersion = "26.1.2"

    // Keep this on!
    generateLibrariesJson = true

    serverDependencies {
        register("LuckPerms") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}

kotlin {
    jvmToolchain(25)
}