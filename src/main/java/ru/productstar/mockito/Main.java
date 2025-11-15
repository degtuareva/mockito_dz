package ru.productstar.mockito;

import ru.productstar.mockito.model.Order;
import ru.productstar.mockito.repository.InitRepository;
import ru.productstar.mockito.service.CustomerService;
import ru.productstar.mockito.service.OrderService;
import ru.productstar.mockito.service.WarehouseService;

public class Main {
    public static void main(String[] args) {
        OrderService orderService = getOrderService();
        Order order = orderService.create("OLGA");
        System.out.println("Создан заказ для клиента: " + order.getCustomer().getName());

        try {
            order = orderService.addProduct(order, "phone", 2, false);
            System.out.println("Общая сумма заказа после добавления телефона: " +
                    order.getTotal());
            order = orderService.addProduct(order, "laptop", 1, true);
            System.out.println("Общая сумма после добавления ноутбука с доставкой: " +
                    order.getTotal());
        } catch(Exception e) {
            System.out.println("Ошибка при добавлении продукта: " + e.getMessage());
        }
    }

    private static OrderService getOrderService() {
        CustomerService customerService = new
                CustomerService(InitRepository.getInstance().getCustomerRepository());
        WarehouseService warehouseService = new WarehouseService(
                InitRepository.getInstance().getWarehouseRepository());
        return new OrderService(customerService,
                warehouseService,
                InitRepository.getInstance().getOrderRepository(),
                InitRepository.getInstance().getProductRepository());
    }
}
