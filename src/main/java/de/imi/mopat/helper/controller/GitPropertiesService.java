package de.imi.mopat.helper.controller;

import de.imi.mopat.model.dto.GitPropertiesDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitPropertiesService {

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

    public GitPropertiesDTO getGitProperties() {
        return new GitPropertiesDTO(buildVersion, branch, commitId, commitIdAbbrev, commitMessageShort);
    }
}