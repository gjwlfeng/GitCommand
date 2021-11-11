package com.zf.plugins.GitCommand

import com.zf.plugins.GitCommand.task.GitAddTask
import com.zf.plugins.GitCommand.task.GitCommitTask
import com.zf.plugins.GitCommand.task.GitDelRemoteTagTask
import com.zf.plugins.GitCommand.task.GitDelTagTask
import com.zf.plugins.GitCommand.task.GitPullTask
import com.zf.plugins.GitCommand.task.GitPushTask
import com.zf.plugins.GitCommand.task.GitShowBranchTask
import com.zf.plugins.GitCommand.task.GitShowLogTask
import com.zf.plugins.GitCommand.task.GitShowRemoteTask
import com.zf.plugins.GitCommand.task.GitShowStatusTask
import com.zf.plugins.GitCommand.task.GitShowTagTask
import com.zf.plugins.GitCommand.task.GitTagPushTask
import com.zf.plugins.GitCommand.task.GitTagTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitCommandPlugin implements Plugin<Project> {

    public static final String sPluginExtensionName = "gitConfig"
    public static final String DEFAULT_REMOTE_NAME = "origin"

    public static final String LOG_SEPARATOR = "###"
    public static final String DOT_GIT = ".git"

    Project project
    GitConfig gitConfig

    @Override
    void apply(Project project) {

        this.project = project
        configExtensions()

        createHelpTask()
        createShowTagTask()
        createShowLogTask()
        createShowStatusTask()
        createShowBranchTask()
        createAddTask()
        createCommitTask()
        createTagTask()
        createDelTagTask()
        createShowRemoteTask()

        gitConfig.repositories.all { Repository repository ->
            createPullTask(repository)
            createPushTask(repository)
            createPushTagTask(repository)
            createDeleteRemoteTagTask(repository)
        }
    }

    def configExtensions() {
        gitConfig = project.extensions.create(sPluginExtensionName, GitConfig.class, project)
    }

    def createHelpTask() {
        def helpTask = project.tasks.register("gitCommandHelp")
        helpTask.configure {
            description "帮助"
            group "git show"
            doLast {
                URL resource = GitCommandPlugin.class.getClassLoader().getResource("help_git_command.txt")
                logger.quiet(resource.getText("utf-8"))
            }
        }
        gitConfig.helpTask = helpTask
    }


    def createAddTask() {
        def gitAddTask = project.tasks.register("gitAdd", GitAddTask, new GitAddTask.CreationAction(gitConfig))
        gitAddTask.configure {
            group 'git cmd local'
            description 'git add'
        }
        gitConfig.gitAddTask = gitAddTask
    }

    def createCommitTask() {
        def gitCommitTask = project.tasks.register("gitCommit", GitCommitTask, new GitCommitTask.CreationAction(gitConfig))

        gitCommitTask.configure {
            group 'git cmd local'
            description 'git commit'
            dependsOn gitConfig.gitAddTask
        }
        gitConfig.gitCommitTask = gitCommitTask
    }

    def createPullTask(Repository repository) {
        def task = project.tasks.register(
                "gitPull${repository.name.capitalize()}",
                GitPullTask,
                new GitPullTask.CreationAction(gitConfig, repository))
        task.configure {
            group "git cmd remote(${repository.name})"
            description 'git pull'
        }
        gitConfig.pullTask.put(repository.name, task)
    }

    def createPushTask(Repository repository) {
        def task = project.tasks.register("gitPush${repository.name.capitalize()}", GitPushTask,
                new GitPushTask.CreationAction(gitConfig, repository))

        task.configure {
            group "git cmd remote(${repository.name})"
            description 'git push'
            dependsOn gitConfig.gitCommitTask
        }
        gitConfig.pushTask.put(repository.name, task)
    }

    def createTagTask() {
        def task = project.tasks.register("gitTag", GitTagTask, new GitTagTask.CreationAction(gitConfig))
        task.configure {
            group 'git cmd local'
            description 'git tag'
            dependsOn gitConfig.gitCommitTask
        }
        gitConfig.gitTagTask = task
    }

    def createDelTagTask() {
        def task = project.tasks.register("gitTagDel", GitDelTagTask, new GitDelTagTask.CreationAction(gitConfig))
        task.configure {
            group 'git cmd local'
            description 'git tag delete'
        }
        gitConfig.gitDelTagTask = task
    }

    def createPushTagTask(Repository repository) {
        def task = project.tasks.register(
                "gitPushTag${repository.name.capitalize()}",
                GitTagPushTask,
                new GitTagPushTask.CreationAction(gitConfig, repository))
        task.configure {
            group "git cmd remote(${repository.name})"
            description 'git tag push'
            dependsOn gitConfig.pushTask.get(repository.name), gitConfig.gitTagTask
        }
        gitConfig.pushTagTask.put(repository.name, task)
    }

    def createDeleteRemoteTagTask(Repository repository) {
        def task = project.tasks.register(
                "gitTagDelete${repository.name}",
                GitDelRemoteTagTask,
                new GitDelRemoteTagTask.CreationAction(gitConfig, repository))
        task.configure {
            group "git cmd remote(${repository.name})"
            description 'del tag'
        }
        gitConfig.pushDelTagTask.put(repository.name, task)
    }

    def createShowTagTask() {
        def showTagTask = project.tasks.register("gitShowTag", GitShowTagTask, new GitShowTagTask.CreationAction(gitConfig))
        showTagTask.configure {
            group 'git show'
            description '查看tag详情'
        }
        gitConfig.showTagTask = showTagTask
    }


    def createShowRemoteTask() {
        def task = project.tasks.register("gitShowRemote", GitShowRemoteTask, new GitShowRemoteTask.CreationAction(gitConfig))
        task.configure {
            group 'git show'
            description '查看仓库'
        }
        gitConfig.showRemoteTask = task
    }

    def createShowLogTask() {
        def task = project.tasks.register("gitShowLog", GitShowLogTask, new GitShowLogTask.CreationAction(gitConfig))
        task.configure {
            group 'git show'
            description '查看log详情'
        }
        gitConfig.showLogTask = task
    }

    def createShowStatusTask() {
        def showStatusTask = project.tasks.register("gitShowStatus", GitShowStatusTask, new GitShowStatusTask.CreationAction(gitConfig))
        showStatusTask.configure {
            group 'git show'
            description '查看git状态'
        }
        gitConfig.showStatusTask = showStatusTask
    }

    def createShowBranchTask() {
        def task = project.tasks.register("gitShowBranch", GitShowBranchTask, new GitShowBranchTask.CreationAction(gitConfig))
        task.configure {
            group 'git show'
            description '查看分支'
        }
        gitConfig.showBranchTask = task
    }

}

