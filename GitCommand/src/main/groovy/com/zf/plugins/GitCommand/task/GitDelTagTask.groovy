package com.zf.plugins.GitCommand.task

import com.zf.plugins.GitCommand.GitCommandPlugin
import com.zf.plugins.GitCommand.GitConfig
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * git tag 任务
 */
class GitDelTagTask extends DefaultTask {

    @InputDirectory
    DirectoryProperty workDirFile = project.objects.directoryProperty()

    @Input
    Property<String> versionName = project.objects.property(String)

    @Input
    @Optional
    Property<Integer> versionCode = project.objects.property(Integer)

    @InputFile
    RegularFileProperty changedLogFile = project.objects.fileProperty()

    @TaskAction
    void action() {

        String tagName = getTagName()

        boolean exits = exits(tagName)
        if (exits) {
            int result = deleteLocalTag(tagName)
            if (result != 0) {
                throw new GradleException("Failed to delete local Tag.tag:${tagName}\"")
            } else {
                project.logger.quiet("Delete '${tagName}' tag successfully.")
            }
        } else {
            project.logger.quiet("No '${tagName}' tag exists locally.")
        }

    }


    String getTagName() {
        def versionCode = this.versionCode.getOrNull()
        if (versionCode != null) {
            return "v${versionName.get().toString()}-${versionCode}"
        }
        return "v${versionName.get().toString()}"
    }

    /**
     * 判断是否存在指定tag
     * @return
     */
    boolean exits(String tagName) {

        String cmd = "git tag -l ${tagName}"
        project.logger.quiet(cmd)

        ByteArrayOutputStream bos = new ByteArrayOutputStream()

        project.exec {
            workingDir workDirFile.asFile.get()
            commandLine 'cmd', '/C', cmd
            errorOutput bos
            standardOutput bos
        }

        String content = bos.toString()
        logger.quiet(bos.toString())

        return content.contains(tagName)

    }

    /**
     * 删除本地tag
     * @param tagName
     * @return
     */
    def deleteLocalTag(String tagName) {

        String cmd = "git tag -d ${tagName}"
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.asFile.get()
            commandLine 'cmd', '/C', cmd
            standardOutput System.out
            errorOutput System.err
        }

        return exeResult.exitValue
    }

     static class CreationAction implements Action<GitDelTagTask> {

        GitConfig config

        CreationAction(GitConfig config) {
            this.config = config
        }

        @Override
        void execute(GitDelTagTask task) {
            configWorkDir(task)
            configChangedLogFile(task)
            configVersionName(task)
        }

        def configVersionName(GitDelTagTask task) {
            def versionName = config.versionName
            if (versionName == null) {
                throw new IllegalArgumentException("\"versionName\" cannot be empty")
            }
            task.versionName.set(versionName)
        }

        def configChangedLogFile(GitDelTagTask task) {
            def changedLogFile = config.changedLogFile
            if (changedLogFile != null) {
                if (!changedLogFile.exists()) {
                    throw new IllegalArgumentException("\"${changedLogFile.absolutePath}\" not found")
                }
            }
            if (changedLogFile == null) {
                throw new IllegalArgumentException("\"changedLogFile\" cannot be empty")
            }
            task.changedLogFile.fileValue(changedLogFile)
        }

        def configWorkDir(GitDelTagTask task) {
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