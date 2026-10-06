package com.reandroid.apkeditor.core;

import com.reandroid.apkeditor.Options;

/**
 * Adaptador que permite ejecutar el motor existente sin acoplarlo a Swing.
 */
public class LegacyOptionsOperation implements Operation {
    private final String id;
    private final String name;
    private final Options options;

    public LegacyOptionsOperation(String id, String name, Options options) {
        this.id = id;
        this.name = name;
        this.options = options;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public Options getOptions() { return options; }

    @Override
    public OperationResult execute(OperationListener listener) throws Exception {
        long start = System.currentTimeMillis();
        if (listener != null) listener.onStarted(id, name);

        ExecutionContext.setListener(listener);
        try {
            options.validate();
            options.runCommand();
            OperationResult result = OperationResult.success(
                    id, System.currentTimeMillis() - start);
            if (listener != null) listener.onCompleted(result);
            return result;
        } catch (Exception ex) {
            if (listener != null) listener.onError(ex.getMessage(), ex);
            OperationResult result = OperationResult.failure(
                    id, System.currentTimeMillis() - start, ex);
            if (listener != null) listener.onCompleted(result);
            throw ex;
        } finally {
            ExecutionContext.clear();
        }
    }
}
