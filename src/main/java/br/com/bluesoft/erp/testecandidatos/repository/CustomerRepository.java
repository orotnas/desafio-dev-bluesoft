package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Customer.
 */
@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByNameContaining(String name);
    
    List<Customer> findCustomersWithOrders();
}