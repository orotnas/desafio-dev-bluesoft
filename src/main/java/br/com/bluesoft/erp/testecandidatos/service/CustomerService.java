package br.com.bluesoft.erp.testecandidatos.service;

import br.com.bluesoft.erp.testecandidatos.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de clientes.
 */
public interface CustomerService {
    
    Customer saveCustomer(Customer customer);
    
    Optional<Customer> findCustomerById(Long id);
    
    Optional<Customer> findCustomerByEmail(String email);
    
    List<Customer> findAllCustomers();
    
    List<Customer> searchCustomersByName(String name);
    
    void updateCustomer(Customer customer);
    
    void deleteCustomer(Long customerId);
    
    List<Customer> findCustomersWithOrders();
    
    boolean validateCustomerEmail(String email);
}