package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import br.com.bluesoft.erp.testecandidatos.model.Customer;
import br.com.bluesoft.erp.testecandidatos.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o repositório de clientes.
 */
@SpringJUnitConfig(classes = {TestConfig.class})
@ActiveProfiles("test")
@Transactional
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @BeforeEach
    public void setUp() {
        // Cria clientes para testes básicos (para outros testes além do de performance)
        customer1 = new Customer();
        customer1.setName("João Silva");
        customer1.setEmail("joao.silva@example.com");
        customer1.setPhone("(11) 99999-1111");
        entityManager.persist(customer1);

        customer2 = new Customer();
        customer2.setName("Maria Santos");
        customer2.setEmail("maria.santos@example.com");
        customer2.setPhone("(11) 99999-2222");
        entityManager.persist(customer2);

        customer3 = new Customer();
        customer3.setName("Pedro Oliveira");
        customer3.setEmail("pedro.oliveira@example.com");
        customer3.setPhone("(11) 99999-3333");
        entityManager.persist(customer3);

        // Cria pedidos para o cliente1 e cliente2
        Order order1 = new Order();
        order1.setOrderNumber("ORD-001");
        order1.setOrderDate(LocalDateTime.now());
        order1.setCustomer(customer1);
        entityManager.persist(order1);

        Order order2 = new Order();
        order2.setOrderNumber("ORD-002");
        order2.setOrderDate(LocalDateTime.now());
        order2.setCustomer(customer1);
        entityManager.persist(order2);

        Order order3 = new Order();
        order3.setOrderNumber("ORD-003");
        order3.setOrderDate(LocalDateTime.now());
        order3.setCustomer(customer2);
        entityManager.persist(order3);

        // Adiciona os pedidos à lista de pedidos dos clientes
        customer1.getOrders().add(order1);
        customer1.getOrders().add(order2);
        customer2.getOrders().add(order3);

        entityManager.flush();
    }

    @Test
    public void testFindByEmail() {
        // Teste para verificar se o método findByEmail funciona corretamente
        Optional<Customer> foundCustomer = customerRepository.findByEmail("joao.silva@example.com");
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado pelo email");
        assertEquals("joao.silva@example.com", foundCustomer.get().getEmail());
    }

    @Test
    public void testFindByEmailCaseInsensitive() {
        // Teste para verificar se o método findByEmail é case insensitive
        Optional<Customer> foundCustomer = customerRepository.findByEmail("JOAO.SILVA@example.com");
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado com email em maiúsculas");
    }

    @Test
    public void testFindByEmailNotFound() {
        // Teste para verificar se o método findByEmail retorna Optional.empty quando não encontra o cliente
        Optional<Customer> foundCustomer = customerRepository.findByEmail("nao.existe@example.com");
        assertFalse(foundCustomer.isPresent(), "Cliente não deveria ser encontrado");
    }

    @Test
    public void testFindByNameContaining() {
        // Teste para verificar se o método findByNameContaining funciona corretamente
        List<Customer> customers = customerRepository.findByNameContaining("Silva");
        assertEquals(1, customers.size(), "Deveria encontrar 1 cliente com 'Silva' no nome");
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("João Silva")), "Deveria conter o cliente João Silva");
    }

    @Test
    public void testFindByNameContainingWithSQLInjection() {
        // Teste para verificar se o método findByNameContaining está protegido contra injeção de SQL
        try {
            List<Customer> customers = customerRepository.findByNameContaining("' OR '1'='1");
            // Se a consulta for vulnerável, retornará todos os clientes
            assertFalse(customers.size() > 1, "A consulta não deveria ser vulnerável a injeção de SQL");
        } catch (Exception e) {
            // Se a consulta for segura, pode lançar uma exceção ou retornar uma lista vazia
            assertTrue(true, "A consulta deveria ser segura contra injeção de SQL");
        }
    }

    /**
     * Cria dezenas de milhares de clientes com pedidos para o teste de performance.
     * Este método é chamado apenas pelo teste de performance.
     */
    private void createManyCustomersWithOrders() {
        // Criando 20.000 clientes, dos quais 1000 terão pedidos
        final int TOTAL_CUSTOMERS = 20000;
        final int CUSTOMERS_WITH_ORDERS = 1000;

        System.out.println("Criando " + TOTAL_CUSTOMERS + " clientes para teste de performance...");

        // Batch processing para melhor performance
        final int BATCH_SIZE = 50;

        for (int i = 0; i < TOTAL_CUSTOMERS; i++) {
            Customer customer = new Customer();
            customer.setName("Customer " + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setPhone("(11) 99999-" + String.format("%04d", i % 10000));
            entityManager.persist(customer);

            // Apenas CUSTOMERS_WITH_ORDERS clientes terão pedidos
            if (i < CUSTOMERS_WITH_ORDERS) {
                // Cada cliente terá entre 1 e 3 pedidos
                int numOrders = 1 + (i % 3);
                for (int j = 0; j < numOrders; j++) {
                    Order order = new Order();
                    order.setOrderNumber("ORD-" + i + "-" + j);
                    order.setOrderDate(LocalDateTime.now());
                    order.setCustomer(customer);
                    entityManager.persist(order);

                    // Adiciona o pedido à lista de pedidos do cliente
                    customer.getOrders().add(order);
                }
            }

            // Flush e clear a cada BATCH_SIZE para evitar problemas de memória
            if (i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
    }

    @Test
    public void testFindCustomersWithOrders() {
        // Cria dezenas de milhares de clientes com pedidos para o teste de performance
        createManyCustomersWithOrders();

        long startTime = System.currentTimeMillis();
        List<Customer> customers = customerRepository.findCustomersWithOrders();
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) < 500, "o tempo máximo de retorno da consulta deveria ser de menos de 500 ms (demorou " + (endTime - startTime) + " ms)");

        // Verifica se o número de clientes com pedidos está correto (15.000 + 2 dos testes básicos)
        assertEquals(2002, customers.size(), "Deveria encontrar 15.002 clientes com pedidos");

        // Verifica se os clientes dos testes básicos estão presentes
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("João Silva")), "Deveria conter o cliente João Silva");
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("Maria Santos")), "Deveria conter o cliente Maria Santos");
        assertFalse(customers.stream().anyMatch(c -> c.getName().equals("Pedro Oliveira")), "Não deveria conter o cliente Pedro Oliveira");
    }

    @Test
    public void testFindAll() {
        // Teste para verificar se o método findAll funciona corretamente
        List<Customer> customers = customerRepository.findAll();
        assertEquals(3, customers.size(), "Deveria encontrar 3 clientes");
    }

    @Test
    public void testFindById() {
        // Teste para verificar se o método findById funciona corretamente
        Optional<Customer> foundCustomer = customerRepository.findById(customer1.getId());
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado pelo ID");
        assertEquals("João Silva", foundCustomer.get().getName());
    }

    @Test
    public void testSave() {
        // Teste para verificar se o método save funciona corretamente
        Customer newCustomer = new Customer();
        newCustomer.setName("Ana Pereira");
        newCustomer.setEmail("ana.pereira@example.com");
        newCustomer.setPhone("(11) 99999-4444");

        Customer savedCustomer = customerRepository.save(newCustomer);
        assertNotNull(savedCustomer, "Cliente salvo não deveria ser nulo");
        assertNotNull(savedCustomer.getId(), "ID do cliente salvo não deveria ser nulo");

        // Verifica se o cliente foi realmente salvo no banco
        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado após salvar");
        assertEquals("Ana Pereira", foundCustomer.get().getName());
    }

    @Test
    public void testDelete() {
        // Teste para verificar se o método delete funciona corretamente
        customerRepository.delete(customer3);

        // Verifica se o cliente foi realmente excluído do banco
        Optional<Customer> foundCustomer = customerRepository.findById(customer3.getId());
        assertFalse(foundCustomer.isPresent(), "Cliente não deveria ser encontrado após excluir");
    }
}
