package ua.hhtops.top;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Statistic;

public enum TopCategory {
    KILLS("kills", "Убийства", "minecraft:player_kills", Statistic.PLAYER_KILLS),
    DEATHS("deaths", "Смерти", "minecraft:deaths", Statistic.DEATHS),
    TIME("time", "Время в игре", "minecraft:play_one_minute", Statistic.PLAY_ONE_MINUTE),
    MONEY("money", "Деньги", null, null);

    private static final List<String> ARGUMENTS;

    static {
        List<String> arguments = new ArrayList<String>();
        for (TopCategory category : values()) {
            arguments.add(category.argument);
        }
        ARGUMENTS = Collections.unmodifiableList(arguments);
    }

    private final String argument;
    private final String displayName;
    private final String statisticKey;
    private final Statistic statistic;

    TopCategory(String argument, String displayName, String statisticKey, Statistic statistic) {
        this.argument = argument;
        this.displayName = displayName;
        this.statisticKey = statisticKey;
        this.statistic = statistic;
    }

    public String getArgument() {
        return argument;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatisticKey() {
        return statisticKey;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public boolean hasStatistic() {
        return statistic != null && statisticKey != null;
    }

    public static List<String> arguments() {
        return ARGUMENTS;
    }

    public static TopCategory fromArgument(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (TopCategory category : values()) {
            if (category.argument.equalsIgnoreCase(normalized)) {
                return category;
            }
        }
        return null;
    }
}
