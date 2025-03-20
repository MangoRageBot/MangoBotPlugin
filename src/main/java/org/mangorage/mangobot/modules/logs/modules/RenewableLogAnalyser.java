package org.mangorage.mangobot.modules.logs.modules;

import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

import java.util.function.Supplier;

public final class RenewableLogAnalyser implements LogAnalyserModule {
    private final Supplier<LogAnalyserModule> supplier;

    public RenewableLogAnalyser(Supplier<LogAnalyserModule> logAnalyserModuleSupplier) {
        this.supplier = logAnalyserModuleSupplier;
    }

    @Override
    public void analyse(String str, StringBuilder message) {
        supplier.get().analyse(str, message);
    }
}
