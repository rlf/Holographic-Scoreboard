package dk.lockfuglsang.wolfencraft.stats;

import org.mcstats.Metrics;

/**
 */
public class CommandPlotter extends Metrics.Plotter {
    int value = 0;
    @Override
    public int getValue() {
        return value;
    }

    public CommandPlotter inc() {
        value++;
        return this;
    }
}
