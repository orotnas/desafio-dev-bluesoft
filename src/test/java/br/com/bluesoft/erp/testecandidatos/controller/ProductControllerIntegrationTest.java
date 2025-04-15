package br.com.bluesoft.erp.testecandidatos.controller;

import br.com.bluesoft.erp.testecandidatos.TesteApplication;
import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import br.com.bluesoft.erp.testecandidatos.model.Product;
import br.com.bluesoft.erp.testecandidatos.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o controller de produtos.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private Product product1;
    private Product product2;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Limpa quaisquer dados existentes
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM Product").executeUpdate();

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

        // Garante que as alterações sejam persistidas
        entityManager.flush();
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));
    }

    @Test
    public void testGetProductById() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/" + product1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1")))
                .andExpect(jsonPath("$.sku", is("SKU001")));
    }

    @Test
    public void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductBySku() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/sku/SKU001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1")))
                .andExpect(jsonPath("$.sku", is("SKU001")));
    }

    @Test
    public void testGetProductByInvalidSku() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/sku/a"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        // Cria um novo produto
        Product newProduct = new Product();
        newProduct.setName("Novo Produto");
        newProduct.setDescription("Descrição do Novo Produto");
        newProduct.setPrice(new BigDecimal("30.00"));
        newProduct.setStock(50);
        newProduct.setSku("SKU003");

        // Executa e verifica
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Novo Produto")))
                .andExpect(jsonPath("$.sku", is("SKU003")));
    }

    @Test
    public void testCreateProductWithoutRequiredFields() throws Exception {
        // Cria um produto sem nome (campo obrigatório)
        Product invalidProduct = new Product();
        invalidProduct.setDescription("Produto sem nome");
        invalidProduct.setPrice(new BigDecimal("40.00"));
        invalidProduct.setStock(20);
        invalidProduct.setSku("SKU004");

        // Executa e verifica - deveria falhar com 400 Bad Request, mas aceita o produto sem nome
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        // Modifica o produto
        product1.setName("Produto 1 Atualizado");
        product1.setPrice(new BigDecimal("15.00"));

        // Executa e verifica
        mockMvc.perform(put("/api/products/" + product1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1 Atualizado")))
                .andExpect(jsonPath("$.price", is(15.00)));
    }

    @Test
    public void testDeleteProduct() throws Exception {
        // Executa e verifica
        mockMvc.perform(delete("/api/products/" + product1.getId()))
                .andExpect(status().isNoContent());

        // Verifica se o produto foi removido do banco de dados
        Optional<Product> deletedProduct = productService.findProductById(product1.getId());
        assertFalse("Produto deveria ser removido", deletedProduct.isPresent());
    }

    @Test
    public void testDeleteNonExistentProduct() throws Exception {
        // Executa e verifica
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateStock() throws Exception {
        // Executa e verifica
        mockMvc.perform(put("/api/products/" + product1.getId() + "/stock")
                .param("stock", "200"))
                .andExpect(status().isOk());

        // Verifica se o estoque foi atualizado
        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Estoque deveria ser atualizado", Integer.valueOf(200), updatedProduct.get().getStock());
    }

    @Test
    public void testUpdateStockWithNegativeValue() throws Exception {
        mockMvc.perform(put("/api/products/" + product1.getId() + "/stock")
                .param("stock", "-50"))
                .andExpect(status().isBadRequest());

        // Verifica se o estoque foi atualizado com valor negativo
        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertNotEquals("Estoque não deveria ser atualizado com valor negativo", Integer.valueOf(-50), updatedProduct.get().getStock());
    }

    @Test
    public void testUpdatePrice() throws Exception {
        // Executa e verifica
        mockMvc.perform(put("/api/products/" + product1.getId() + "/price")
                .param("price", "25.00"))
                .andExpect(status().isOk());

        // Verifica se o preço foi atualizado
        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Preço deveria ser atualizado", new BigDecimal("25.00"), updatedProduct.get().getPrice());
    }

    @Test
    public void testUpdatePriceWithNegativeValue() throws Exception {
        mockMvc.perform(put("/api/products/" + product1.getId() + "/price")
                .param("price", "-10.00"))
                .andExpect(status().isBadRequest());

        // Verifica se o preço foi atualizado com valor negativo
        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertNotEquals("Preço não deveria ser atualizado com valor negativo", new BigDecimal("-10.00"), updatedProduct.get().getPrice());
    }

    @Test
    public void testGetInventoryValue() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void testGetProductsByPriceRange() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "5.00")
                .param("maxPrice", "25.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetProductsByPriceRangeWithDefaultValues() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/products/price-range"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void testGetProductsByPriceRangeWithInvalidRange() throws Exception {
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "30.00")
                .param("maxPrice", "20.00"))
                .andExpect(status().isBadRequest());
    }
}
