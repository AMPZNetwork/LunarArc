package io.lunararcdevs.lunararc.launcher;

import java.lang.instrument.Instrumentation;

public class LunarArcAgent {

    private static final String INSTRUMENTATION_PROPERTY = "lunararc.agent.instrumentation";

    public static volatile Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.getProperties().put(INSTRUMENTATION_PROPERTY, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.getProperties().put(INSTRUMENTATION_PROPERTY, inst);
    }

    /** Safe from any classloader, unlike the {@link #instrumentation} field - see above. */
    public static Instrumentation sharedInstrumentation() {
        Object stashed = System.getProperties().get(INSTRUMENTATION_PROPERTY);
        return stashed instanceof Instrumentation inst ? inst : null;
    }
}
