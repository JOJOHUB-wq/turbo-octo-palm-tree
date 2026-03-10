package ua.hhtops.top;

public final class TopEntry {

    private final String playerName;
    private final double sortableValue;
    private final String displayValue;

    public TopEntry(String playerName, double sortableValue, String displayValue) {
        this.playerName = playerName;
        this.sortableValue = sortableValue;
        this.displayValue = displayValue;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getSortableValue() {
        return sortableValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
