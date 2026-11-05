package com.merlin.PropertManagmentSystem.Subscription;

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
@RequestMapping("/api/subscriptions")
@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<SubscriptionResponseDto> subscribe(
            @Valid @RequestBody SubscriptionDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(dto, authenticatedUser));
    }

    @GetMapping("/getAllSubscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionService.getAllSubscription(authenticatedUser));
    }

    @GetMapping("/getMySubscriptions")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<List<SubscriptionResponseDto>> getMySubscriptions(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionService.getMySubscription(authenticatedUser));
    }

    @GetMapping("/getSubscriptionById/{subscriptionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(
            @PathVariable("subscriptionId") Long subscriptionId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionService.getSubscriptionById(subscriptionId, authenticatedUser));
    }
}