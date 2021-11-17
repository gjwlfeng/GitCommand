package com.zf.plugins.GitCommand.task

import com.zf.plugins.GitCommand.GitCommandPlugin
import com.zf.plugins.GitCommand.GitConfig
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * git tag 任务
 */
class GitTagTask extends DefaultTask {

    @InputDirectory
    final DirectoryProperty workDirFile = project.objects.directoryProperty()

    @Input
    final Property<String> versionName = project.objects.property(String)

    @Optional
    @Input
    final Property<Integer> versionCode = project.objects.property(Integer)

    @InputFile
    final RegularFileProperty changedLogFile = project.objects.fileProperty()

    @TaskAction
    void action() {

        String tagName = getTagName()

        boolean exits = exits(tagName)
        if (exits) {
            int result = deleteLocalTag(tagName)
            if (result != 0) {
                throw new GradleException("Failed to delete local Tag.tag:${tagName}\"")
            }
        } else {
            project.logger.quiet("No'${tagName}'tag exists locally.")
        }

        def lines = getLog()
        def remark = getRemark(lines)
        def result = createTag(tagName, remark)
        if (result != 0) {
            throw new GradleException("Failed to create local ${tagName} Tag.")
        }
    }

    private String getTagName() {
        def versionCode = this.versionCode.getOrNull()
        if (versionCode != null) {
            return "v${versionName.get().toString()}-${versionCode}"
        }
        return "v${versionName.get().toString()}"
    }

    /**
     * 备注
     * @return
     */
    private static String getRemark(def logs) {
        def changes = ""
        logs.eachWithIndex { log, index ->
            changes += "${log} "
        }
        return changes
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
                logs.add(trimLine)
            }
        }
        return logs
    }

    /**
     * 判断是否存在指定tag
     * @return
     */
    private boolean exits(String tagName) {

        String cmd = "git tag -l ${tagName}"
        project.logger.quiet(cmd)

        ByteArrayOutputStream bos = new ByteArrayOutputStream()

        project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput bos
            standardOutput bos
        }

        String content = bos.toString()
        logger.quiet(bos.toString())

        return content.contains(tagName)

    }

    /**
     * 创建tag
     * @param tagName
     * @return
     */
    private int createTag(String tagName, String remark) {

        def cmd = "git tag -a ${tagName} -m \"${remark}\""

        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            errorOutput System.err
            standardOutput System.out
        }

        return exeResult.exitValue
    }

    /**
     * 删除本地tag
     * @param tagName
     * @return
     */
    private int deleteLocalTag(String tagName) {

        String cmd = "git tag -d ${tagName}"
        project.logger.quiet(cmd)

        def exeResult = project.exec {
            workingDir workDirFile.get().asFile
            commandLine 'cmd', '/C', cmd
            standardOutput System.out
            errorOutput System.err
        }

        return exeResult.exitValue
    }

    static class CreationAction implements Action<GitTagTask> {

        GitConfig config

        CreationAction(GitConfig config) {
            this.config = config
        }

        @Override
        void execute(GitTagTask task) {
            configWorkDir(task)
            configChangedLogFile(task)
            configVersionName(task)
            configVersionCode(task)
        }

        def configVersionName(GitTagTask task) {
            def versionName = config.versionName
            if (versionName == null) {
                throw new IllegalArgumentException("\"versionName\" cannot be empty")
            }
            task.versionName.set(versionName)
        }

        def configVersionCode(GitTagTask task) {
            def versionCode = config.versionCode
            if (versionCode != null) {
                task.versionCode.set(versionCode)
            }
        }

        def configChangedLogFile(GitTagTask task) {
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

        def configWorkDir(GitTagTask task) {
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