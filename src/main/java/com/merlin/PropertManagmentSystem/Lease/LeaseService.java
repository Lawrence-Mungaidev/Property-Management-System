package com.merlin.PropertManagmentSystem.Lease;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Notification.NotificationService;
import com.merlin.PropertManagmentSystem.Notification.NotificationType;
import com.merlin.PropertManagmentSystem.Notification.channel;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecordService;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Tenant.TenantRepository;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final LeaseMapper leaseMapper;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyAsignRepository propertyAsignRepository;
    private final NotificationService  notificationService;
    private final RentRecordService rentRecordService;

    private static final int MAX_DAYS_BACK = 7;
    private static final int MAX_DAYS_AHEAD = 14;


    @Transactional
    public LeaseResponseDto createLease(LeaseDto dto, User authenticatedUser) {

        Unit unit = unitRepository.findById(dto.unit())
                .orElseThrow(()-> new ResourceNotFound("Unit not found"));

        Tenant tenant = tenantRepository.findById(dto.tenant())
                .orElseThrow(()-> new ResourceNotFound("Tenant not found"));

        Property property = unit.getProperty();

        Lease lease = new Lease();

        if(!ownsIt(lease,property,authenticatedUser)){
            throw new BusinessRuleException("Sorry you cant create a Lease");
        }

        LocalDate today = LocalDate.now();
        LocalDate start = dto.startDate();

        if (start.isBefore(today.minusDays(MAX_DAYS_BACK))
                || start.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessRuleException("Start date must be within 7 days before or 14 days after today");
        }

        lease.setPreferredBillingDay(unit.getProperty().getPreferredBillingDay());
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setStartDate(LocalDate.now());
        lease.setDepositAmount(unit.getDepositAmount());
        lease.setLeaseStatus(LeaseStatus.ACTIVE);

        if(unit.getMonthlyRent() == null){
            throw new BusinessRuleException("The unit has no monthly rent set yet");
        }

        lease.setRentAtSigning(unit.getMonthlyRent());


        unit.setVacant(false);

        unitRepository.save(unit);

        var savedLease = leaseRepository.save(lease);

        String leaseCreated = "A lease for " + tenant.getFirstName() + " " + tenant.getLastName() +  " has been successfully created";

        notificationService.systemNotification(leaseCreated, tenant.getLandlord(), NotificationType.LEASE_CREATED, channel.EMAIL);

       BigDecimal amountDues =  rentRecordService.createFirstRentRecord(savedLease);

       savedLease.setRentAtSigning(amountDues);

       var saveLease = leaseRepository.save(savedLease);

        return leaseMapper.toLeaseResponseDto(saveLease);
    }

    private boolean ownsIt(Lease lease, Property property, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY);

        boolean exists = false;
        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            if(property.getLandlord() != null && !propertyRepository.existsByIdAndLandlordId(property.getId(), authenticatedUser.getId())){
                throw new BusinessRuleException("Sorry you cant create a Lease");
            }
            lease.setLandlord(authenticatedUser);
            exists= true;

        } else if (isOfficer && authenticatedUser.isActive()) {

            if(authenticatedUser.getOrganisation() != null){
                throw new BusinessRuleException("Sorry you cant create a Lease ");
            }

            if(property.getOrganisation() != null && !propertyRepository.existsByIdAndOrganisationId(property.getId(), authenticatedUser.getOrganisation().getId())){
                throw new BusinessRuleException("Sorry you cant create a Lease");
            }

            lease.setOrganisation(authenticatedUser.getOrganisation());
            exists= true;

        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {


            if(!propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property)){
                throw new BusinessRuleException("You cant create a Lease");
            }

            if(authenticatedUser.getOrganisation() != null){
                lease.setOrganisation(authenticatedUser.getOrganisation());
                exists= true;
            } else if (authenticatedUser.getLandlord() != null) {
                lease.setLandlord(authenticatedUser.getLandlord());
                exists= true;
            }else {
                throw new BusinessRuleException("Sorry you cant create a Lease");
            }


        }else {
            throw new BusinessRuleException("You cannot create a lease");
        }
        return exists;
    }

    @Transactional
    public LeaseResponseDto createOnBoardingLease(OnboardLeaseDto dto, User authenticatedUser){

        if (!authenticatedUser.isActive()) {
            throw new BusinessRuleException("Your account is inactive");
        }

        Role role = authenticatedUser.getRole();
        if (role == null) {
            throw new BusinessRuleException("Your account has no role assigned");
        }

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY);

        Unit unit = unitRepository.findById(dto.unitId())
                .orElseThrow(()-> new ResourceNotFound("Unit not found"));

        if (unit.getProperty() == null) {
            throw new BusinessRuleException("Unit has no property");
        }
        if (unit.getMonthlyRent() == null) {
            throw new BusinessRuleException("Unit has no monthly rent set");
        }
        if (unit.getProperty().getPreferredBillingDay() == 0) {
            throw new BusinessRuleException("Property has no billing day set");
        }

        boolean isVacant = leaseRepository.existsByUnitAndLeaseStatusIn(unit, List.of(LeaseStatus.ACTIVE, LeaseStatus.PENDING_APPROVAL));

        if(isVacant){
            throw new BusinessRuleException("Unit has already been rented");
        }


        Lease lease = new Lease();

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            if(!unit.getProperty().getLandlord().getId().equals(authenticatedUser.getId())){
                throw new BusinessRuleException("You don't own this Property");
            }

            lease.setLandlord(authenticatedUser);
            lease.setLeaseStatus(LeaseStatus.ACTIVE);
            lease.setPreferredBillingDay(unit.getProperty().getPreferredBillingDay());


        } else if (isOfficer && authenticatedUser.isActive()) {

            if(!unit.getProperty().getOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("You don't own this Property");
            }

            lease.setOrganisation(authenticatedUser.getOrganisation());
            lease.setLeaseStatus(LeaseStatus.ACTIVE);
            lease.setPreferredBillingDay(unit.getProperty().getPreferredBillingDay());

        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

            boolean assigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, unit.getProperty());

            if(!assigned){
                throw new BusinessRuleException("Property not found or youre not assigned");
            }

            if(authenticatedUser.getLandlord() != null){
                lease.setLandlord(authenticatedUser.getLandlord());
            }

            if(authenticatedUser.getOrganisation() != null){
                lease.setOrganisation(authenticatedUser.getOrganisation());
            }

            if (lease.getLandlord() == null && lease.getOrganisation() == null) {
                throw new BusinessRuleException("Caretaker is not linked to a landlord or organisation");
            }

            lease.setLeaseStatus(LeaseStatus.ACTIVE);

            String newLease = "Theirs a new tenant on board who has leased";

            notificationService.systemNotification(newLease,authenticatedUser.getLandlord(),NotificationType.New_Tenant_Leased,channel.EMAIL);


        }else {
            throw new BusinessRuleException("You cant create a lease");
        }

        Tenant tenant = tenantRepository.findById(dto.tenantId())
                .orElseThrow(() -> new ResourceNotFound("Tenant not found"));

        boolean isOwner = assertTenantBelongsToOwner(tenant, lease);

        if(!isOwner){
            throw new BusinessRuleException("You cant create a lease tied to this tenant");
        }

        LocalDate today = LocalDate.now();
        BigDecimal rent = unit.getMonthlyRent();
        int billingDay = unit.getProperty().getPreferredBillingDay();


        if (dto.startDate().isAfter(today)) {
            throw new BusinessRuleException("Start date cannot be in the future");
        }

        BigDecimal depositAmountPaid;

        if (dto.depositAmountPaid().signum() > 0) {
            lease.setDepositPaid(dto.depositAmountPaid());
        }


        lease.setStartDate(dto.startDate());
        lease.setTenant(tenant);
        lease.setUnit(unit);
        lease.setRentAtSigning(unit.getMonthlyRent());
        lease.setDepositAmount(unit.getDepositAmount());
        unit.setVacant(false);
        unitRepository.save(unit);

        lease = leaseRepository.save(lease);



        LocalDate currentCycleStart = cycleStartFor(today, billingDay);
        LocalDate firstCycleStart = cycleStartFor(dto.startDate(), billingDay);

        int maxCycles = (int) ChronoUnit.MONTHS.between(firstCycleStart, currentCycleStart) + 1;

        if (dto.unpaidCycles() < 0) {
            throw new BusinessRuleException("Unpaid cycles cannot be negative");
        }
        if (dto.unpaidCycles() > maxCycles) {
            throw new BusinessRuleException(
                    "Unpaid cycles (" + dto.unpaidCycles() + ") exceed the billing cycles since lease start (" + maxCycles + ")");
        }

        int cycles = Math.max(dto.unpaidCycles(), 1);
        BigDecimal remainingPaid = BigDecimal.ZERO;

        if (dto.unpaidCycles() > 0) {
            if (dto.rentPartialAmountPaid() == null) {
                throw new BusinessRuleException("Partial amount paid is required when there are unpaid cycles");
            }
            remainingPaid = dto.rentPartialAmountPaid();
            BigDecimal totalOwed = rent.multiply(BigDecimal.valueOf(cycles));

            if (remainingPaid.signum() < 0 || remainingPaid.compareTo(totalOwed) >= 0) {
                throw new BusinessRuleException("Partial amount must be zero or more and less than the total owed");
            }
        }

        for (int i = cycles - 1; i >= 0; i--) {
            LocalDate periodStart = currentCycleStart.minusMonths(i);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

            BigDecimal amountPaid;
            if (dto.unpaidCycles() == 0) {
                amountPaid = rent;
            } else {
                amountPaid = remainingPaid.min(rent);
                remainingPaid = remainingPaid.subtract(amountPaid);
            }

            rentRecordService.createRecord(lease, periodStart, periodEnd, rent, amountPaid);
        }

        return leaseMapper.toLeaseResponseDto(lease);
    }


    private boolean assertTenantBelongsToOwner(Tenant tenant, Lease lease) {

        boolean isOwner = false;

        if(tenant.getLandlord() != null){
            if(tenant.getLandlord().getId().equals(lease.getLandlord().getId())){
                isOwner = true;
            }
        }else if(tenant.getOrganisation() != null){
            if(tenant.getOrganisation().getId().equals(lease.getOrganisation().getId())){
                isOwner = true;
            }
        }else {
            throw new BusinessRuleException("Invalid exception. You cannot perform this operation");
        }

        return isOwner;
    }

    private LocalDate cycleStartFor(LocalDate date, int billingDay) {
        LocalDate start = date.withDayOfMonth(billingDay);
        return start.isAfter(date) ? start.minusMonths(1) : start;
    }


    @Transactional
    public void endLease( Long  leaseId,EndLeaseDto dto, User authenticatedUser) {

        boolean isOfficer =  authenticatedUser.getRole().equals(Role.CHAIRMAN)
                ||  authenticatedUser.getRole().equals(Role.SECRETARY);

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(()-> new ResourceNotFound("Lease not Found"));


        if(!ownsIt(lease,lease.getUnit().getProperty(), authenticatedUser)){
            throw new BusinessRuleException("You are not allowed to end this lease");
        }


        lease.setDepositDeductions(dto.depositDeductions());
        lease.setDepositRefunded(dto.depositRefunded());
        lease.setDeductionReason(dto.deductionReason());
        lease.getUnit().setVacant(true);
        unitRepository.save(lease.getUnit());
        lease.setEndDate(LocalDate.now());
        lease.setLeaseStatus(LeaseStatus.ENDED);
        leaseRepository.save(lease);

    }

    public List<LeaseResponseDto> getAllLeaseByProperty(Long propertyId,User authenticatedUser) {

        Property property  = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFound("Property not found"));

        boolean isOfficer =  authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        List<Lease> leaseList = leaseRepository.findByUnit_Property(property);

        boolean isAllowed;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(property.getLandlord() == null
                    || !property.getLandlord().getId().equals(authenticatedUser.getId())){
                throw new BusinessRuleException("Sorry you cant view this Lease");
            }
            isAllowed = true;
        }else if(isOfficer && authenticatedUser.isActive()) {
            if (authenticatedUser.getOrganisation() == null
                    || !propertyRepository.existsByIdAndOrganisationId(propertyId, authenticatedUser.getOrganisation().getId())) {
                throw new BusinessRuleException("You are not allowed to view leases for this property");
            }
            isAllowed = true;
        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {
            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property);

            if (!isAssigned) {
                throw new BusinessRuleException("You are not assigned to this property");
            }

            isAllowed = true;
        }else {
            throw new BusinessRuleException("You are not allowed to view leases");
        }

        if(!isAllowed) {
            throw new BusinessRuleException("You are not allowed to view leases");
        }

        return leaseList
                .stream()
                .map(leaseMapper :: toLeaseResponseDto)
                .toList();
    }

    public LeaseResponseDto getLeaseByUnit(Long unitId,User authenticatedUser) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(()-> new ResourceNotFound("Unit not Found"));

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        Lease lease  = leaseRepository.findByUnitAndLeaseStatus(unit, LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFound("Lease not found"));

        boolean isAllowed;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!unit.getProperty().getLandlord().getId().equals(authenticatedUser.getId())){
                throw new BusinessRuleException("You cant see the lease for this property");
            }

            isAllowed = true;
        }else if(isOfficer && authenticatedUser.isActive()) {
            if(!unit.getProperty().getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())) {
                throw new BusinessRuleException("You cant see the lease for this property");
            }

            isAllowed = true;
        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)) {
            boolean isAssigned = propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, unit.getProperty());

            if (!isAssigned) {
                throw new BusinessRuleException("You are not allowed to view leases for this property");
            }

            isAllowed = true;
        }else {
            throw new BusinessRuleException("You are not allowed to view leases");
        }

        if(!isAllowed) {
            throw new BusinessRuleException("You are not allowed to view leases");
        }

        return leaseMapper.toLeaseResponseDto(lease);
    }

    public List<LeaseResponseDto> getAllLeaseUnitHistory(Long unitId,User authenticatedUser) {
            Unit unit = unitRepository.findById(unitId)
                    .orElseThrow(()-> new ResourceNotFound("Unit not Found"));

            boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                    || authenticatedUser.getRole().equals(Role.SECRETARY)
                    || authenticatedUser.getRole().equals(Role.TREASURER);

            List<Lease> leaseList = leaseRepository.findByUnit(unit);

            boolean isAllowed;

            if(authenticatedUser.getRole().equals(Role.LANDLORD)){
                if(unit.getProperty().getLandlord() != null && !unit.getProperty().getLandlord().getId().equals(authenticatedUser.getId())){
                    throw new BusinessRuleException("You cant see the lease for this property");
                }

                isAllowed = true;
            }else if(isOfficer && authenticatedUser.isActive()) {
                if(unit.getProperty().getOrganisation() != null && !unit.getProperty().getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())){
                    throw new BusinessRuleException("You cant see the lease for this property");
                }
                isAllowed = true;
            } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

                boolean isAssigned = propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, unit.getProperty());

                if (!isAssigned) {
                    throw new BusinessRuleException("You are not allowed to view leases for this property");
                }
                isAllowed = true;

            }else {
                throw new BusinessRuleException("You are not allowed to view leases");
            }

            if (!isAllowed) {
                throw new BusinessRuleException("You are not allowed to view leases");
            }

            return leaseList
                    .stream()
                    .map(leaseMapper :: toLeaseResponseDto)
                    .toList();
    }

}
