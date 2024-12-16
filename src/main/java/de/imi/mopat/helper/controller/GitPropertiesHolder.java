package de.imi.mopat.helper.controller;

import de.imi.mopat.model.GitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitPropertiesHolder {

    @Value("${git.build.version:Unknown}")
    private String buildVersion;

    @Value("${git.branch:Unknown}")
    private String branch;

    @Value("${git.commit.id:Unknown}")
    private String commitId;

    @Value("${git.commit.id.abbrev:Unknown}")
    private String commitIdAbbrev;

    @Value("${git.commit.message.short:Unknown}")
    private String commitMessageShort;

    public GitProperties getGitProperties() {
        return new GitProperties(
                buildVersion,
                branch,
                commitId,
                commitIdAbbrev,
                commitMessageShort
        );
    }
}