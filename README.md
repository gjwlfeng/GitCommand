# GitCommand
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/gjwlfeng/GitCommand/master/LICENSE)




GitCommand：快捷的git仓库管理工具。

GitCommand 快捷提供仓库管理，解放双手，实现自动化流程。


### Gradle插件使用方式

#### 配置build.gradle

在位于项目的根目录 `build.gradle` 文件中添加 ApkSign插件的依赖， 如下：

```groovy
buildscript {
    dependencies {
        classpath 'com.zf.plugins:GitCommand:1.0.3'
    }
}
```

并在当前App的 `build.gradle` 文件中apply这个插件

```groovy
apply plugin: 'com.zf.plugins.GitCommand'
```

#### 配置插件

```groovy
gitConfig {
    //当前版本名称 ,git commit ,git tag 都会用到
    versionName "1.1.0"
    //版本号,可选
    versionCode 1
    //git commit 提交日志
    changedLogFilePath new File("E:\\XXXXX\\log.txt").absolutePath
    //可选参数。git命令当前的运行的目录，默认为项目的根目录(`project.getRootDir().absolutePath`)
    workDirFilePath new File("E:\\workspace\\XXXXXX\\GitProject").absolutePath
    //可选参数。远程仓储名称集合，默认名称为origin,如果设置当前参数则不存在仓库名‘origin’. 远程仓储名是通过命令 `git remote add Hello http://wwww.baidu.com/Hello.git` 所添加的， Hello就是远程仓库名
    repositories {
    //定义 remote name
        gitee{
            // 可选，修改remote name
            repoName "12"
        }
        github{}
        origin{}
    } 
}
```
	
**gitCommand插件命令：**

![GitCommand插件 gradle命令](https://raw.githubusercontent.com/gjwlfeng/GitCommand/master/GitCommand/pic/gradle_git_command.png)

 
 如上面的图片所示的gradle命令
 

| ID |     gradle命令  | git命令 |  依赖ID|
|----|----------------| ------- |---|
|  1 | ./gradle gitShowLog             |    git log --date=format:\"%m-%d %H%M:%S\" --pretty=format:\"%h - %an, %ad : %s\" -5"  |  |
|  2 | ./gradle gitShowRemote             |    git remote -v  | |
|  3 | ./gradle gitShowStatus             |    git status| |
|  4 | ./gradle gitShowTag             |    git tag -n --sort=taggerdate| |
|  5 | ./gradle gitAdd             |   git add .  | |
|  6 | ./gradle gitCommit             |     git commit -m "加上日志文件时日志" |5 |
|  7 | ./gradle gitTag            |   git tag v${versionName}-${versionCode}    |5,6 |
|  8 | ./gradle gitTagDel             |    git tag -d v${versionName}-${versionCode}   | |
|  9 | ./gradle gitPull${repositoryName}            |    git pull ${repositoryName}  | |
| 10 | ./gradle gitPush${repositoryName}            |    git push ${repositoryName}  ${currBrandName}  |5,6 |
| 11 | ./gradle gitPushTag${repositoryName}            |    git push ${repositoryName} ${tagName}  |5,6,7 |
| 12 | ./gradle gitPushDleTag${repositoryName}            |    git push ${repositoryName} :refs/tags/${tagName} ||

 

## Q&A
- [输出乱码](https://github.com/903600017/GitCommand/wiki/Terminal-%E8%BE%93%E5%87%BA%E4%B9%B1%E7%A0%81%EF%BC%9F)？

## 技术支持

* Read The Fucking Source Code
* 通过提交issue来寻求帮助
* 联系我们寻求帮助.(QQ群：366399995)

## 贡献代码
* 欢迎提交issue
* 欢迎提交PR


## License

    Copyright 2017 903600017

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
