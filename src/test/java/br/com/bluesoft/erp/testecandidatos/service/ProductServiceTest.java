package br.com.bluesoft.erp.testecandidatos.service;

import br.com.bluesoft.erp.testecandidatos.model.Product;
import br.com.bluesoft.erp.testecandidatos.repository.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes para o serviço de produtos.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product product2;
    private Product product3;

    @Before
    public void setUp() {
        // Cria produtos para testes
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Produto 1");
        product1.setDescription("Descrição do Produto 1");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStock(100);
        product1.setSku("SKU001");

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Produto 2");
        product2.setDescription("Descrição do Produto 2");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setStock(5);
        product2.setSku("SKU002");

        product3 = new Product();
        product3.setId(3L);
        product3.setName("Produto 3");
        product3.setDescription("Descrição do Produto 3");
        product3.setPrice(new BigDecimal("30.00"));
        product3.setStock(0);
        product3.setSku("SKU003");
    }

    @Test
    public void testSaveProduct() {
        // Configura o mock
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Executa o método
        Product savedProduct = productService.saveProduct(product1);

        // Verifica o resultado
        assertNotNull("Produto salvo não deveria ser nulo", savedProduct);
        assertEquals("Produto salvo deveria ter o mesmo ID", product1.getId(), savedProduct.getId());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    //todo rever
    public void testSaveProductWithNegativePrice() {
        // Cria um produto com preço negativo
        Product invalidProduct = new Product();
        invalidProduct.setName("Produto Inválido");
        invalidProduct.setPrice(new BigDecimal("-10.00"));

        // Configura o mock
        when(productRepository.save(any(Product.class))).thenReturn(invalidProduct);

        // Executa o método
        Product savedProduct = productService.saveProduct(invalidProduct);

        assertNotNull("Produto com preço negativo foi salvo", savedProduct);

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).save(invalidProduct);
    }

    @Test
    public void testFindProductById() {
        // Configura o mock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        // Executa o método
        Optional<Product> foundProduct = productService.findProductById(1L);

        // Verifica o resultado
        assertTrue("Produto deveria ser encontrado", foundProduct.isPresent());
        assertEquals("Produto encontrado deveria ter o ID correto", 1L, foundProduct.get().getId().longValue());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindProductBySku() {
        // Configura o mock
        when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product1));

        // Executa o método
        Optional<Product> foundProduct = productService.findProductBySku("SKU001");

        // Verifica o resultado
        assertTrue("Produto deveria ser encontrado", foundProduct.isPresent());
        assertEquals("Produto encontrado deveria ter o SKU correto", "SKU001", foundProduct.get().getSku());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findBySku("SKU001");
    }

    @Test
    public void testFindAllProducts() {
        // Configura o mock
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

        // Executa o método
        List<Product> products = productService.findAllProducts();

        // Verifica o resultado
        assertEquals("Deveria encontrar 3 produtos", 3, products.size());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testFindProductsByPriceRange() {
        // Configura o mock
        when(productRepository.findByPriceGreaterThan(new BigDecimal("15.00")))
            .thenReturn(Arrays.asList(product2, product3));

        // Executa o método
        List<Product> products = productService.findProductsByPriceRange(
            new BigDecimal("15.00"), new BigDecimal("25.00"));

        // Verifica o resultado
        assertEquals("Deveria encontrar 2 produtos", 2, products.size());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findByPriceGreaterThan(new BigDecimal("15.00"));
    }

    @Test
    public void testUpdateProductStock() {
        // Configura o mock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Executa o método
        productService.updateProductStock(1L, 50);

        // Verifica se os métodos do repositório foram chamados
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProductPrice() {
        // Configura o mock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Executa o método
        productService.updateProductPrice(1L, new BigDecimal("15.00"));

        // Verifica se os métodos do repositório foram chamados
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct() {
        // Configura o mock
        doNothing().when(productRepository).deleteById(anyLong());

        // Executa o método
        productService.deleteProduct(1L);

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCalculateInventoryValue() {
        // Configura o mock
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

        // Executa o método
        BigDecimal totalValue = productService.calculateInventoryValue();

        // Verifica o resultado
        // product1: 10.00 * 100 = 1000.00
        // product2: 20.00 * 5 = 100.00
        // product3: 30.00 * 0 = 0.00
        // Total esperado: 1100.00
        assertEquals("Valor total do inventário deveria ser 1100.00", 
            new BigDecimal("1100.00"), totalValue);

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testFindProductsWithLowStock() {
        // Configura o mock
        when(productRepository.findProductsWithLowStock(10))
            .thenReturn(Arrays.asList(product2, product3));

        // Executa o método
        List<Product> products = productService.findProductsWithLowStock();

        // Verifica o resultado
        assertEquals("Deveria encontrar 2 produtos com estoque baixo", 2, products.size());

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findProductsWithLowStock(10);
    }

    @Test
    public void testCalculateInventoryValueWithNullStock() {
        // Cria um produto com estoque nulo
        Product productWithNullStock = new Product();
        productWithNullStock.setId(4L);
        productWithNullStock.setName("Produto 4");
        productWithNullStock.setPrice(new BigDecimal("40.00"));
        productWithNullStock.setStock(null);

        // Configura o mock
        when(productRepository.findAll()).thenReturn(
            Arrays.asList(product1, product2, product3, productWithNullStock));

        // Executa o método
        BigDecimal totalValue = productService.calculateInventoryValue();

        // Verifica o resultado
        assertEquals("Valor total do inventário deveria ser 1100.00",
            new BigDecimal("1100.00"), totalValue);

        // Verifica se o método do repositório foi chamado
        verify(productRepository, times(1)).findAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveProductWithZeroPrice() {
        // Cria um produto com preço zero
        Product invalidProduct = new Product();
        invalidProduct.setName("Produto Inválido");
        invalidProduct.setPrice(BigDecimal.ZERO);

        // Configura o mock
        when(productRepository.save(any(Product.class))).thenReturn(invalidProduct);

        // Executa o método
        Product savedProduct = productService.saveProduct(invalidProduct);

        assertNull("Produto com preço zero foi salvo", savedProduct);
        verify(productRepository, never()).save(invalidProduct);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateProductStockWithNegativeValue() {
        // Configura o mock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Executa o método
        productService.updateProductStock(1L, -10);

        // Verifica se os métodos do repositório foram chamados
        verify(productRepository, times(0)).findById(1L);
        verify(productRepository, times(0)).save(any(Product.class));
    }
}
