package com.zf.plugins.GitCommand

import com.zf.plugins.GitCommand.task.GitAddTask
import com.zf.plugins.GitCommand.task.GitCommitTask
import com.zf.plugins.GitCommand.task.GitDelTagTask
import com.zf.plugins.GitCommand.task.GitShowBranchTask
import com.zf.plugins.GitCommand.task.GitShowLogTask
import com.zf.plugins.GitCommand.task.GitShowRemoteTask
import com.zf.plugins.GitCommand.task.GitShowStatusTask
import com.zf.plugins.GitCommand.task.GitShowTagTask
import com.zf.plugins.GitCommand.task.GitTagTask
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider


public class GitConfig {
    public String versionName
    public Integer versionCode
    public File changedLogFile
    public File workDirFile
    public NamedDomainObjectContainer<Repository> repositories;

    GitConfig(Project project) {
        repositories = project.objects.domainObjectContainer(Repository.class)
    }

    TaskProvider<DefaultTask> helpTask
    TaskProvider<GitShowTagTask> showTagTask
    TaskProvider<GitShowLogTask> showLogTask
    TaskProvider<GitShowStatusTask> showStatusTask
    TaskProvider<GitShowBranchTask> showBranchTask

    TaskProvider<GitAddTask> gitAddTask
    TaskProvider<GitCommitTask> gitCommitTask

    TaskProvider<GitTagTask> gitTagTask

    TaskProvider<GitDelTagTask> gitDelTagTask

    TaskProvider<GitShowRemoteTask> showRemoteTask

    HashMap<String, TaskProvider> pullTask = new HashMap<>()
    HashMap<String, TaskProvider> pushTask = new HashMap<>()
    HashMap<String, TaskProvider> pushTagTask = new HashMap<>()
    HashMap<String, TaskProvider> pushDelTagTask = new HashMap<>()

    def versionName(String versionName) {
        this.versionName = versionName
    }

    def versionCode(Integer versionCode) {
        this.versionCode = versionCode
    }

    def changedLogFile(File changedLogFile) {
        this.changedLogFile = changedLogFile
    }

    def workDirFile(File workDirFile) {
        this.workDirFile = workDirFile
    }

    void repositories(Action<NamedDomainObjectContainer<Repository>> action) {
        action.execute(repositories)
    }

    @Override
    String toString() {
        return "GitConfig{" +
                "versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", changedLogFile=" + changedLogFile +
                ", workDirFile=" + workDirFile
                '}'
    }
}
