/*
 * Copyright (c) 2018 Ellis Michael (emichael@cs.washington.edu)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dslabs.framework.testing.utils;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public abstract class GlobalSettings {
    private static final Properties props = System.getProperties();

    private static final String TEST_NUM = "testNum", LOG_LEVEL = "logLevel";

    @Getter private static final boolean verbose =
            Boolean.parseBoolean(lookupWithDefault("verbose", "true")),

    simulated = Boolean.parseBoolean(lookupWithDefault("simulated", "false")),

    singleThreaded =
            Boolean.parseBoolean(lookupWithDefault("singleThreaded", "false")),

    startVisualization =
            Boolean.parseBoolean(lookupWithDefault("startViz", "false"));

    @Getter private static final long seed =
            Long.parseLong(lookupWithDefault("seed", "117418"));
    // the "single source of randomness", all other `Random` instances all seeded from this
    // not sharing this `rand` for performance, and not seeded directly from 
    // `seed` to avoid two identically-used `Random` generating identical sequences
    // however this results in different sequences when running same test from 
    // different sets of tests, feels ok for this now
    @Getter private static final Random rand = new Random(seed);

    @Getter @Setter private static boolean saveTraces =
            Boolean.parseBoolean(lookupWithDefault("saveTraces", "false"));

    private static final boolean doChecks =
            Boolean.parseBoolean(lookupWithDefault("doChecks", "false"));

    @Setter private static boolean errorChecksTemporarilyEnabled = false;

    private static final boolean timeoutsDisabled = Boolean.parseBoolean(
            lookupWithDefault("testTimeoutsDisabled", "false"));

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%4$-7s] [%1$tF %1$tT] [%3$s] %5$s%6$s%n");

        // Configure logging
        LogManager logManager = LogManager.getLogManager();
        Logger logger = logManager.getLogger("");

        // Remove existing handlers
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }

        // Setup formatter
        ConsoleHandler handler = new ConsoleHandler();

        // Set level
        String name = lookupWithDefault(LOG_LEVEL, "WARNING");
        Level level;
        try {
            level = Level.parse(name);
        } catch (IllegalArgumentException ignored) {
            level = Level.WARNING;
        }
        handler.setLevel(level);
        logger.setLevel(level);

        // Add new handler
        logger.addHandler(handler);
    }

    public static boolean timeoutsEnabled() {
        // First, if we're in debug mode, timeouts are always disabled
        List<String> arguments =
                ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : arguments) {
            if (arg.equals("-Xdebug") || arg.startsWith("-agentlib:jdwp")) {
                return false;
            }
        }

        // Next, if there's a command-line option, return that
        return !timeoutsDisabled;
    }

    private static String lookupWithDefault(String keyName,
                                            String defaultValue) {
        if (props.containsKey(keyName)) {
            return props.getProperty(keyName);
        }
        return defaultValue;
    }

    public static boolean doAllChecks() {
        return doChecks;
    }

    public static boolean doErrorChecks() {
        return doChecks || errorChecksTemporarilyEnabled;
    }
}
