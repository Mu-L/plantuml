//    permits to start the build setting the javac release parameter, no parameter means build for java8:
// gradle clean build -x javaDoc -PjavacRelease=8
// gradle clean build -x javaDoc -PjavacRelease=17
//    also supported is to build first, with java17, then switch the java version, and run the test with java8:
// gradle clean build -x javaDoc -x test
// gradle test
val javacRelease = (project.findProperty("javacRelease") ?: "8") as String

plugins {
	java
	`maven-publish`
	signing
}

group = "net.sourceforge.plantuml"
description = "PlantUML"

java {
	withSourcesJar()
	//withJavadocJar()
	registerFeature("pdf") {
		usingSourceSet(sourceSets["main"])
	}
}

dependencies {
	compileOnly("org.apache.ant:ant:1.10.13")
	testImplementation("org.assertj:assertj-core:3.24.2")
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
	testImplementation("org.scilab.forge:jlatexmath:1.0.7")
}

repositories {
	mavenLocal()
	mavenCentral()
}

sourceSets {
	main {
		java {
			srcDirs("build/generated/sjpp")
		}
		resources {
			srcDirs("build/generated/sjpp")
			include("**/*.png")
			include("**/*.svg")
			include("**/*.txt")
		}
	}
}

tasks.compileJava {
	if (JavaVersion.current().isJava8) {
		java.targetCompatibility = JavaVersion.VERSION_1_8
	} else {
		options.release.set(Integer.parseInt(javacRelease))
	}
}

tasks.withType<Jar>().configureEach {
	manifest {
		attributes["Main-Class"] = "net.sourceforge.plantuml.Run"
		attributes["Implementation-Version"] = archiveVersion
		attributes["Build-Jdk-Spec"] = System.getProperty("java.specification.version")
		from("../manifest.txt")
	}
	from("../skin") { into("skin") }
	from("../stdlib") { into("stdlib") }
	from("../svg") { into("svg") }
	from("../themes") { into("themes") }
	// source sets for java and resources are on "src", only put once into the jar
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

val syncSources by tasks.registering(Sync::class) {
	from(rootProject.layout.projectDirectory.dir("src"))
	into(project.layout.buildDirectory.dir("sources/sjpp/java"))
}

val preprocessMitLicenceAntTask by tasks.registering() {
	dependsOn(syncSources)
	inputs.dir(project.layout.buildDirectory.dir("sources/sjpp/java"))
	outputs.dir(project.layout.buildDirectory.dir("generated/sjpp"))
	doLast {
		ant.withGroovyBuilder {
			"taskdef"(
				"name" to "sjpp",
				"classname" to "sjpp.SjppAntTask",
				"classpath" to rootProject.layout.projectDirectory.files("sjpp.jar").asPath
			)
			"sjpp"(
				"src" to project.layout.buildDirectory.dir("sources/sjpp/java").get().asFile.absolutePath,
				"dest" to project.layout.buildDirectory.dir("generated/sjpp").get().asFile.absolutePath,
				"define" to "__MIT__"
			)
		}
	}
}

tasks.processResources{
	dependsOn(preprocessMitLicenceAntTask)
}

tasks.compileJava{
	dependsOn(preprocessMitLicenceAntTask)
}

tasks.named("sourcesJar"){
	dependsOn(preprocessMitLicenceAntTask)
}


publishing {
	publications.create<MavenPublication>("maven") {
		from(components["java"])
		pom {
			name.set("PlantUML")
			description.set("PlantUML is a component that allows to quickly write diagrams from text.")
			groupId = project.group as String
			artifactId = project.name
			version = project.version as String
			url.set("https://plantuml.com/")
			licenses {
				license {
					name.set("The GNU General Public License")
					url.set("http://www.gnu.org/licenses/gpl.txt")
				}
			}
			developers {
				developer {
					id.set("arnaud.roques")
					name.set("Arnaud Roques")
					email.set("plantuml@gmail.com")
				}
			}
			scm {
				connection.set("scm:git:git://github.com:plantuml/plantuml.git")
				developerConnection.set("scm:git:ssh://git@github.com:plantuml/plantuml.git")
				url.set("https://github.com/plantuml/plantuml")
			}
		}
	}
	repositories {
		maven {
			name = "OSSRH"
			val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
			val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
			url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
			credentials {
				username = System.getenv("OSSRH_USERNAME")
				password = System.getenv("OSSRH_PASSWORD")
			}
		}
	}
}
