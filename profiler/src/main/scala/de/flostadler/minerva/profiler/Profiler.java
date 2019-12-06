package de.flostadler.minerva.profiler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Profiler {

    private static final String PROC_SELF_CMDLINE_FILE = "/proc/self/cmdline";

    private MBeanServer platformMBeanServer;
    private ObjectName osObjectName;
    private MemoryMXBean memoryMXBean;
    private String role;

    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("finish")));

        try {
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            osObjectName = ObjectName.getInstance("java.lang:type=OperatingSystem");
        } catch (Throwable ex) {
//           logger.warn("Failed to get Operation System MBean", ex);
        }

        try {
            memoryMXBean = ManagementFactory.getMemoryMXBean();
        } catch (Throwable ex) {
//           logger.warn("Failed to get Memory MBean", ex);
        }

    }

    private String getRole() {
        if (role != null && !role.isEmpty()) {
            return role;
        }

        role = probeRole(getCmdline());
        return role;
    }

    public static String probeRole(String cmdline) {
        if (ProcessUtils.isSparkExecutor(cmdline)) {
            return "executor";
        } else if (ProcessUtils.isSparkDriver(cmdline)) {
            return "driver";
        } else {
            return null;
        }
    }

    public static String getCmdline() {
        try {
            File file = new File(PROC_SELF_CMDLINE_FILE);
            if (!file.exists() || file.isDirectory() || !file.canRead()) {
                return null;
            }

            String cmdline = new String(Files.readAllBytes(Paths.get(file.getPath())));
            cmdline = cmdline.replace((char) 0, ' ');
            return cmdline;
        } catch (Throwable ex) {
//            logger.warn("Failed to read file " + PROC_SELF_CMDLINE_FILE, ex);
            return null;
        }
    }
}
