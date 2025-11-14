package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.repository.CustomerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    /**
     * Тест 1 - Получение покупателя "Ivan"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * <p>
     * Тест 2 - Получение покупателя "Oleg"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * - в метод getOrCreate была передана строка "Oleg"
     */
    @Test
    public void getExistingCustomerIvan() {
        CustomerRepository repo = mock(CustomerRepository.class);
        when(repo.getByName("Ivan")).thenReturn(new Customer("Ivan"));

        CustomerService service = new CustomerService(repo);

        Customer result = service.getOrCreate("Ivan");

        assertNotNull(result);
        assertEquals("Ivan", result.getName());

        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo, times(1)).getByName("Ivan");
        // метод add не вызывается, т.к. клиент существует
        verify(repo, never()).add(any());
    }

    @Test
    public void getOrCreateNewCustomerOleg() {
        CustomerRepository repo = mock(CustomerRepository.class);
        when(repo.getByName("Oleg")).thenReturn(null);
        when(repo.add(any(Customer.class))).thenAnswer(invocation -> {
            Customer cust = invocation.getArgument(0);
            cust.setId(999);
            return cust;
        });

        CustomerService service = new CustomerService(repo);

        Customer result = service.getOrCreate("Oleg");

        assertNotNull(result);
        assertEquals("Oleg", result.getName());
        assertEquals(999, result.getId());

        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo, times(1)).getByName("Oleg");
        inOrder.verify(repo, times(1)).add(any(Customer.class));
    }
}
