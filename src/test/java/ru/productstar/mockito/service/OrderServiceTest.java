package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.ProductNotFoundException;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.model.Order;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.OrderRepository;
import ru.productstar.mockito.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    /**
     * Покрыть тестами методы create и addProduct.
     * Можно использовать вызовы реальных методов.
     * <p>
     * Должны быть проверены следующие сценарии:
     * - создание ордера для существующего и нового клиента
     * - добавление существующего и несуществующего товара
     * - добавление товара в достаточном и не достаточном количестве
     * - заказ товара с быстрой доставкой
     * <p>
     * Проверки:
     * - общая сумма заказа соответствует ожидаемой
     * - корректная работа для несуществующего товара
     * - порядок и количество вызовов зависимых сервисов
     * - факт выбрасывания ProductNotFoundException
     */
    private CustomerService customerService;
    private WarehouseService warehouseService;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderService orderService;

    @BeforeEach
    void setup() {
        customerService = mock(CustomerService.class);
        warehouseService = mock(WarehouseService.class);
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);

        orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);
    }

    @Test
    void createOrderForExistingAndNewCustomer() {
        Customer existingCustomer = new Customer("Ivan");
        when(customerService.getOrCreate("Ivan")).thenReturn(existingCustomer);
        when(orderRepository.create(existingCustomer)).thenReturn(new Order(existingCustomer));

        Customer newCustomer = new Customer("Oleg");
        when(customerService.getOrCreate("Oleg")).thenReturn(newCustomer);
        when(orderRepository.create(newCustomer)).thenReturn(new Order(newCustomer));

        Order order1 = orderService.create("Ivan");
        Order order2 = orderService.create("Oleg");

        assertEquals(existingCustomer, order1.getCustomer());
        assertEquals(newCustomer, order2.getCustomer());

        verify(customerService, times(1)).getOrCreate("Ivan");
        verify(orderRepository, times(1)).create(existingCustomer);
        verify(customerService, times(1)).getOrCreate("Oleg");
        verify(orderRepository, times(1)).create(newCustomer);
    }

    @Test
    void addProductScenarios() throws ProductNotFoundException {
        Order order = new Order(new Customer("Ivan"));
        order.setId(0);

        Warehouse warehouse = mock(Warehouse.class);
        Product product = new Product("phone");

        when(orderRepository.addDelivery(eq(0), any())).thenAnswer(invocation -> {
            ru.productstar.mockito.model.Delivery delivery = invocation.getArgument(1);
            order.addDelivery(delivery);
            return order;
        });

        // существующий товар
        when(productRepository.getByName("phone")).thenReturn(product);

        // findWarehouse возвращает склад с достаточным количеством
        when(warehouseService.findWarehouse("phone", 1)).thenReturn(warehouse);
        when(warehouseService.getStock(warehouse, "phone")).thenReturn(new ru.productstar.mockito.model.Stock(product, 500, 10));

        Order resultOrder = orderService.addProduct(order, "phone", 1, false);
        assertEquals(500, resultOrder.getTotal());

        // findClosestWarehouse возвращает склад для быстрой доставки
        when(warehouseService.findClosestWarehouse("phone", 1)).thenReturn(warehouse);
        Order fastOrder = orderService.addProduct(order, "phone", 1, true);
        assertEquals(1000, fastOrder.getTotal());

        // товара нет
        when(warehouseService.findWarehouse("tv", 1)).thenReturn(null);
        ProductNotFoundException thrown = assertThrows(ProductNotFoundException.class, () -> {
            orderService.addProduct(order, "tv", 1, false);
        });
        assertTrue(thrown.getMessage().contains("tv not found"));

        // недостаточно товара
        when(warehouseService.findWarehouse("phone", 100)).thenReturn(null);
        ProductNotFoundException thrown2 = assertThrows(ProductNotFoundException.class, () -> {
            orderService.addProduct(order, "phone", 100, false);
        });
        assertTrue(thrown2.getMessage().contains("phone not found"));

        verify(warehouseService, times(3)).findWarehouse(anyString(), anyInt());
        verify(warehouseService, times(1)).findClosestWarehouse(anyString(), anyInt());
        verify(productRepository, times(2)).getByName(anyString());
        verify(orderRepository, times(2)).addDelivery(anyInt(), any());
    }
}
