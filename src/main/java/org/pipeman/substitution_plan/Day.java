package org.pipeman.substitution_plan;

public enum Day {
    TODAY("heutige"),
    TOMORROW("morgige");

    private final Object lock = new Object();
    private final String localization;

    Day(String localization) {
        this.localization = localization;
    }

    public String localization() {
        return localization;
    }

    public Object lock() {
        return lock;
    }
}
