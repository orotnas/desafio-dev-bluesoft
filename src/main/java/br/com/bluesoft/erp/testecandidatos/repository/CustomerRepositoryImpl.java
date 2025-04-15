package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.model.Customer;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação do repositório para a entidade Customer.
 */
@Repository
public class CustomerRepositoryImpl extends BaseRepositoryImpl<Customer, Long> implements CustomerRepository {

    @Override
    public Optional<Customer> findByEmail(String email) {
        TypedQuery<Customer> query = entityManager.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
        query.setParameter("email", email);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Customer> findByNameContaining(String name) {
        TypedQuery<Customer> query = entityManager.createQuery(
                "SELECT c FROM Customer c WHERE c.name LIKE '%" + name + "%'", Customer.class);
        return query.getResultList();
    }

    @Override
    public List<Customer> findCustomersWithOrders() {
        TypedQuery<Customer> query = entityManager.createQuery(
                "SELECT c FROM Customer c", Customer.class);
        List<Customer> allCustomers = query.getResultList();

        return allCustomers.stream()
                .filter(c -> c.getOrders() != null && !c.getOrders().isEmpty())
                .collect(Collectors.toList());
    }
}
