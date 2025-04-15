package br.com.bluesoft.erp.testecandidatos.service;

import br.com.bluesoft.erp.testecandidatos.model.Customer;
import br.com.bluesoft.erp.testecandidatos.model.Order;
import br.com.bluesoft.erp.testecandidatos.model.OrderItem;
import br.com.bluesoft.erp.testecandidatos.model.Product;
import br.com.bluesoft.erp.testecandidatos.model.Status;
import br.com.bluesoft.erp.testecandidatos.repository.CustomerRepository;
import br.com.bluesoft.erp.testecandidatos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do serviço para gerenciamento de pedidos.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Autowired
    public OrderServiceImpl(CustomerRepository customerRepository, 
                           ProductRepository productRepository,
                           ProductService productService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public Order createOrder(Long customerId, List<OrderItem> items) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (!customerOpt.isPresent()) {
            throw new IllegalArgumentException("Cliente não encontrado");
        }

        Customer customer = customerOpt.get();

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber());


        // Persiste o pedido antes de adicionar os itens
        entityManager.persist(order);

        // Adiciona os itens ao pedido
        if (items != null && !items.isEmpty()) {
            for (OrderItem item : items) {

                // Associa o item ao pedido
                item.setOrder(order);



                entityManager.persist(item);

            }
        }

        // order.updateTotalAmount();

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderByNumber(String orderNumber) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.orderNumber = :orderNumber", Order.class);
        query.setParameter("orderNumber", orderNumber);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o", Order.class);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByCustomerId(Long customerId) {
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.customer.id = :customerId", Order.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addItemToOrder(Long orderId, OrderItem item) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Order order = orderOpt.get();

        // Associa o item ao pedido
        item.setOrder(order);

        // Persiste o item
        entityManager.persist(item);
    }

    @Override
    @Transactional
    public void removeItemFromOrder(Long orderId, Long itemId) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Order order = orderOpt.get();

        OrderItem itemToRemove = null;
        for (OrderItem item : order.getItems()) {
            if (item.getId().equals(itemId)) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove != null) {
            // Remove o item
            entityManager.remove(itemToRemove);
        }
    }

    @Override
    @Transactional
    public void updateOrderItem(Long orderId, OrderItem item) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        // item.updateSubtotal();

        entityManager.merge(item);

        // orderOpt.get().updateTotalAmount();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(Long orderId) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Order order = orderOpt.get();

        return order.calculateTotal();
    }

    @Override
    @Transactional
    public void finalizeOrder(Long orderId) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Order order = orderOpt.get();

        // Atualiza o valor total do pedido
        BigDecimal total = order.calculateTotal();
        order.setTotalAmount(total);

        order.setStatus(Status.FINALIZADO);
        entityManager.merge(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Optional<Order> orderOpt = findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Order order = orderOpt.get();
        entityManager.merge(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().getDayOfMonth() + "0000";
    }
}
