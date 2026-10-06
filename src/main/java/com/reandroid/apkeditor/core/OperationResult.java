package com.reandroid.apkeditor.core;

public final class OperationResult {
    private final boolean success;
    private final String operationId;
    private final long durationMillis;
    private final Throwable error;

    private OperationResult(boolean success, String operationId,
                            long durationMillis, Throwable error) {
        this.success = success;
        this.operationId = operationId;
        this.durationMillis = durationMillis;
        this.error = error;
    }

    public static OperationResult success(String operationId, long durationMillis) {
        return new OperationResult(true, operationId, durationMillis, null);
    }

    public static OperationResult failure(String operationId, long durationMillis, Throwable error) {
        return new OperationResult(false, operationId, durationMillis, error);
    }

    public boolean isSuccess() { return success; }
    public String getOperationId() { return operationId; }
    public long getDurationMillis() { return durationMillis; }
    public Throwable getError() { return error; }
}
