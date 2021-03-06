import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.5.31"
	id("org.jetbrains.compose") version "1.0.0-beta5"
}

group = "pl.szkolykreatywne"
version = "1.1"

repositories {
	google()
	mavenCentral()
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
	implementation(compose.desktop.currentOs)
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

tasks.withType<KotlinCompile>() {
	kotlinOptions.jvmTarget = "11"
}

compose.desktop {
	application {
		mainClass = "MainKt"
		nativeDistributions {
			targetFormats(TargetFormat.Exe)
			packageName = "MonteCarlo"
			vendor = "github.com/KamilKurde"
			packageVersion = "1.1.0"
			windows {
				iconFile.set(project.file("src" + File.separator + "main" + File.separator + "resources" + File.separator + "icon.ico"))
			}
		}
	}
}