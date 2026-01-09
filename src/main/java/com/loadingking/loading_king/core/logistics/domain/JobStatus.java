package com.loadingking.loading_king.core.logistics.domain.model;

public enum JobStatus {
    PENDING("대기"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    CANCELLED("취소됨");

    private final String description;

    JobStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
