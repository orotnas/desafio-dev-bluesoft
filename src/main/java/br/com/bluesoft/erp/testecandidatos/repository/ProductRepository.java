package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Product.
 */
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByPriceGreaterThan(BigDecimal minPrice);

    List<Product> searchByName(String name);

    List<Product> findProductsWithLowStock(Integer minStock);

}
