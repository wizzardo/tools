package com.wizzardo.tools.gradle

import com.wizzardo.tools.sql.query.Generator
import org.gradle.api.*
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

import java.text.SimpleDateFormat

class BuildPlugin implements Plugin<Project> {

    def getGitRevision() {
        return "git rev-parse HEAD".execute().text.trim()
    }

    def getGitTags() {
        return "git tag --points-at HEAD".execute().text.trim().readLines()
    }

    def getGitBranchName() {
        def branchName = System.getenv().get('GIT_BRANCH')
        if (!branchName) {
            branchName = "git rev-parse --abbrev-ref HEAD".execute().text.trim()
            if (branchName == gitRevision) {
                branchName = "git branch -a --contains ${"git rev-parse --verify HEAD".execute().text}".execute()
                        .text.trim().readLines()
                        .find({ it.contains('origin/') })?.with({ it.split('origin/')[1].trim() }) ?: ''
            }
        }
        return branchName
    }

    abstract static class SqlToolsExtension {
        @Nested
        abstract Migrations getMigrations();

        void migrations(Action<? super Migrations> action) {
            action.execute(getMigrations());
        }

        @Nested
        abstract Migrations getMigrationsTest();

        void migrationsTest(Action<? super Migrations> action) {
            action.execute(getMigrationsTest());
        }


        @Nested
        abstract TablesGenerator getTablesGenerator();

        void tablesGenerator(Action<? super TablesGenerator> action) {
            action.execute(getTablesGenerator());
        }

        @Nested
        abstract TablesGenerator getTablesGeneratorTest();

        void tablesGeneratorTest(Action<? super TablesGenerator> action) {
            action.execute(getTablesGeneratorTest());
        }
    }

    abstract static class Migrations {
        abstract Property<Boolean> getEnabled();

        abstract Property<String> getSrc();

        abstract Property<String> getOut();
    }

    abstract static class TablesGenerator {
        abstract Property<Boolean> getEnabled();

        abstract Property<String> getSrc();

        abstract Property<String> getOut();

        abstract Property<String> getPackageName();
    }

    def void apply(Project project) {
        project.with {
            apply plugin: 'java'

            ext.getGitRevision = { -> gitRevision }
            ext.getGitBranchName = { -> gitBranchName }

            tasks.withType(JavaCompile) {
                if (JavaVersion.current().java8Compatible) {
                    options.compilerArgs += ["-parameters"]
                }
            }

            task('javadocJar', type: Jar, {
                classifier = 'javadoc'
                from javadoc
            })

            task('sourcesJar', type: Jar, {
                classifier = 'sources'
                from sourceSets.main.allSource
            })

            artifacts {
                archives javadocJar, sourcesJar
            }


            task([type: Jar, description: 'Generates runnable jar with all dependencies'], 'fatJar', {
                baseName = project.name + '-all'
//                from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from { sourceSets.main.runtimeClasspath.collect { !it.isFile() ? it : zipTree(it) } }
                exclude 'META-INF/*'
                with jar

                manifest {
                    attributes(
                            "Main-Class": "${{ -> project.hasProperty('mainClassName') ? mainClassName : '' }}",
                            "revision": gitRevision,
                            "branch": gitBranchName,
                            "buildTime": new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()),
                            "version": "${{ -> version }}"
                    )
                }
            })
            task([dependsOn: fatJar, description: 'Alias to fatJar'], 'uberjar')


            task([description: 'preparing app/info'], 'makeAppInfo', {
                doLast {
                    def file = new File(project.buildDir, 'resources/main/application.yaml')
                    if (!file.exists())
                        file = new File(project.buildDir, 'resources/main/application.yml')

                    if (file.exists()) {
                        def yml = file.text
                        yml = yml.replace('{name}', String.valueOf(project.name))
                        yml = yml.replace('{description}', String.valueOf(project.description))
                        yml = yml.replace('{version}', String.valueOf(project.version))
                        yml = yml.replace('{revision}', String.valueOf(gitRevision))
                        yml = yml.replace('{branch}', String.valueOf(gitBranchName))
                        yml = yml.replace('{tags}', String.valueOf(gitTags))
                        yml = yml.replace('{buildTime}', new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()))
                        file.setText(yml)
                    }
                }
            })

            tasks.jar.dependsOn makeAppInfo

            SqlToolsExtension toolsSqlExtension = project.extensions.create('sqlTools', SqlToolsExtension.class)
            task([description: 'listing all migrations'], 'listMigrations', {
                doLast {
                    [
                            main: toolsSqlExtension.migrations,
                            test: toolsSqlExtension.migrationsTest,
                    ].each { sourceSet, migrations ->
                        if (migrations && migrations.getEnabled().getOrElse(false)) {
                            def out = migrations.getOut().getOrElse('migrations.txt')
                            if (!out.startsWith('/'))
                                out = '/' + out

                            def file = new File(projectDir.absolutePath, "src/$sourceSet/resources$out")
                            file.parentFile.mkdirs()

                            def src = migrations.getSrc().getOrElse('migrations')

                            def sout = new StringBuilder(), serr = new StringBuilder()
                            def proc = "find ${src} -name *.sql".execute([], new File(projectDir.absolutePath, "src/$sourceSet/resources"))
                            proc.consumeProcessOutput(sout, serr)
                            proc.waitForOrKill(1000)
                            if (serr.length() > 0) {
                                System.err.println(serr)
                                throw new IllegalStateException()
                            }

                            file.setText(sout.toString())
                        }
                    }
                }
            })

            tasks.processResources.dependsOn listMigrations
            tasks.processTestResources.dependsOn listMigrations

            task([description: 'generate dto tables'], 'generateTables', {
                doLast {
                    [
                            main: toolsSqlExtension.tablesGenerator,
                            test: toolsSqlExtension.tablesGeneratorTest,
                    ].each { sourceSet, tablesGenerator ->
                        def isEnabled = tablesGenerator && tablesGenerator.getEnabled().getOrElse(false)
                        println "generateTables for ${sourceSet} sourceSet enabled: ${isEnabled}"
                        if (isEnabled) {
                            def src = tablesGenerator.getSrc().get()
                            def out = tablesGenerator.getOut().get()
                            def packageName = tablesGenerator.getPackageName().get()

                            Generator generator = new Generator(new File(project.projectDir, out).getCanonicalPath(), packageName);

                            def files = new File(project.projectDir, src).listFiles()
                            if (files) {
//                                files.each { println "  ${it}" }
                                generator.createTables(files);
                            } else {
                                println "no files in src dir"
                            }
                        }
                    }
                }
            })

            tasks.compileJava.dependsOn generateTables
            tasks.compileTestJava.dependsOn generateTables

        }
    }
}
