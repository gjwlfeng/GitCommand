package com.zf.plugins.GitCommand;

public class Repository {
    public String name;
    public String repoName;

    Repository(String name) {
        this.name = name
    }

    Repository(String name, String repoName) {
        this.name = name
        this.repoName = repoName
    }

    Repository() {
    }
    void repoName(String repoName) {
        this.repoName = repoName
    }
}
