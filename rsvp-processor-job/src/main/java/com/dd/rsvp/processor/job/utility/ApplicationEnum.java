package com.dd.rsvp.processor.job.utility;

public enum ApplicationEnum {

    SPARK_SESSION("Creating shared spark session"),
    SPARK_SESSION_ERROR("Could not create spark sql context"),
    VERIFIED_STATUS("VerifiedStatus"),
    PROCESS_ID("ProcessId"),
    S3_DATA_KEY("S3DataKey"),
    MIGRATED_STATUS("MigratedStatus");

    private String message;

    ApplicationEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
