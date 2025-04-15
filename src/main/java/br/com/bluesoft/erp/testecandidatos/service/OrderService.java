package br.com.bluesoft.erp.testecandidatos.service;

import br.com.bluesoft.erp.testecandidatos.model.Order;
import br.com.bluesoft.erp.testecandidatos.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de pedidos.
 */
public interface OrderService {
    
    Order createOrder(Long customerId, List<OrderItem> items);
    
    Optional<Order> findOrderById(Long id);
    
    Optional<Order> findOrderByNumber(String orderNumber);
    
    List<Order> findAllOrders();
    
    List<Order> findOrdersByCustomerId(Long customerId);
    
    void addItemToOrder(Long orderId, OrderItem item);
    
    void removeItemFromOrder(Long orderId, Long itemId);
    
    void updateOrderItem(Long orderId, OrderItem item);
    
    BigDecimal calculateOrderTotal(Long orderId);
    
    void finalizeOrder(Long orderId);
    
    void cancelOrder(Long orderId);
}