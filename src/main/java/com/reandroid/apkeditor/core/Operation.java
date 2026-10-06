package com.reandroid.apkeditor.core;

/**
 * Unidad de trabajo independiente de la interfaz.
 * CLI y GUI pueden ejecutar las mismas operaciones.
 */
public interface Operation {
    String getId();
    String getName();
    OperationResult execute(OperationListener listener) throws Exception;
}
