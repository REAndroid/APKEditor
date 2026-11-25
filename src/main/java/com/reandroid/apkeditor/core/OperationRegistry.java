package com.reandroid.apkeditor.core;

import java.util.*;

public final class OperationRegistry {
    private final Map<String, Operation> operations = new LinkedHashMap<>();

    public OperationRegistry register(Operation operation) {
        Objects.requireNonNull(operation, "operation");
        operations.put(operation.getId(), operation);
        return this;
    }

    public Operation get(String id) {
        return operations.get(id);
    }

    public Collection<Operation> all() {
        return Collections.unmodifiableCollection(operations.values());
    }
}
