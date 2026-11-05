package com.merlin.PropertManagmentSystem.Unit;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Lease.LeaseRepository;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository  leaseRepository;
    private final PropertyAsignRepository propertyAsignRepository;

    public UnitResponseDto createUnit(Long propertyId,UnitDto dto, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN) ||
                authenticatedUser.getRole().equals(Role.SECRETARY);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()->new ResourceNotFound("Property Not Found"));

        if (unitRepository.existsByPropertyAndUnitNumber(property, dto.unitNumber()))
            throw new DuplicateRuleException("Unit already exists in this property");
        if (unitRepository.existsByAccountNumber(dto.accountNumber()))
            throw new DuplicateRuleException("Account number already in use");

        Property rightProperty;


        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!propertyRepository.existsByIdAndLandlordId(property.getId(),authenticatedUser.getId())){
                throw new ResourceNotFound("Property Not Found");
            }
            rightProperty = property;

        } else if(isOfficer && !authenticatedUser.isActive()){
            if(!propertyRepository.existsByIdAndOrganisationId(property.getId(), authenticatedUser.getOrganisation().getId())){
                throw new ResourceNotFound("Property Not Found");
            }
            rightProperty = property;
        }else {
            throw new BusinessRuleException("You are not allowed to Create Unit");
        }

        Unit unit = unitMapper.toUnit(dto);

        unit.setProperty(rightProperty);
        unit.setCreatedBy(authenticatedUser);

        var savedUnit  = unitRepository.save(unit);

        return unitMapper.toUnitResponseDto(savedUnit);
    }

    public UnitResponseDto updateUnit(Long unitId,Long propertyId, UpdateUnit dto, User authenticatedUser) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));


        Property property = unit.getProperty();


        if(property.getLandlord() != null){
            if(!property.getLandlord().getId().equals(authenticatedUser.getId())){
                 throw new ResourceNotFound("Sorry you cant view this units coz you don't own the property, contact the developer");
            }

        }else if(property.getOrganisation() != null){
            if(!property.getOrganisation().getId().equals(authenticatedUser.getId())){
                throw new ResourceNotFound("Sorry you cant view this units coz you don't own the property, contact the developer");
            }

        }else {
            throw new BusinessRuleException("You are not allowed to update this unit");
        }


        if (dto.unitNumber() != null) {
            String unitNo = dto.unitNumber().trim().toUpperCase();

            if (!unitNo.equals(unit.getUnitNumber())
                    && unitRepository.existsByPropertyIdAndUnitNumberAndIdNot(property.getId(), unitNo, unit.getId())) {
                throw new DuplicateRuleException("Unit " + unitNo + " already exists in this property");
            }
            unit.setUnitNumber(unitNo);
        }
        if (dto.accountNumber() != null) {
            String account = dto.accountNumber().trim().toUpperCase();

            if (!account.equals(unit.getAccountNumber())
                    && unitRepository.existsByAccountNumberAndIdNot(account, unit.getId())) {
                throw new DuplicateRuleException("Account number " + account + " is already used by another unit");
            }
            unit.setAccountNumber(account);
        }
        if (dto.deposit() != null){
            unit.setDepositAmount(dto.deposit());
        }

        if(dto.monthlyRent() != null){
            unit.setMonthlyRent(dto.monthlyRent());
        }

        var savedUnit = unitRepository.save(unit);



        return unitMapper.toUnitResponseDto(savedUnit);
    }

    public List<UnitResponseDto> getAllUnits(Long propertyId,User authenticatedUser) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFound("Property not found"));

        List<Unit> units = new ArrayList<>();

        boolean allowed;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            allowed = property.getLandlord() != null && property.getLandlord().getId().equals(authenticatedUser.getId());
            if(!allowed){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            units = unitRepository.findByProperty(property);

        }else if(authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER) ||authenticatedUser.getRole().equals(Role.CHAIRMAN )){

            allowed = property.getOrganisation() != null && property.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());


            if(!allowed){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            units = unitRepository.findByProperty(property);
        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {
            List<Property> propertyAssigns = propertyAsignRepository.findByCareTakerAndUnAssignedAtIsNull(authenticatedUser);

            boolean isFound = propertyAssigns.contains(property);

            if(!isFound){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            units = unitRepository.findByProperty(property);

        }else {
            throw new BusinessRuleException("You are not allowed to view this Units");
        }

        return  units
                .stream()
                .map(unitMapper :: toUnitResponseDto)
                .toList();

    }

    public UnitResponseDto getUnit(Long unitId,  User authenticatedUser) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));

        Property property = unit.getProperty();

        Unit rightUnit;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            boolean isOwner =property.getLandlord()!=null && property.getLandlord().getId().equals(authenticatedUser.getId());

            if(!isOwner){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            rightUnit = unit;
        } else if (authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER) ||authenticatedUser.getRole().equals(Role.CHAIRMAN)) {

            boolean isRightOrganisation = property.getOrganisation() != null && property.getOrganisation().getId()
                    .equals(authenticatedUser.getOrganisation().getId());

            if(!isRightOrganisation){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            rightUnit = unit;
        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){
            boolean isAssigned = propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser,property);

            if(!isAssigned){
                throw new BusinessRuleException("You are not allowed to view this Units");
            }

            rightUnit = unit;
        }else {
            throw new BusinessRuleException("You are not allowed to view this Units");
        }

        return  unitMapper.toUnitResponseDto(rightUnit);
    }

    public void activateUnit(Long unitId, User authenticatedUser) {
        Unit unit  = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));

        Property property = unit.getProperty();

        Unit rightUnit;

    if(authenticatedUser.getRole().equals(Role.LANDLORD)){
        boolean  isOwner =property.getLandlord() !=null && property.getLandlord().getId()
                .equals(authenticatedUser.getId());

        if(!isOwner){
            throw new BusinessRuleException("You are not allowed to update this Units");
        }

        rightUnit = unit;
    } else if (authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER) ||authenticatedUser.getRole().equals(Role.CHAIRMAN)) {
        boolean  isRightOrganisation = property.getOrganisation() !=null && property.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

        if(!isRightOrganisation){
            throw new BusinessRuleException("You are not allowed to update this Units");
        }
        rightUnit = unit;
    }else {
        throw new BusinessRuleException("You are not allowed to update this Units");
    }

    if(unit.isActive()){
        throw new BusinessRuleException("This unit is already active");
    }

    rightUnit.setActive(true);
    unitRepository.save(rightUnit);
    }

    public void deactivateUnit(Long unitId, User authenticatedUser) {
        Unit unit  = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));

        Property property = unit.getProperty();

        Unit rightUnit;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            boolean  isOwner =property.getLandlord() !=null
                    && property.getLandlord().getId().equals(authenticatedUser.getId());

            if(!isOwner){
                throw new BusinessRuleException("You are not allowed to update this Units");
            }

            rightUnit = unit;
        } else if (authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER) ||authenticatedUser.getRole().equals(Role.CHAIRMAN)) {
            boolean  isRightOrganisation = property.getOrganisation() != null
                    &&  property.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

            if(!isRightOrganisation){
                throw new BusinessRuleException("You are not allowed to update this Units");
            }
            rightUnit = unit;
        }else {
            throw new BusinessRuleException("You are not allowed to update this Units");
        }

        if(!unit.isActive()){
            throw new BusinessRuleException("This unit is already active");
        }

        rightUnit.setActive(false);
        unitRepository.save(rightUnit);
    }

    public void deleteUnit(Long unitId, User authenticatedUser) {

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));

        Property property = unit.getProperty();


        if(property.getLandlord() != null ){
            if(!property.getLandlord().getId().equals(authenticatedUser.getId()) && !authenticatedUser.getRole().equals(Role.LANDLORD)){
                throw new BusinessRuleException("You are not allowed to delete this Unit");
            }
        } else if (property.getOrganisation() != null) {
            if(!property.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId()) && !authenticatedUser.getRole().equals(Role.CHAIRMAN)) {
                throw new BusinessRuleException("You are not allowed to delete this Unit");
            }

        }else {
            throw new BusinessRuleException("You are not allowed to delete a Unit");
        }


        if (leaseRepository.existsByUnit(unit)) {
            throw new BusinessRuleException("Cannot delete a unit that has lease history");
        }

        unitRepository.delete(unit);
    }

}
