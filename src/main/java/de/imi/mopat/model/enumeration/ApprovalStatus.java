package de.imi.mopat.model.enumeration;

public enum ApprovalStatus {
    DRAFT,         // Not yet approved, no review created
    UNDER_REVIEW,  // Review already exists
    APPROVED       // Has been approved
}