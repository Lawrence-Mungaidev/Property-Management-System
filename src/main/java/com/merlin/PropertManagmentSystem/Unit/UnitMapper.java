package com.merlin.PropertManagmentSystem.Unit;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UnitMapper {

    public Unit toUnit(UnitDto dto){
        Unit unit = new Unit();

        unit.setUnitNumber(dto.unitNumber());
        unit.setAccountNumber(dto.accountNumber());
        unit.setMonthlyRent(dto.monthlyRent());
        unit.setDepositAmount(dto.deposit());
        unit.setCreatedAt(LocalDateTime.now());
        unit.setUnitType(dto.unitType());
        unit.setVacant(true);
        unit.setActive(true);

        return unit;
    }

    public UnitResponseDto toUnitResponseDto(Unit unit){
        return new UnitResponseDto(unit.getId(), unit.getUnitNumber(), unit.getAccountNumber(), unit.getMonthlyRent(), unit.getDepositAmount(),unit.getCreatedAt(),unit.isActive(),unit.isVacant());
    }
}
