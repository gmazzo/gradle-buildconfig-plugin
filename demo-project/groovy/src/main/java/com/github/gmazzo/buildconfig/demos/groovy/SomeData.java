package com.github.gmazzo.buildconfig.demos.groovy;

import java.util.Objects;

public final class SomeData {
    private final String value1;
    private final int value2;

    public SomeData(String value1, int value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SomeData someData = (SomeData) o;
        return value2 == someData.value2 &&
                Objects.equals(value1, someData.value1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value1, value2);
    }

}
