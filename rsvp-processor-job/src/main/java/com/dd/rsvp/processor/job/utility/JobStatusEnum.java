package com.dd.rsvp.processor.job.utility;

public enum JobStatusEnum {
    COMPLETED("Completed"),
    PENDING("Pending"),
    RUNNING("Running"),
    NA("NA");

    private String message;

    JobStatusEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
