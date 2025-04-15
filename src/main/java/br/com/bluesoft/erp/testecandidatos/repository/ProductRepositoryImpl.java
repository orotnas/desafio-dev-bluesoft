package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.model.Product;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do repositório para a entidade Product.
 */
@Repository
public class ProductRepositoryImpl extends BaseRepositoryImpl<Product, Long> implements ProductRepository {

    @Override
    public Optional<Product> findBySku(String sku) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.sku = :sku", Product.class);
        query.setParameter("sku", sku);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findByPriceGreaterThan(BigDecimal minPrice) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE price > :minPrice", Product.class);
        query.setParameter("minPrice", minPrice);
        return query.getResultList();
    }

    @Override
    public List<Product> searchByName(String name) {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM products WHERE name LIKE '%" + name + "%'", Product.class);
        return query.getResultList();
    }

    @Override
    public List<Product> findProductsWithLowStock(Integer minStock) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.stock > :minStock", Product.class);
        query.setParameter("minStock", minStock);
        return query.getResultList();
    }
}
