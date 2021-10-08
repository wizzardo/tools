package com.wizzardo.tools.gradle

import com.wizzardo.tools.sql.query.Generator
import org.gradle.api.*
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
        abstract TablesGenerator getTablesGenerator();

        void tablesGenerator(Action<? super TablesGenerator> action) {
            action.execute(getTablesGenerator());
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
            apply plugin: 'maven'

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
                from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
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
                    def migrations = toolsSqlExtension.migrations
                    if (migrations && migrations.getEnabled().getOrElse(false)) {
                        def out = migrations.getOut().getOrElse('migrations.txt')
                        if (!out.startsWith('/'))
                            out = '/' + out

                        def file = new File(projectDir.absolutePath, 'src/main/resources' + out)
                        file.parentFile.mkdirs()

                        def src = migrations.getSrc().getOrElse('migrations')

                        def sout = new StringBuilder(), serr = new StringBuilder()
                        def proc = "find ${src} -name *.sql".execute([], new File(projectDir.absolutePath, 'src/main/resources'))
                        proc.consumeProcessOutput(sout, serr)
                        proc.waitForOrKill(1000)
                        if (serr.length() > 0) {
                            System.err.println(serr)
                            throw new IllegalStateException()
                        }

                        file.setText(sout.toString())
                    }
                }
            })

            tasks.processResources.dependsOn listMigrations

            task([description: 'generate dto tables'], 'generateTables', {
                doLast {
                    def tablesGenerator = toolsSqlExtension.tablesGenerator
                    if (tablesGenerator && tablesGenerator.getEnabled().getOrElse(false)) {
                        def src = tablesGenerator.getSrc().get()
                        def out = tablesGenerator.getOut().get()
                        def packageName = tablesGenerator.getPackageName().get()

                        Generator generator = new Generator(out, packageName);
                        generator.createTables(new File(src).listFiles());
                    }
                }
            })

            tasks.compileJava.dependsOn generateTables

        }
    }
}
