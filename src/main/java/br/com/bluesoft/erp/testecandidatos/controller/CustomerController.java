package br.com.bluesoft.erp.testecandidatos.controller;

import br.com.bluesoft.erp.testecandidatos.model.Customer;
import br.com.bluesoft.erp.testecandidatos.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de clientes.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.findAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerService.findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.findCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomersByName(@RequestParam String name) {
        List<Customer> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.findCustomerById(id)
                .map(existingCustomer -> {
                    customer.setId(id);
                    customerService.updateCustomer(customer);
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-orders")
    public ResponseEntity<List<Customer>> getCustomersWithOrders() {
        List<Customer> customers = customerService.findCustomersWithOrders();
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/validate-email")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        boolean isValid = customerService.validateCustomerEmail(email);
        return ResponseEntity.ok(isValid);
    }
}