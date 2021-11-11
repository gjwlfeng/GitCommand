package com.zf.plugins.GitCommand.task

import com.zf.plugins.GitCommand.GitCommandPlugin
import com.zf.plugins.GitCommand.GitConfig
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * git add 任务
 */
class GitAddTask extends DefaultTask {

    @InputDirectory
    DirectoryProperty workDirFile = project.objects.directoryProperty()

    @TaskAction
    void action() {
        def cmd = 'git add . '
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput System.err
            standardOutput System.out
        }

        if (exeResult.exitValue != 0) {
            project.logger.quiet('Executing "git add ." command failed')
        }
    }

     static class CreationAction implements Action<GitAddTask> {

        GitConfig config

        CreationAction(GitConfig config) {
            this.config = config
        }

        @Override
        void execute(GitAddTask task) {
            configWorkDir(task)
        }

        def configWorkDir(GitAddTask task) {
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

            task.workDirFile.set(workDirFile)
        }
    }

}