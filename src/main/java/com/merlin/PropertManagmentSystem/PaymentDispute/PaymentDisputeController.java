package com.merlin.PropertManagmentSystem.PaymentDispute;

import com.merlin.PropertManagmentSystem.User.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/paymentDisputes")
@RestController
public class PaymentDisputeController {

    private final PaymentDisputeService paymentDisputeService;

    @PostMapping("/reversePayment")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<PDResponseDTO> reversePayment(
            @Valid @RequestBody ReversePaymentDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentDisputeService.reversePayment(authenticatedUser, dto));
    }

    @GetMapping("/getAllPaymentDisputes")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<List<PDResponseDTO>> getAllPaymentDisputes(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentDisputeService.getAllPaymentDispute(authenticatedUser));
    }

    @GetMapping("/getPaymentDisputeById/{paymentDisputeId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<PDResponseDTO> getPaymentDisputeById(
            @PathVariable("paymentDisputeId") Long paymentDisputeId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(paymentDisputeService.getPaymentDispute(paymentDisputeId, authenticatedUser));
    }
}