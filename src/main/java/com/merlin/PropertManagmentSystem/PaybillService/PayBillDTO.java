package com.merlin.PropertManagmentSystem.PaybillService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PayBillDTO(
        @NotBlank(message = "Paybill number is required")
        @Pattern(regexp = "^\\d{5,7}$",
                message = "Paybill must be 5 to 7 digits")
        String payBill
) {
}
