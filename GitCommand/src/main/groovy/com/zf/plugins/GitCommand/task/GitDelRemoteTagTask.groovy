package com.zf.plugins.GitCommand.task

import com.zf.plugins.GitCommand.GitCommandPlugin
import com.zf.plugins.GitCommand.GitConfig
import com.zf.plugins.GitCommand.Repository
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * git tag 推送任务
 */
class GitDelRemoteTagTask extends DefaultTask {

    @InputDirectory
    DirectoryProperty workDirFile = project.objects.directoryProperty()

    @Input
    @Optional
    Property<Integer> versionCode = project.objects.property(Integer)

    @Input
    Property<String> versionName = project.objects.property(String)

    @Input
    Property<Repository> repository = project.objects.property(Repository)


    /**
     * 删除 远程tag
     * @param tagName
     * @return
     */
    def deleteRemoteTag(String tagName) {

        String cmd = "git push ${getRepoName()} :refs/tags/${tagName}"

        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            standardOutput System.out
            errorOutput System.err
        }
        return exeResult.exitValue
    }

    def getRepoName() {
        def repo = repository.get()
        def repoName = repo.name
        if (repo.repoName != null && repo.repoName.trim().length() > 0) {
            repoName = repo.repoName.trim()
        }
       return repoName
    }

    @TaskAction
    void action() {

        checkRepositoryName()

        String tagName = getTagName()
        int result = deleteRemoteTag(tagName)
        if (result != 0) {
            project.logger.quiet("Failed to delete remote Tag.tag:${tagName}")
        }
    }

    String getTagName() {
        def versionCode = this.versionCode.getOrNull()
        if (versionCode != null) {
            return "v${versionName.get().toString()}-${versionCode}"
        }
        return "v${versionName.get().toString()}"
    }

    def checkRepositoryName() {

        String cmd = 'git remote'

        project.logger.quiet(cmd)

        ByteArrayOutputStream bos = new ByteArrayOutputStream()

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput System.err
            standardOutput bos
        }

        if (exeResult.exitValue != 0) {
            throw new GradleException('Failed to get the current branch name')
        } else {
            def repositoryNames = bos.toString().trim()
            if (repositoryNames.length() > 0) {

                def repositoryNameAar = repositoryNames.split(System.getProperty("line.separator"))

                def configRepoName = getRepoName();

                def repoName = repositoryNameAar.find { line ->
                    return line.contains(configRepoName)
                }
                if (!repoName) {
                    throw new GradleException("\"${configRepoName}\" repository not found.\nAvailability repository:\n ${repositoryNameAar.join("\n")}",)
                }
            } else {
                throw new GradleException("\"git remote\" command cannot be executed")
            }
        }
    }

    static class CreationAction implements Action<GitDelRemoteTagTask> {

        GitConfig config
        Repository repository

        CreationAction(GitConfig config, Repository repository) {
            this.config = config
            this.repository = repository
        }

        @Override
        void execute(GitDelRemoteTagTask task) {
            configWorkDir(task)
            configVersionName(task)
            configVersionCode(task)
            configRepository(task);
        }

        def configVersionName(GitDelRemoteTagTask task) {
            def versionName = config.versionName
            if (versionName == null) {
                throw new IllegalArgumentException("\"versionName\" cannot be empty")
            }
            task.versionName.set(versionName)
        }

        def configVersionCode(GitDelRemoteTagTask task) {
            def versionCode = config.versionCode
            if (versionCode != null) {
                task.versionCode.set(versionCode)
            }
        }

        def configRepository(GitDelRemoteTagTask task) {
            task.repository.set(repository)
        }

        def configWorkDir(GitDelRemoteTagTask task) {
            def workDirFile = config.workDirFile
            if (workDirFile == null) {
                workDirFile = task.project.getRootDir()
            }


            if (!workDirFile.exists()) {
                throw new GradleException("Git working directory does not exist，workDirPath=${workDirFile.absolutePath}")
            }

            if (!workDirFile.isDirectory()) {
                throw new GradleException("The working directory must be a folder")
            }

            File dotGitDir = new File(workDirFile, GitCommandPlugin.DOT_GIT)
            if ((!dotGitDir.exists()) || (!dotGitDir.isDirectory())) {
                throw new GradleException("Please execute the \"git init\" command to initialize the directory first.")
            }

            task.workDirFile.fileValue(workDirFile)
        }


    }
}