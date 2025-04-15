package br.com.bluesoft.erp.testecandidatos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import br.com.bluesoft.erp.testecandidatos.model.Product;
import br.com.bluesoft.erp.testecandidatos.service.ProductService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes para o controller de produtos.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Product product1;
    private Product product2;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

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
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Configura o mock
        when(productService.findAllProducts()).thenReturn(Arrays.asList(product1, product2));

        // Executa e verifica
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));

        // Verifica se o método do serviço foi chamado
        verify(productService, times(1)).findAllProducts();
    }

    @Test
    public void testGetProductsWithLowStock() throws Exception {
        // Configura o mock
        when(productService.findProductsWithLowStock()).thenReturn(Arrays.asList(product2));

        // Executa e verifica
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("Produto 2")));

        // Verifica se o método do serviço foi chamado
        verify(productService, times(1)).findProductsWithLowStock();
    }

    @Test
    public void testGetInventoryValue() throws Exception {
        // Configura o mock
        when(productService.calculateInventoryValue()).thenReturn(new BigDecimal("1100.00"));

        // Executa e verifica
        mockMvc.perform(get("/api/products/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", is(1100.00)));

        // Verifica se o método do serviço foi chamado
        verify(productService, times(1)).calculateInventoryValue();
    }

    @Test
    public void testGetProductsByPriceRange() throws Exception {
        // Configura o mock
        when(productService.findProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(Arrays.asList(product1, product2));

        // Executa e verifica
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "5.00")
                .param("maxPrice", "25.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));

        // Verifica se o método do serviço foi chamado com os parâmetros corretos
        verify(productService, times(1)).findProductsByPriceRange(
            new BigDecimal("5.00"), new BigDecimal("25.00"));
    }

    @Test
    public void testGetProductsByPriceRangeWithInvalidRange() throws Exception {
        when(productService.findProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(Arrays.asList(product1, product2));

        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "30.00")
                .param("maxPrice", "20.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)));

        // Verifica se o método do serviço foi chamado mesmo com parâmetros inválidos
        verify(productService, times(1)).findProductsByPriceRange(
            new BigDecimal("30.00"), new BigDecimal("20.00"));
    }
}
