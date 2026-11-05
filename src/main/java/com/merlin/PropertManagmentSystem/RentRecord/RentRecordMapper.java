package com.merlin.PropertManagmentSystem.RentRecord;

import org.springframework.stereotype.Component;

@Component
public class RentRecordMapper {

    public RentRecordResponseDto toRentRecordResponseDto(RentRecord rentRecord) {
        return new RentRecordResponseDto(rentRecord.getId(), rentRecord.getAmountDue(),  rentRecord.getAmountPaid(),rentRecord.getRentStatus(),rentRecord.getLease().getUnit().getId());
    }
}
