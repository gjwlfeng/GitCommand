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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * git add 任务
 */
class GitPullTask extends DefaultTask {

    @Input
    final Property<Repository> repository = project.objects.property(Repository)

    @InputDirectory
    final DirectoryProperty workDirFile = project.objects.directoryProperty()

    @TaskAction
    void action() {

        checkRepositoryName()

        def branchName = getCurBranchName()
        project.logger.quiet("current branch:${branchName}")

        def cmd = "git pull ${getRepoName()} ${branchName}"
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput System.err
            standardOutput System.out
        }

        if (exeResult.exitValue != 0) {
            project.logger.quiet('执行 git pull 命令失败')
        }
    }

    private String getRepoName() {
        def repo = repository.get()
        def repoName = repo.name
        if (repo.repoName != null && repo.repoName.trim().length() > 0) {
            repoName = repo.repoName.trim()
        }
        return repoName
    }

    private String getCurBranchName() {

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
            if (branchName) {
                return branchName
            }
            throw new GradleException('Failed to get the current branch name.Branch name is empty')
        }
    }


    private String checkRepositoryName() {

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


    static class CreationAction implements Action<GitPullTask> {

        GitConfig config;
        Repository repository;

        CreationAction(GitConfig config, Repository repository) {
            this.config = config
            this.repository = repository
        }

        @Override
        void execute(GitPullTask task) {
            configWorkDir(task)
            configRepository(task)
        }

        def configRepository(GitPullTask task) {
            task.repository.set(repository)
        }

        def configWorkDir(GitPullTask task) {
            def workDirFile = config.workDirFile
            if (workDirFile == null) {
                workDirFile = task.project.getRootDir()
            }

            if (!workDirFile.exists()) {
                throw new GradleException("Git working directory does not exist，workDirFile=${workDirFile.absolutePath}")
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