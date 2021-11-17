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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * git commit 任务
 */
class GitCommitTask extends DefaultTask {

    @InputDirectory
    final DirectoryProperty workDirFile = project.objects.directoryProperty()

    @InputFile
    final RegularFileProperty changedLogFile = project.objects.fileProperty()

    @Input
    final Property<String> versionName = project.objects.property(String)

    @Optional
    @Input
    final Property<Integer> versionCode = project.objects.property(Integer)

    @TaskAction
    void action() {
        if (!isNeedCommit()) {

            String cmd = getCommitCommand()
            project.logger.quiet(cmd)

            def exeResult = project.exec {
                workingDir workDirFile.get().asFile
                commandLine 'cmd', '/C', cmd
                errorOutput System.err
                standardOutput System.out
            }
            if (exeResult.exitValue != 0) {
                throw new GradleException("commit failure")
            }
        }
    }

    private boolean isNeedCommit() {

        ByteArrayOutputStream bos = new ByteArrayOutputStream()

        String cmd = 'git status'
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput System.err
            standardOutput bos
        }

        String content = bos.toString()
        project.logger.quiet(content)

        if (exeResult.exitValue != 0) {
            throw new GradleException('Failed to get git status')
        }

        return content.contains('nothing to commit')
    }

    private List<String> getLog() {
        def logs = []

        int startNum = -1

        changedLogFile.get().asFile.eachLine { lineContent, num ->
            if (lineContent.trim().startsWith(GitCommandPlugin.LOG_SEPARATOR)) {
                startNum = num
            }
        }

        changedLogFile.get().asFile.eachLine { lineContent, num ->
            String trimLine = lineContent.trim()
            if (trimLine && startNum < num) {
                logs.add(lineContent)
            }
        }
        return logs
    }


    private String getTagName() {
        def versionCode = this.versionCode.getOrNull()
        if (versionCode != null) {
            return "v${versionName.get().toString()}-${versionCode}"
        }
        return "v${versionName.get().toString()}"
    }


    private String getCommitCommand() {
        def changes = ""

        def lines = getLog()

        lines.eachWithIndex { log, index ->
            changes += "${log} "
        }
        def remark = "${getTagName()} ${changes}"

        return "git commit -m \"${remark}\""
    }


    static class CreationAction implements Action<GitCommitTask> {

        GitConfig config

        CreationAction(GitConfig config) {
            this.config = config
        }

        @Override
        void execute(GitCommitTask task) {
            configWorkDir(task)
            configVersionName(task)
            configVersionCode(task)
            configChangedLogFile(task)
        }

        def configChangedLogFile(GitCommitTask task) {
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

        def configWorkDir(GitCommitTask task) {
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

        def configVersionName(GitCommitTask task) {
            def versionName = config.versionName
            if (versionName == null) {
                throw new IllegalArgumentException("\"versionName\" cannot be empty")
            }
            task.versionName.set(versionName)
        }

        def configVersionCode(GitCommitTask task) {
            def versionCode = config.versionCode
            if (versionCode != null) {
                task.versionCode.set(versionCode)
            }
        }
    }
}