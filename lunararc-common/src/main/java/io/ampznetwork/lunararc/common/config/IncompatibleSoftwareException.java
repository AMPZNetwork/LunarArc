package io.ampznetwork.lunararc.common.config;

public final class IncompatibleSoftwareException extends Error {
    public IncompatibleSoftwareException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
