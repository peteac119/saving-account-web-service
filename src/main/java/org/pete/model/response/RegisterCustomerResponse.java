package org.pete.model.response;

public record RegisterCustomerResponse(
        boolean isCreated,
        String errorMessage
) {}
