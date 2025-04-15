package br.com.bluesoft.erp.testecandidatos.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.bluesoft.erp.testecandidatos.TesteApplication;
import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import br.com.bluesoft.erp.testecandidatos.model.Customer;
import br.com.bluesoft.erp.testecandidatos.model.Order;
import br.com.bluesoft.erp.testecandidatos.model.OrderItem;
import br.com.bluesoft.erp.testecandidatos.model.Product;
import br.com.bluesoft.erp.testecandidatos.repository.CustomerRepository;
import br.com.bluesoft.erp.testecandidatos.repository.ProductRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testes de integração para o serviço de pedidos.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @PersistenceContext
    private EntityManager entityManager;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderItem orderItem;

    @Before
    public void setUp() {
        // Limpa quaisquer dados existentes
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM Product").executeUpdate();
        entityManager.createQuery("DELETE FROM Customer").executeUpdate();

        // Cria cliente para testes
        customer = new Customer();
        customer.setName("João Silva");
        customer.setEmail("joao.silva@example.com");
        customer.setPhone("(11) 99999-1111");
        customer.setOrders(new ArrayList<>());
        entityManager.persist(customer);

        // Cria produto para testes
        product = new Product();
        product.setName("Produto 1");
        product.setDescription("Descrição do Produto 1");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setSku("SKU001");
        entityManager.persist(product);

        // Cria pedido para testes
        order = new Order();
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);
        entityManager.persist(order);

        // Adiciona o pedido à lista de pedidos do cliente
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        customer.setOrders(orders);
        entityManager.persist(customer);

        // Cria item de pedido para testes
        orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        orderItem.setSubtotal(new BigDecimal("20.00"));
        entityManager.persist(orderItem);

        // Adiciona o item ao pedido
        order.getItems().add(orderItem);
        entityManager.persist(order);

        // Garante que as alterações sejam persistidas
        entityManager.flush();
    }

    @Test
    public void testCreateOrder() {
        // Cria lista de itens para o pedido
        List<OrderItem> items = new ArrayList<>();

        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(3);
        newItem.setUnitPrice(new BigDecimal("10.00"));
        items.add(newItem);

        // Executa o método
        Order createdOrder = orderService.createOrder(customer.getId(), items);

        // Verifica o resultado
        assertNotNull("Pedido criado não deveria ser nulo", createdOrder);
        assertNotNull("Pedido criado deveria ter um ID", createdOrder.getId());
        assertEquals("Pedido deveria ter o cliente correto", customer.getId(), createdOrder.getCustomer().getId());
        assertEquals("Pedido deveria ter 1 item", 1, createdOrder.getItems().size());

        // Verifica se o pedido foi realmente salvo no banco de dados
        Optional<Order> foundOrder = orderService.findOrderById(createdOrder.getId());
        assertTrue("Pedido deveria ser encontrado no banco de dados", foundOrder.isPresent());
    }

    @Test
    public void testFindOrderById() {
        // Executa o método
        Optional<Order> foundOrder = orderService.findOrderById(order.getId());

        // Verifica o resultado
        assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
        assertEquals("Pedido encontrado deveria ter o ID correto", order.getId(), foundOrder.get().getId());
        assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", foundOrder.get().getOrderNumber());
    }

    @Test
    public void testFindOrderByNumber() {
        // Executa o método
        Optional<Order> foundOrder = orderService.findOrderByNumber("ORD-001");

        // Verifica o resultado
        assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
        assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", foundOrder.get().getOrderNumber());
    }

    @Test
    public void testFindAllOrders() {
        // Executa o método
        List<Order> orders = orderService.findAllOrders();

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 pedido", 1, orders.size());
        assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", orders.get(0).getOrderNumber());
    }

    @Test
    public void testFindOrdersByCustomerId() {
        // Executa o método
        List<Order> orders = orderService.findOrdersByCustomerId(customer.getId());

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 pedido", 1, orders.size());
        assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", orders.get(0).getOrderNumber());
    }

    @Test
    public void testAddItemToOrder() {
        // Cria um novo item para adicionar
        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(1);
        newItem.setUnitPrice(new BigDecimal("10.00"));

        // Executa o método
        orderService.addItemToOrder(order.getId(), newItem);

        // Verifica se o item foi adicionado ao pedido
        Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
        assertEquals("Pedido deveria ter 2 itens", 2, updatedOrder.get().getItems().size());
    }

    @Test
    public void testRemoveItemFromOrder() {
        // Executa o método
        orderService.removeItemFromOrder(order.getId(), orderItem.getId());

        // Verifica se o item foi removido do pedido
        Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
        assertEquals("Pedido não deveria ter itens", 0, updatedOrder.get().getItems().size());
    }

    @Test
    public void testUpdateOrderItem() {
        // Modifica o item
        orderItem.setQuantity(5);
        orderItem.setUnitPrice(new BigDecimal("12.00"));

        // Executa o método
        orderService.updateOrderItem(order.getId(), orderItem);

        // Verifica se o item foi atualizado
        Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
        assertEquals("Item deveria ter a quantidade atualizada", 5, (int) updatedOrder.get().getItems().get(0).getQuantity());
        assertEquals("Item deveria ter o preço unitário atualizado", new BigDecimal("12.00"), updatedOrder.get().getItems().get(0).getUnitPrice());
    }

    @Test
    public void testCalculateOrderTotal() {
        // Executa o método
        BigDecimal total = orderService.calculateOrderTotal(order.getId());

        // Verifica o resultado
        assertEquals("Valor total deveria ser 20.00", new BigDecimal("20.00"), total);
    }

    @Test
    public void testFinalizeOrder() {
        // Executa o método
        orderService.finalizeOrder(order.getId());

        // Verifica se o pedido foi finalizado
        Optional<Order> finalizedOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", finalizedOrder.isPresent());
        assertEquals("Valor total do pedido deveria ser 20.00", new BigDecimal("20.00"), finalizedOrder.get().getTotalAmount());

        // Verifica se o estoque do produto foi atualizado
        Optional<Product> updatedProduct = productRepository.findById(product.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Estoque do produto deveria ser atualizado", 98, updatedProduct.get().getStock().intValue());
    }

    @Test
    public void testCancelOrder() {
        // Executa o método
        orderService.cancelOrder(order.getId());

        // Verifica se o pedido foi cancelado
        Optional<Order> canceledOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", canceledOrder.isPresent());

        assertEquals("Status do pedido deveria ser CANCELADO", "CANCELADO", canceledOrder.get().getStatus().name());

        Optional<Product> updatedProduct = productRepository.findById(product.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Estoque do produto deveria ser restaurado", 100, (int) updatedProduct.get().getStock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrderWithInvalidCustomerId() {
        // Executa o método - deve lançar IllegalArgumentException
        orderService.createOrder(999L, new ArrayList<>());
    }

    @Test
    public void testOrderNumberUniqueness() {
        // Cria dois pedidos no mesmo dia
        Order order1 = orderService.createOrder(customer.getId(), new ArrayList<>());
        Order order2 = orderService.createOrder(customer.getId(), new ArrayList<>());

        // Verifica se os números dos pedidos são diferentes
        assertNotEquals("Os números dos pedidos deveriam ser diferentes",
                        order1.getOrderNumber(), order2.getOrderNumber());
    }

    @Test
    public void testNegativeQuantityInOrderItem() {
        // Cria um item com quantidade negativa
        OrderItem negativeItem = new OrderItem();
        negativeItem.setProduct(product);
        negativeItem.setQuantity(-5); // Quantidade negativa
        negativeItem.setUnitPrice(new BigDecimal("10.00"));

        // Tenta adicionar o item ao pedido
        orderService.addItemToOrder(order.getId(), negativeItem);

        // Verifica se o item foi adicionado (não deveria ser possível com quantidade negativa)
        Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
        assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());

        // Procura pelo item com quantidade negativa
        boolean foundNegativeItem = false;
        for (OrderItem item : updatedOrder.get().getItems()) {
            if (item.getQuantity() != null && item.getQuantity() < 0) {
                foundNegativeItem = true;
                break;
            }
        }

        assertFalse("Não deveria ser possível adicionar um item com quantidade negativa", foundNegativeItem);
    }
}
