package se.dandel.tools.classdepanalyzer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DependencyTrackerTest {

    private String includes = "net.java.cargotracker,org.joda.time";

    @Test
    public void includes() throws Exception {
        assertTrue(isAllowed("net.java.cargotracker.application.internal.DefaultCargoInspectionService"));
        assertTrue(isAllowed("org.joda.time.DateTime"));
    }

    private boolean isAllowed(String name) {
        String[] split = includes.split(",");
        for (String string : split) {
            if (name.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

}
