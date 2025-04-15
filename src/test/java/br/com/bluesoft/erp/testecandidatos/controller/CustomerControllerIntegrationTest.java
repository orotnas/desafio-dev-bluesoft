package br.com.bluesoft.erp.testecandidatos.controller;

import br.com.bluesoft.erp.testecandidatos.TesteApplication;
import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import br.com.bluesoft.erp.testecandidatos.model.Customer;
import br.com.bluesoft.erp.testecandidatos.model.Order;
import br.com.bluesoft.erp.testecandidatos.service.CustomerService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o controller de clientes.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class CustomerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerService customerService;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Limpa quaisquer dados existentes
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM Customer").executeUpdate();

        // Cria clientes para testes
        customer1 = new Customer();
        customer1.setName("João Silva");
        customer1.setEmail("joao.silva@example.com");
        customer1.setPhone("(11) 99999-1111");
        customer1.setOrders(new ArrayList<>());
        entityManager.persist(customer1);

        customer2 = new Customer();
        customer2.setName("Maria Santos");
        customer2.setEmail("maria.santos@example.com");
        customer2.setPhone("(11) 99999-2222");
        customer2.setOrders(new ArrayList<>());
        entityManager.persist(customer2);

        // Adiciona pedidos ao customer2
        Order order = new Order();
        order.setOrderNumber("ORD-001");
        order.setCustomer(customer2);
        entityManager.persist(order);

        List<Order> orders = new ArrayList<>();
        orders.add(order);
        customer2.setOrders(orders);
        entityManager.persist(customer2);

        customer3 = new Customer();
        customer3.setName("Pedro Oliveira");
        customer3.setEmail("pedro.oliveira@example.com");
        customer3.setPhone("(11) 99999-3333");
        customer3.setOrders(new ArrayList<>());
        entityManager.persist(customer3);

        // Garante que as alterações sejam persistidas
        entityManager.flush();
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("João Silva")))
                .andExpect(jsonPath("$[1].name", is("Maria Santos")))
                .andExpect(jsonPath("$[2].name", is("Pedro Oliveira")));
    }

    @Test
    public void testGetCustomerById() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/customers/" + customer1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao.silva@example.com")));
    }

    @Test
    public void testGetCustomerByEmail() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/customers/email/joao.silva@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("João Silva")))
                .andExpect(jsonPath("$.email", is("joao.silva@example.com")));
    }

    @Test
    public void testGetCustomerByInvalidEmail() throws Exception {
        mockMvc.perform(get("/api/customers/email/email-invalido"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchCustomersByName() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/customers/search").param("name", "Silva"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("João Silva")));
    }

    @Test
    public void testSearchCustomersByNameWithSQLInjection() throws Exception {
        // Executa e verifica - deveria falhar com 400 Bad Request
        mockMvc.perform(get("/api/customers/search").param("name", "' OR '1'='1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateCustomer() throws Exception {
        // Cria um novo cliente
        Customer newCustomer = new Customer();
        newCustomer.setName("Novo Cliente");
        newCustomer.setEmail("novo.cliente@example.com");
        newCustomer.setPhone("(11) 99999-4444");

        // Executa e verifica
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Novo Cliente")))
                .andExpect(jsonPath("$.email", is("novo.cliente@example.com")));
    }

    @Test
    public void testCreateCustomerWithInvalidEmail() throws Exception {
        // Cria um cliente com e-mail inválido
        Customer invalidCustomer = new Customer();
        invalidCustomer.setName("Cliente Inválido");
        invalidCustomer.setEmail("email-invalido");
        invalidCustomer.setPhone("(11) 99999-5555");

        // Executa e verifica - deveria falhar com 400 Bad Request
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        // Modifica o cliente
        customer1.setName("João Silva Atualizado");
        customer1.setEmail("joao.atualizado@example.com");

        // Executa e verifica
        mockMvc.perform(put("/api/customers/" + customer1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("João Silva Atualizado")))
                .andExpect(jsonPath("$.email", is("joao.atualizado@example.com")));
    }

    @Test
    public void testDeleteCustomer() throws Exception {
        // Executa e verifica
        mockMvc.perform(delete("/api/customers/" + customer1.getId()))
                .andExpect(status().isNoContent());

        // Verifica se o cliente foi removido do banco de dados
        Optional<Customer> deletedCustomer = customerService.findCustomerById(customer1.getId());
        assertFalse("Cliente deveria ser removido", deletedCustomer.isPresent());
    }

    @Test
    public void testDeleteCustomerWithOrders() throws Exception {
        // Executa e verifica - deveria falhar com 400 Bad Request, mas exclui o cliente com pedidos
        mockMvc.perform(delete("/api/customers/" + customer2.getId()))
                .andExpect(status().isNoContent());

        // Verifica se o cliente foi removido do banco de dados
        Optional<Customer> deletedCustomer = customerService.findCustomerById(customer2.getId());
        assertFalse("Cliente com pedidos deveria ser removido", deletedCustomer.isPresent());
    }

    @Test
    public void testGetCustomersWithOrders() throws Exception {
        // Executa e verifica
        mockMvc.perform(get("/api/customers/with-orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Maria Santos")));
    }

    @Test
    public void testValidateEmail() throws Exception {
        // Executa e verifica com e-mail válido
        mockMvc.perform(post("/api/customers/validate-email")
                .param("email", "joao.silva@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Executa e verifica com e-mail inválido
        mockMvc.perform(post("/api/customers/validate-email")
                .param("email", "joao.silva@dominio-sem-tld"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("true"));

        // Executa e verifica com e-mail claramente inválido
        mockMvc.perform(post("/api/customers/validate-email")
                .param("email", "email-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("false"));
    }
}