package com.merlin.PropertManagmentSystem.TransactionLog;

import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/transactionLogs")
@RestController
public class TransactionLogController {

    private final TransactionLogService transactionLogService;

    @GetMapping("/getAllTransactionLogs")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<List<TransactionLogResponseDto>> getAllTransactionLogs(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionLogService.getAllTransactionLog(authenticatedUser));
    }

    @GetMapping("/searchTransactionLog/{transactionCode}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<TransactionLogResponseDto> searchTransactionLog(
            @PathVariable("transactionCode") String transactionCode,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionLogService.searchTransactionLog(transactionCode, authenticatedUser));
    }

    @GetMapping("/getTransactionLogById/{transactionLogId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER')")
    public ResponseEntity<TransactionLogResponseDto> getTransactionLogById(
            @PathVariable("transactionLogId") Long transactionLogId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(transactionLogService.getTransactionLogById(transactionLogId, authenticatedUser));
    }
}