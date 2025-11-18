package ru.productstar.delivery;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.productstar.mockito.delivery.DeliveryCostCalculator;
import ru.productstar.mockito.delivery.Size;
import ru.productstar.mockito.delivery.Workload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeliveryCostCalculatorTest {

    @Test
    @Tag("basic")
    void fragileOver30KmThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                DeliveryCostCalculator.calculateCost(31, Size.SMALL, true, Workload.LOW));
        assertEquals("Fragile goods cannot be delivered over 30 km", ex.getMessage());
    }

    @ParameterizedTest(name = "{index} => distance={0}, size={1}, fragile={2}, workload={3}, expected={4}")
    @CsvSource({
            "1, SMALL, false, LOW, 400",
            "5, LARGE, false, MODERATE, 400",
            "15, SMALL, true, HIGH, 840",
            "40, SMALL, false, VERY_HIGH, 640"
    })
    void testCalculateCostVariants(int distance, Size size, boolean fragile, Workload workload, int expected) {
        int actual = DeliveryCostCalculator.calculateCost(distance, size, fragile, workload);
        assertEquals(expected, actual);
    }
}
