package de.flostadler.minerva.profiler;

import de.flostadler.minerva.profiler.reporter.Reporter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.CheckedFunction1.liftTry;
import static java.util.stream.Collectors.toMap;

public class Arguments {

    private static final String REPORTER_KEY = "reporter";

    private Class<Reporter> reporterClass;
    private Properties properties;

    public Arguments(Class<Reporter> reporterClass, Properties properties) {
        this.reporterClass = reporterClass;
        this.properties = properties;
    }

    public Class<Reporter> getReporterClass() {
        return reporterClass;
    }

    public void setReporterClass(Class<Reporter> reporterClass) {
        this.reporterClass = reporterClass;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static Arguments parse(String args) {
        Set<String[]> argumentMappings = Arrays.stream(args.split(";"))
                .map(s -> s.split("="))
                .filter(strings -> strings.length == 2)
                .collect(Collectors.toSet());

        Properties properties = new Properties();

        properties.putAll(argumentMappings.stream().collect(toMap(s -> s[0], s -> s[1], (a, b) -> a)));

        Class<Reporter> reporterClass = Optional.ofNullable(properties.getProperty(REPORTER_KEY))
                .map(liftTry(Class::forName))
                .map(tri -> tri.getOrElseThrow(() -> new IllegalArgumentException("Reporter class could not be loaded!")))
                .map(liftTry(c -> (Class<Reporter>) c))
                .map(tri -> tri.getOrElseThrow(() -> new IllegalArgumentException("Given reporter class is not of type 'Reporter'")))
                .orElseThrow(() -> new IllegalArgumentException("Reporter not defined!"));

        return new Arguments(reporterClass, properties);
    }

}
