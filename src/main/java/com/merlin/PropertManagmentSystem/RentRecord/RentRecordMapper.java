package com.merlin.PropertManagmentSystem.RentRecord;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RentRecordMapper {

    public RentRecordResponseDto toRentRecordResponseDto(RentRecord rentRecord) {
        BigDecimal amountDue =rentRecord.getMonthlyRent().subtract(rentRecord.getAmountPaid());

        return new RentRecordResponseDto(rentRecord.getId(), rentRecord.getMonthlyRent(),  rentRecord.getAmountPaid(),amountDue,rentRecord.getRentStatus(),rentRecord.getLease().getUnit().getId());
    }
}
