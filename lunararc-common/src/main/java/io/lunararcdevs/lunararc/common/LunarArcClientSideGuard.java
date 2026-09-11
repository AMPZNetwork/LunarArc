package io.lunararcdevs.lunararc.common;

public final class LunarArcClientSideGuard {

    private LunarArcClientSideGuard() {
    }

    /**
     * Thrown when LunarArc is loaded on a client. Kept as its own type so the message is
     * recognisable in a crash report or a mod-loading error screen rather than reading like an
     * internal fault.
     */
    public static final class ClientSideNotSupportedException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        ClientSideNotSupportedException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public static void requireDedicatedServer(boolean clientSide) {
        if (!clientSide) return;
        throw new ClientSideNotSupportedException(MESSAGE);
    }

    public static final String MESSAGE = """

            LunarArc is a server-only mod and is not needed on the client.

            Remove it from this instance's mods folder to continue.
            """;
}
