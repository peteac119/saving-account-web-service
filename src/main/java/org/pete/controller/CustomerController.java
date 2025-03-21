package org.pete.controller;

import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.response.RegisterCustomerResponse;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/cust")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest registerCustomerRequest) {
        RegisterCustomerResult result = customerService.registerCustomer(registerCustomerRequest);
        return switch (result) {
            case RegisterCustomerResult.ValidationFails vf -> ResponseEntity.badRequest().body(createValidationFailsResponse(vf));
            case RegisterCustomerResult.CustAlreadyExists ce-> ResponseEntity.ok(createCustAlreadyExistsResponse());
            default -> ResponseEntity.status(HttpStatus.CREATED).body(createCustSuccessResponse());
        };
    }

    private RegisterCustomerResponse createValidationFailsResponse(RegisterCustomerResult.ValidationFails vf) {
        return new RegisterCustomerResponse(false, vf.getMessage());
    }

    private RegisterCustomerResponse createCustAlreadyExistsResponse() {
        return new RegisterCustomerResponse(false, null);
    }

    private RegisterCustomerResponse createCustSuccessResponse() {
        return new RegisterCustomerResponse(true, null);
    }
}
