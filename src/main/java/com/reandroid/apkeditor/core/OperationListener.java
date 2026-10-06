package com.reandroid.apkeditor.core;

public interface OperationListener {
    default void onStarted(String operationId, String operationName) {}
    default void onProgress(int percent) {}
    default void onMessage(String message) {}
    default void onWarning(String message) {}
    default void onError(String message, Throwable error) {}
    default void onCompleted(OperationResult result) {}
}
