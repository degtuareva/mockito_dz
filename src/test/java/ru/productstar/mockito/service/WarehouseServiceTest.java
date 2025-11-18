package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.delivery.Size;
import ru.productstar.mockito.delivery.Workload;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    /**
     * Покрыть тестами методы findWarehouse и findClosestWarehouse.
     * Вызывать реальные методы зависимых сервисов и репозиториев нельзя.
     * Поиск должен осуществляться как минимум на трех складах.
     * <p>
     * Должны быть проверены следующие сценарии:
     * - поиск несуществующего товара
     * - поиск существующего товара с достаточным количеством
     * - поиск существующего товара с недостаточным количеством
     * <p>
     * Проверки:
     * - товар находится на нужном складе, учитывается количество и расстояние до него
     * - корректная работа для несуществующего товара
     * - порядок и количество вызовов зависимых сервисов
     */
    private WarehouseRepository warehouseRepository;
    private WarehouseService warehouseService;

    @BeforeEach
    void setup() {
        warehouseRepository = mock(WarehouseRepository.class);
        warehouseService = new WarehouseService(warehouseRepository);
    }

    @Test
    void findWarehouseTests() {
        Warehouse wh1 = new Warehouse("wh1", 10);
        Warehouse wh2 = new Warehouse("wh2", 20);
        Warehouse wh3 = new Warehouse("wh3", 30);

        wh1.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 5));
        wh2.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 15));
        wh3.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 1));

        when(warehouseRepository.all()).thenReturn(List.of(wh1, wh2, wh3));

        Warehouse result1 = warehouseService.findWarehouse("phone", 10);
        assertNotNull(result1);
        assertEquals("wh2", result1.getName());

        Warehouse result2 = warehouseService.findWarehouse("phone", 2);
        assertNotNull(result2);
        assertEquals("wh1", result2.getName());

        Warehouse result3 = warehouseService.findWarehouse("tv", 1);
        assertNull(result3);

        verify(warehouseRepository, atLeastOnce()).all();
    }

    @Test
    void findClosestWarehouseTests() {
        Warehouse wh1 = new Warehouse("wh1", 10);
        Warehouse wh2 = new Warehouse("wh2", 20);
        Warehouse wh3 = new Warehouse("wh3", 5);

        wh1.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 5));
        wh2.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 15));
        wh3.addStock(new Stock(new ru.productstar.mockito.model.Product("phone"), 100, 1));

        when(warehouseRepository.all()).thenReturn(List.of(wh1, wh2, wh3));

        Warehouse closest = warehouseService.findClosestWarehouse("phone", 3);
        assertNotNull(closest);
        assertEquals("wh1", closest.getName());

        Warehouse closest2 = warehouseService.findClosestWarehouse("phone", 10);
        assertNotNull(closest2);
        assertEquals("wh2", closest2.getName());

        Warehouse closest3 = warehouseService.findClosestWarehouse("tv", 1);
        assertNull(closest3);

        verify(warehouseRepository, atLeastOnce()).all();
    }

    @Test
    void canCreateWarehouseServiceWithRepository() {
        WarehouseRepository repo = mock(WarehouseRepository.class);
        WarehouseService svc = new WarehouseService(repo);
        assertNotNull(svc);
    }

    @Test
    void getStockReturnsCorrectStock() {
        Product someProduct = new Product("phone");
        Stock stock = new Stock(someProduct, 10, 1000);
        List<Stock> stocks = new ArrayList<>();
        stocks.add(stock);
        Warehouse warehouse = new Warehouse(stocks);
        WarehouseService svc = new WarehouseService();
        Stock result = svc.getStock(warehouse, "phone");
        assertEquals(stock, result);
    }

    @Test
    void testCalculateDeliveryCostDelegatesCorrectly() {
        WarehouseService svc = new WarehouseService();
        int cost = svc.calculateDeliveryCost(5, Size.LARGE, false, Workload.MODERATE);
        assertEquals(400, cost);
    }
}
