package com.zf.plugins.GitCommand.task

import com.zf.plugins.GitCommand.GitCommandPlugin
import com.zf.plugins.GitCommand.GitConfig
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

 class GitShowTagTask extends DefaultTask {


    @InputDirectory
    DirectoryProperty workDirFile = project.objects.directoryProperty()


    @TaskAction
    void action() {
        String cmd = "git tag -n --sort=taggerdate"
        project.logger.quiet(cmd)

        project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            standardOutput System.out
            errorOutput System.err
        }
    }

     static class CreationAction implements Action<GitShowTagTask> {

        GitConfig config

        CreationAction(GitConfig config) {
            this.config = config
        }

        @Override
        void execute(GitShowTagTask task) {

            configWorkDir(task)

        }

        def  configWorkDir(GitShowTagTask task) {
            def workDirFile = config.workDirFile
            if (workDirFile == null) {
                workDirFile = task.project.getRootDir()
            }

            if (!workDirFile.exists()) {
                throw new GradleException("Git working directory does not exist，workDirPath=${workDirFile.absolutePath}")
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
