package de.imi.mopat.model.dto;

public record GitPropertiesDTO(
        String buildVersion,
        String branch,
        String commitId,
        String commitIdAbbrev,
        String commitMessageShort
) { }