package com.merlin.PropertManagmentSystem.NotificationBatch;

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
@RequestMapping("/api/notificationBatches")
@RestController
public class NotificationBatchController {

    private final NotificationBatchService notificationBatchService;

    @PostMapping("/createNotificationBatch")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<NotificationBatchResponseDto> createNotificationBatch(
            @Valid @RequestBody NotificationBatchDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationBatchService.createNotificationBatch(dto, authenticatedUser));
    }

    @PostMapping("/adminSendingBatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationBatchResponseDto> adminSendingBatch(
            @Valid @RequestBody NotificationBatchDto dto,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationBatchService.adminSendingBatch(dto, authenticatedUser));
    }

    @GetMapping("/getAllNotificationBatches")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<List<NotificationBatchResponseDto>> getAllNotificationBatches(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationBatchService.getAllNotifications(authenticatedUser));
    }

    @GetMapping("/getNotificationBatchById/{notificationBatchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<NotificationBatchResponseDto> getNotificationBatchById(
            @PathVariable("notificationBatchId") Long notificationBatchId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationBatchService.getNotificationById(notificationBatchId, authenticatedUser));
    }
}