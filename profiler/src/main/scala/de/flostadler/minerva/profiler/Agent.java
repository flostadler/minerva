package de.flostadler.minerva.profiler;

import java.lang.instrument.Instrumentation;

public final class Agent {

    private Agent() {

    }

    public static void agentmain(final String args, final Instrumentation instrumentation) {
        premain(args, instrumentation);
    }

    public static void premain(final String args, final Instrumentation instrumentation) {
        Arguments arguments = Arguments.parse(args);


    }
}
