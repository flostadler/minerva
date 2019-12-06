package de.flostadler.minerva.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {
    private static final String SPARK_PROCESS_KEYWORD = "spark.yarn.app.container.log.dir";
    private static final String SPARK_CMDLINE_KEYWORD = "spark.";
    private static final String SPARK_EXECUTOR_CLASS_NAME = "spark.executor.CoarseGrainedExecutorBackend";
    private static final String SPARK_EXECUTOR_KEYWORD = "spark.driver.port";


    public static String getCurrentProcessName() {
        try {
            return ManagementFactory.getRuntimeMXBean().getName();
        } catch (Throwable ex) {
            return ex.getMessage();
        }
    }

    public static List<String> getJvmInputArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = runtimeMXBean.getInputArguments();
        return jvmArgs == null ? new ArrayList<>() : jvmArgs;
    }

    public static boolean isSparkProcess(String cmdline) {
        if (cmdline != null && !cmdline.isEmpty()) {
            if (cmdline.contains(SPARK_CMDLINE_KEYWORD)) {
                return true;
            }
        }

        List<String> strList = ProcessUtils.getJvmInputArguments();
        for (String str : strList) {
            if (str.toLowerCase().contains(SPARK_PROCESS_KEYWORD.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSparkExecutor(String cmdline) {
        if (cmdline != null && !cmdline.isEmpty()) {
            if (cmdline.contains(SPARK_EXECUTOR_CLASS_NAME)) {
                return true;
            }
        }

        List<String> strList = ProcessUtils.getJvmInputArguments();
        for (String str : strList) {
            if (str.toLowerCase().contains(SPARK_EXECUTOR_KEYWORD.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSparkDriver(String cmdline) {
        return isSparkProcess(cmdline) && !isSparkExecutor(cmdline);
    }

    public static void main(String[] args) {
        System.out.println(getCurrentProcessName());
        System.out.println(isSparkProcess(null));
        System.out.println(isSparkExecutor(null));
        System.out.println(isSparkDriver(null));
    }
}
