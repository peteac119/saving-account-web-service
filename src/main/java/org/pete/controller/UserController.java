package org.pete.controller;

import org.pete.model.request.CustomerLoginRequest;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.response.RegisterCustomerResponse;
import org.pete.model.result.CustomerLoginResult;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/customer/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest registerCustomerRequest) {
        RegisterCustomerResult result = userService.registerCustomer(registerCustomerRequest);
        return switch (result) {
            case RegisterCustomerResult.ValidationFails vf ->
                    ResponseEntity
                            .badRequest().
                            body(
                                    new RegisterCustomerResponse(false, vf.getMessage())
                            );
            case RegisterCustomerResult.CustAlreadyExists ce->
                    ResponseEntity
                            .ok(
                                    new RegisterCustomerResponse(false, null)
                            );
            default -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new RegisterCustomerResponse(true, null)
                    );
        };
    }

    @PostMapping(path = "/customer/login")
    public ResponseEntity<?> customerLogin(@RequestBody CustomerLoginRequest customerLoginRequest) {
        CustomerLoginResult result = userService.customerLogin(customerLoginRequest);

        return switch (result) {
            case CustomerLoginResult.WrongPassword wrongPassword -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case CustomerLoginResult.UserNotFound userNotFound -> ResponseEntity.notFound().build();
            default -> ResponseEntity.ok().build();
        };
    }
}
