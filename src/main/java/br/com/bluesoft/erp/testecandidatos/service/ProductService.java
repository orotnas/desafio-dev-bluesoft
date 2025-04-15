package br.com.bluesoft.erp.testecandidatos.service;

import br.com.bluesoft.erp.testecandidatos.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de produtos.
 */
public interface ProductService {
    
    Product saveProduct(Product product);
    
    Optional<Product> findProductById(Long id);
    
    Optional<Product> findProductBySku(String sku);
    
    List<Product> findAllProducts();
    
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    void updateProductStock(Long productId, Integer newStock);
    
    void updateProductPrice(Long productId, BigDecimal newPrice);
    
    void deleteProduct(Long productId);
    
    BigDecimal calculateInventoryValue();
    
    List<Product> findProductsWithLowStock();
}