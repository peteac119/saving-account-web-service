package org.pete.model.result;

public class UserLoginResult {
    public static class LoginSuccess extends UserLoginResult {}
    public static class UserNotFound extends UserLoginResult {}
    public static class WrongPassword extends UserLoginResult {}
}
