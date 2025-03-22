package org.pete.model.result;

public class CustomerLoginResult {
    public static class LoginSuccess extends CustomerLoginResult {}
    public static class UserNotFound extends CustomerLoginResult {}
    public static class WrongPassword extends CustomerLoginResult {}
}
