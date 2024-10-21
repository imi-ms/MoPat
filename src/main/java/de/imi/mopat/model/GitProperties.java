package de.imi.mopat.model;

public record GitProperties(
        String buildVersion,
        String branch,
        String commitId,
        String commitIdAbbrev,
        String commitMessageShort
) { }