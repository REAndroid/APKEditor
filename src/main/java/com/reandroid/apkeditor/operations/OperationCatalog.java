package com.reandroid.apkeditor.operations;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.core.LegacyOptionsOperation;
import com.reandroid.apkeditor.compile.BuildOptions;
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.apkeditor.info.InfoOptions;
import com.reandroid.apkeditor.merge.MergerOptions;
import com.reandroid.apkeditor.protect.ProtectorOptions;
import com.reandroid.apkeditor.refactor.RefactorOptions;

/**
 * Punto único para registrar las operaciones que ya existen en el motor.
 * Aquí no se dibuja ninguna ventana.
 */
public final class OperationCatalog {
    private OperationCatalog() {}

    public static OperationRegistry create() {
        return new OperationRegistry()
                .register(new LegacyOptionsOperation("decode", "Decode APK", new DecompileOptions()))
                .register(new LegacyOptionsOperation("build", "Build APK", new BuildOptions()))
                .register(new LegacyOptionsOperation("merge", "Merge APK", new MergerOptions()))
                .register(new LegacyOptionsOperation("refactor", "Refactor", new RefactorOptions()))
                .register(new LegacyOptionsOperation("protect", "Protect", new ProtectorOptions()))
                .register(new LegacyOptionsOperation("info", "APK Info", new InfoOptions()));
    }
}
