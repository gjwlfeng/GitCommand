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
import org.gradle.api.tasks.TaskAction

/**
 * git commit 推送任务
 */
class GitPushTask extends DefaultTask {

    @Input
    Property<Repository> repository = project.objects.property(Repository)

    @InputDirectory
    DirectoryProperty workDirFile = project.objects.directoryProperty()


    @TaskAction
    void action() {

        checkRepositoryName()

        def branchName = getCurBranchName()
        int exitValue = pushCommit(branchName)
        if (exitValue != 0) {
            throw new GradleException('Commit failure')
        }
    }

    def getRepoName() {
        def repo = repository.get()
        def repoName = repo.name
        if (repo.repoName != null && repo.repoName.trim().length() > 0) {
            repoName = repo.repoName.trim()
        }
        return repoName
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

                def configRepo = getRepoName();

                def repoName = repositoryNameAar.find { line ->
                    return line.contains(configRepo)
                }
                if (!repoName) {
                    throw new GradleException("\"${configRepo}\" repository not found.\nAvailability repository:\n ${repositoryNameAar.join("\n")}",)
                }
            } else {
                throw new GradleException("\"git remote\" command cannot be executed")
            }
        }
    }

    def getCurBranchName() {

        String cmd = 'git rev-parse --abbrev-ref HEAD'
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
            def branchName = bos.toString().trim()
            project.logger.quiet(branchName)
            if (branchName) {
                return branchName
            }
            throw new GradleException('Failed to get the current branch name.Branch name is empty')
        }
    }

    def pushCommit(String branchName) {

        String cmd = "git push ${getRepoName()} ${branchName}"
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            standardOutput System.out
            errorOutput System.err
        }

        return exeResult.exitValue
    }
     static class CreationAction implements Action<GitPushTask> {

        GitConfig config
         Repository repository

        CreationAction(GitConfig config, Repository repository) {
            this.config = config
            this.repository = repository
        }

        @Override
        void execute(GitPushTask task) {
            configWorkDir(task)
            configRepository(task)
        }

        def configRepository(GitPushTask task) {
            task.repository.set(repository)
        }

        def configWorkDir(GitPushTask task) {
            def workDirFile = config.workDirFile
            if (workDirFile == null) {
                workDirFile = task.project.getRootDir()
            }

            if (!workDirFile.exists()) {
                throw new GradleException("Git working directory does not exist，workDirFile=${workDirFile.absolutePath}")
            }

            if(!workDirFile.isDirectory()){
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