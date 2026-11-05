package com.merlin.PropertManagmentSystem.RentRecord;


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
@RequestMapping("/api/rentRecords")
@RestController
public class RentRecordController {

    private final RentRecordService rentRecordService;

    @GetMapping("/getAllRentRecords")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<List<RentRecordResponseDto>> getAllRentRecords(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(rentRecordService.getAllRentRecords(authenticatedUser));
    }

    @GetMapping("/getRentRecordById/{rentRecordId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'CHAIRMAN', 'SECRETARY', 'TREASURER', 'CARETAKER')")
    public ResponseEntity<RentRecordResponseDto> getRentRecordById(
            @PathVariable("rentRecordId") Long rentRecordId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(rentRecordService.getRentRecordById(authenticatedUser, rentRecordId));
    }
}
