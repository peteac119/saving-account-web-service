package org.pete.service;

import org.pete.constant.Role;
import org.pete.entity.Users;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.repository.UserRepository;
import org.pete.validator.UserInfoValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final UserInfoValidator userInfoValidator;

    public UserService(UserRepository userRepository,
                       UserInfoValidator userInfoValidator,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.userInfoValidator = userInfoValidator;
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
        Users user = userRepository.findOneByEmailOrCitizenId(email, citizenId);

        if (Objects.nonNull(user)) {
            return new RegisterCustomerResult.CustAlreadyExists();
        }

        user = mapNewCustomer(registerCustomerRequest);

        userRepository.save(user);

        return new RegisterCustomerResult.Success();
    }

    private Users mapNewCustomer(RegisterCustomerRequest registerCustomerRequest) {
        String password = registerCustomerRequest.getPassword();
        String pinNumber = registerCustomerRequest.getPinNum().trim();

        Users users = new Users();
        users.setThaiName(registerCustomerRequest.getThaiName().trim());
        users.setEnglishName(registerCustomerRequest.getEnglishName().trim());
        users.setEmail(registerCustomerRequest.getEmail().trim());
        users.setCitizenId(registerCustomerRequest.getCitizenId().trim());
        users.setRole(List.of(Role.CUSTOMER).toString());

        users.setPassword(bCryptPasswordEncoder.encode(password));
        users.setPinNum(bCryptPasswordEncoder.encode(pinNumber));

        return users;
    }

    private RegisterCustomerResult validateCustomerInfo(RegisterCustomerRequest registerCustomerRequest) {
        String citizenId = registerCustomerRequest.getCitizenId();
        if (!userInfoValidator.validateCitizenId(citizenId)) {
            return new RegisterCustomerResult.ValidationFails("Citizen Id is not in the correct format.");
        }

        String email = registerCustomerRequest.getEmail();
        if (!userInfoValidator.validateEmail(email)) {
            return new RegisterCustomerResult.ValidationFails("Email is not in the correct format.");
        }

        String thaiName = registerCustomerRequest.getThaiName();
        if (!userInfoValidator.validateName(thaiName)) {
            return new RegisterCustomerResult.ValidationFails("Thai name is not in the correct format.");
        }

        String englishName = registerCustomerRequest.getEnglishName();
        if (!userInfoValidator.validateName(englishName)) {
            return new RegisterCustomerResult.ValidationFails("English name is not in the correct format.");
        }

        String pinNumber = registerCustomerRequest.getPinNum();
        if (!userInfoValidator.validatePinNumber(pinNumber)) {
            return new RegisterCustomerResult.ValidationFails("PIN number is not in the correct format.");
        }

        return null;
    }

//    @Transactional(readOnly = true)
//    public CustomerLoginResult customerLogin(CustomerLoginRequest customerLoginRequest) {
//        Users currentUser = userRepository.findOneByEmail(customerLoginRequest.getEmail());
//
//        if (Objects.isNull(currentUser)) {
//            return new CustomerLoginResult.UserNotFound();
//        }
//
//        String inputPass = bCryptPasswordEncoder.encode(customerLoginRequest.getPassword());
//
//        if (!inputPass.equals(currentUser.getPassword())) {
//            return new CustomerLoginResult.WrongPassword();
//        }
//
//        currentUser.setLastLoginDate(LocalDateTime.now());
//
//        return new CustomerLoginResult.LoginSuccess();
//    }
}
