package com.merlin.PropertManagmentSystem.Notification;

import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/getSentNotifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDto>> getSentNotifications(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.sentNotification(authenticatedUser));
    }

    @GetMapping("/getMyNotifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.myNotifications(authenticatedUser));
    }

    @PatchMapping("/markAsRead/{notificationId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("notificationId") Long notificationId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        notificationService.markAsRead(notificationId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}