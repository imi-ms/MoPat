package de.imi.mopat.model.enumeration;

public enum ReviewStatus {

    PENDING , APPROVED, REJECTED;

    public boolean isUnfinished(){
        return this == PENDING || this == REJECTED;
    }

    public boolean isFinished() {
        return this == APPROVED;
    }
}