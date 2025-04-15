package br.com.bluesoft.erp.testecandidatos.repository;

import br.com.bluesoft.erp.testecandidatos.TesteApplication;
import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import br.com.bluesoft.erp.testecandidatos.model.Product;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Testes de integração para o repositório de produtos.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Product product1;
    private Product product2;
    private Product product3;

    @Before
    public void setUp() {
        // Cria produtos para testes
        product1 = new Product();
        product1.setName("Produto 1");
        product1.setDescription("Descrição do Produto 1");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStock(100);
        product1.setSku("SKU001");
        entityManager.persist(product1);

        product2 = new Product();
        product2.setName("Produto 2");
        product2.setDescription("Descrição do Produto 2");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setStock(5);
        product2.setSku("SKU002");
        entityManager.persist(product2);

        product3 = new Product();
        product3.setName("Produto 3");
        product3.setDescription("Descrição do Produto 3");
        product3.setPrice(new BigDecimal("30.00"));
        product3.setStock(0);
        product3.setSku("SKU003");
        entityManager.persist(product3);

        entityManager.flush();
    }

    @Test
    public void testFindBySku() {
        // Teste para verificar se o método findBySku funciona corretamente
        Optional<Product> foundProduct = productRepository.findBySku("SKU001");
        assertTrue("Produto deveria ser encontrado pelo SKU", foundProduct.isPresent());
        assertEquals("SKU001", foundProduct.get().getSku());
    }

    @Test
    public void testFindBySkuNotFound() {
        Optional<Product> foundProduct = productRepository.findBySku("SKU999");
        assertFalse("Produto não deveria ser encontrado", foundProduct.isPresent());
    }

    @Test
    public void testFindByPriceGreaterThan() {
        List<Product> products = productRepository.findByPriceGreaterThan(new BigDecimal("15.00"));
        assertEquals("Deveria encontrar 2 produtos com preço maior que 15.00", 2, products.size());
        assertTrue("Deveria conter o produto 2", products.stream().anyMatch(p -> p.getSku().equals("SKU002")));
        assertTrue("Deveria conter o produto 3", products.stream().anyMatch(p -> p.getSku().equals("SKU003")));
    }

    @Test
    public void testSearchByName() {
        List<Product> products = productRepository.searchByName("Produto");
        assertEquals("Deveria encontrar 3 produtos com 'Produto' no nome", 3, products.size());
    }

    @Test
    public void testSearchByNameWithSQLInjection() {
        List<Product> products = productRepository.searchByName("' OR '1'='1");
        assertTrue("A consulta não deveria ser vulnerável a injeção de SQL", products.size() == 0);
    }

    @Test
    public void testFindProductsWithLowStock() {
        List<Product> products = productRepository.findProductsWithLowStock(10);

        // e deve conter os produtos 2 (estoque = 5) e 3 (estoque = 0)
        assertFalse("Não deveria conter o produto 1 (estoque alto)",
            products.stream().anyMatch(p -> p.getSku().equals("SKU001")));
        assertTrue("Deveria conter o produto 2 (estoque baixo)", 
            products.stream().anyMatch(p -> p.getSku().equals("SKU002")));
        assertTrue("Deveria conter o produto 3 (estoque baixo)", 
            products.stream().anyMatch(p -> p.getSku().equals("SKU003")));
    }
}
