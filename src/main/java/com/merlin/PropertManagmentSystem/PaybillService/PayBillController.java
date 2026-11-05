package com.merlin.PropertManagmentSystem.PaybillService;


import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/payBills")
@RestController
public class PayBillController {

    private final PaybillService paybillService;

    @PostMapping("/createPayBill")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN')")
    public ResponseEntity<String> createPayBill(
            @Valid @RequestBody PayBillDTO dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paybillService.createPayBill(dto, authenticatedUser));
    }
}