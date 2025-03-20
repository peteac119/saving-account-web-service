package org.pete.controller;

import org.pete.entity.Customer;
import org.pete.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

    private final CustomerRepository customerRepository;

    public AuthenticationController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> test() {
        List<Customer> customer = customerRepository.findAll();
        return ResponseEntity.ok(customer);
    }
}
