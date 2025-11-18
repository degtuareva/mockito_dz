package ru.productstar.mockito.delivery;

public enum Workload {
    VERY_HIGH(1.6), HIGH(1.4), MODERATE(1.2), LOW(1.0);

    private final double coefficient;

    Workload(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getCoefficient() {
        return coefficient;
    }
}