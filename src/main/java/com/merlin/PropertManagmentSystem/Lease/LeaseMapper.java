package com.merlin.PropertManagmentSystem.Lease;

import org.springframework.stereotype.Component;

@Component
public class LeaseMapper {

    public LeaseResponseDto toLeaseResponseDto(Lease lease){
        return new LeaseResponseDto(lease.getId(), lease.getTenant().getId(),lease.getUnit().getId(),lease.getStartDate(),lease.getEndDate(),lease.getRentAtSigning());
    }


}
