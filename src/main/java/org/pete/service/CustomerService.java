package org.pete.service;

import org.pete.entity.Customer;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.repository.CustomerRepository;
import org.pete.validator.CustomerInfoValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CustomerService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CustomerRepository customerRepository;
    private final CustomerInfoValidator customerInfoValidator;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerInfoValidator customerInfoValidator,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.customerRepository = customerRepository;
        this.customerInfoValidator = customerInfoValidator;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public RegisterCustomerResult registerCustomer(RegisterCustomerRequest registerCustomerRequest) {
        RegisterCustomerResult validationResult = validateCustomerInfo(registerCustomerRequest);

        if (Objects.nonNull(validationResult)) {
            return validationResult;
        }

        String email = registerCustomerRequest.getEmail().trim();
        String citizenId = registerCustomerRequest.getCitizenId().trim();
        Customer customer = customerRepository.findOneByEmailOrCitizenId(email, citizenId);

        if (Objects.nonNull(customer)) {
            return new RegisterCustomerResult.CustAlreadyExists();
        }

        customer = mapNewCustomer(registerCustomerRequest);

        customerRepository.save(customer);

        return new RegisterCustomerResult.Success();
    }

    private Customer mapNewCustomer(RegisterCustomerRequest registerCustomerRequest) {
        String password = registerCustomerRequest.getPassword();
        String pinNumber = registerCustomerRequest.getPinNum().trim();

        Customer customer = new Customer();
        customer.setThaiName(registerCustomerRequest.getThaiName().trim());
        customer.setEnglishName(registerCustomerRequest.getEnglishName().trim());
        customer.setEmail(registerCustomerRequest.getEmail().trim());
        customer.setCitizenId(registerCustomerRequest.getCitizenId().trim());
        customer.setCreationDate(LocalDateTime.now());
        customer.setLastUpdateDate(LocalDateTime.now());

        customer.setPassword(bCryptPasswordEncoder.encode(password));
        customer.setPinNum(bCryptPasswordEncoder.encode(pinNumber));

        return customer;
    }

    private RegisterCustomerResult validateCustomerInfo(RegisterCustomerRequest registerCustomerRequest) {
        String citizenId = registerCustomerRequest.getCitizenId();
        if (!customerInfoValidator.validateCitizenId(citizenId)) {
            return new RegisterCustomerResult.ValidationFails("Citizen Id is not in the correct format.");
        }

        String email = registerCustomerRequest.getEmail();
        if (!customerInfoValidator.validateEmail(email)) {
            return new RegisterCustomerResult.ValidationFails("Email is not in the correct format.");
        }

        String thaiName = registerCustomerRequest.getThaiName();
        if (!customerInfoValidator.validateName(thaiName)) {
            return new RegisterCustomerResult.ValidationFails("Thai name is not in the correct format.");
        }

        String englishName = registerCustomerRequest.getEnglishName();
        if (!customerInfoValidator.validateName(englishName)) {
            return new RegisterCustomerResult.ValidationFails("English name is not in the correct format.");
        }

        String pinNumber = registerCustomerRequest.getPinNum();
        if (!customerInfoValidator.validatePinNumber(pinNumber)) {
            return new RegisterCustomerResult.ValidationFails("PIN number is not in the correct format.");
        }

        return null;
    }

}
