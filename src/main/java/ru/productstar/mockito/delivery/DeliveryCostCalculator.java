package ru.productstar.mockito.delivery;

public class DeliveryCostCalculator {

    public static int calculateCost(int distanceKm, Size size, boolean fragile, Workload workload) {
        if (fragile && distanceKm > 30) {
            throw new IllegalArgumentException("Fragile goods cannot be delivered over 30 km");
        }

        int baseCost;
        if (distanceKm > 30) baseCost = 300;
        else if (distanceKm > 10) baseCost = 200;
        else if (distanceKm > 2) baseCost = 100;
        else baseCost = 50;

        int sizeCost = (size == Size.LARGE) ? 200 : 100;
        int fragileCost = fragile ? 300 : 0;

        int total = baseCost + sizeCost + fragileCost;

        total = (int) Math.round(total * workload.getCoefficient());

        return Math.max(total, 400);
    }
}