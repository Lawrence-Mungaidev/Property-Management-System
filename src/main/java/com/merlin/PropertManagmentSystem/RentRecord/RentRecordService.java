package com.merlin.PropertManagmentSystem.RentRecord;

import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Credit.CreditRepository;
import com.merlin.PropertManagmentSystem.Credit.CreditStatus;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Lease.DepositStatus;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Lease.LeaseRepository;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.PaymentAllocation.AllocationType;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocation;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocationService;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class RentRecordService {

    private final RentRecordRepository rentRecordRepository;
    private final RentRecordMapper rentRecordMapper;
    private  final PropertyAsignRepository propertyAsignRepository;
    private final UnitRepository unitRepository;
    private final LeaseRepository leaseRepository;
    private final CreditRepository creditRepository;
    private final PaymentAllocationService paymentAllocationService;

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");


    public void createFirstRentRecord(Lease lease) {

        LocalDate today = LocalDate.now(NAIROBI);
        LocalDate billingStart = lease.getStartDate().isBefore(today) ? today : lease.getStartDate();

        LocalDate next = nextBillingDate(billingStart, lease.getPreferredBillingDay());

        if (!next.isAfter(billingStart)) {          // guards the case where start is the billing day
            next = billingStart.plusMonths(1);
        }
        long days = Math.min(ChronoUnit.DAYS.between(billingStart, next), 30);

        BigDecimal rentAmount = lease.getRentAtSigning();
        BigDecimal amountDue = days < 10
                ? rentAmount
                : rentAmount.multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        createRecord(lease, billingStart, next.minusDays(1), amountDue, BigDecimal.ZERO, false);


        applyPayment(lease);

    }


    public RentRecord createRecord(Lease lease, LocalDate periodStart, LocalDate periodEnd,
                            BigDecimal amountDue, BigDecimal amountPaid, boolean declared) {

        if (amountDue.signum() <= 0) {
            throw new BusinessRuleException("Amount due must be positive");
        }
        if (amountPaid.signum() < 0 || amountPaid.compareTo(amountDue) > 0) {
            throw new BusinessRuleException("Amount paid must be between 0 and the amount due");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new BusinessRuleException("Period end is before period start");
        }
        if (rentRecordRepository.existsByLeaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(lease, periodStart, periodEnd)) {
            throw new BusinessRuleException("A rent record already covers this period");
        }

        RentRecord record = new RentRecord();
        record.setLease(lease);
        record.setStartDate(periodStart);
        record.setEndDate(periodEnd);
        record.setAmountDue(amountDue);
        record.setAmountPaid(amountPaid);
        record.setDeclared(declared);
        record.setRentStatus(statusFor(amountDue, amountPaid));
        return rentRecordRepository.save(record);
    }

    private RentStatus statusFor(BigDecimal due, BigDecimal paid) {
        if (paid.compareTo(due) >= 0) return RentStatus.PAID;
        if (paid.signum() == 0) return RentStatus.UNPAID;
        return RentStatus.PARTIALLY_PAID;
    }

    @Transactional
    public void applyPayment(Lease lease) {

        Tenant tenant = lease.getTenant();

        List<Credit> creditList = creditRepository.findByTenantAndCreditStatusInOrderByCreatedAtAsc(tenant, List.of(CreditStatus.UNUSED,CreditStatus.PARTIALLY_USED));

        // 1. Deposit first, if still owing

        BigDecimal initialOwingDeposit = lease.getDepositAmount().subtract(lease.getDepositPaid());
        if (initialOwingDeposit.signum() > 0) {


            for (Credit credit : creditList) {
                if (lease.getDepositPaid().compareTo(lease.getDepositAmount()) >= 0){
                    break;
                }

                BigDecimal owingDeposit = lease.getDepositAmount().subtract(lease.getDepositPaid());;

                BigDecimal remainingAmount = credit.getRemainingAmount();
                if (remainingAmount.signum() <= 0) continue;

                BigDecimal takeAmount = owingDeposit.min(remainingAmount);

                lease.setDepositPaid(lease.getDepositPaid().add(takeAmount));

                if(lease.getDepositPaid().compareTo(lease.getDepositAmount()) >= 0){
                            lease.setDepositStatus(DepositStatus.FULLY_PAID);
                }else {
                    lease.setDepositStatus(DepositStatus.PARTIALLY_PAID);
                }

                credit.setRemainingAmount(remainingAmount.subtract(takeAmount));

                if(credit.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0){
                    credit.setCreditStatus(CreditStatus.FULLY_USED);
                } else {
                    credit.setCreditStatus(CreditStatus.PARTIALLY_USED);
                }

                creditRepository.save(credit);

                paymentAllocationService.addAllocation(null, credit, AllocationType.DEPOSIT, takeAmount,lease);

            }


        }


        List<RentRecord> rentRecordList = rentRecordRepository
                .findByLeaseAndRentStatusInOrderByStartDateAsc(
                        lease, List.of(RentStatus.UNPAID, RentStatus.PARTIALLY_PAID));

        for (RentRecord record : rentRecordList) {
            for (Credit credit : creditList) {
                if (record.getAmountDue().compareTo(record.getAmountPaid()) <= 0) {
                    break;
                }

                BigDecimal owingRent = record.getAmountDue().subtract(record.getAmountPaid());


                if (credit.getRemainingAmount().signum() <= 0) {
                    continue;
                }
                BigDecimal takeRentAmount = owingRent.min(credit.getRemainingAmount());

                record.setAmountPaid(record.getAmountPaid().add(takeRentAmount));
                credit.setRemainingAmount(credit.getRemainingAmount().subtract(takeRentAmount));


                if(credit.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0){
                    credit.setCreditStatus(CreditStatus.FULLY_USED);
                } else {
                    credit.setCreditStatus(CreditStatus.PARTIALLY_USED);
                }


                record.setRentStatus(statusFor(record.getAmountDue(), record.getAmountPaid()));

                paymentAllocationService.addAllocation(record,credit,AllocationType.RENT,takeRentAmount,lease);

                creditRepository.save(credit);

            }
        }

        // Entities are managed inside the transaction, but saving explicitly is harmless
        leaseRepository.save(lease);
        rentRecordRepository.saveAll(rentRecordList);
    }



    public LocalDate nextBillingDate(LocalDate from, int billingDay) {
        LocalDate candidate = from.withDayOfMonth(billingDay);
        if (!candidate.isAfter(from)) {
            candidate = candidate.plusMonths(1);
        }
        return candidate;
    }

    public List<RentRecordResponseDto> getAllRentRecords(User authenticatedUser){

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        List<RentRecord> rentRecordList = new ArrayList<>();

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            rentRecordList = rentRecordRepository.findByLease_Unit_Property_Landlord(authenticatedUser);

        } else if (isOfficer) {

            rentRecordList = rentRecordRepository.findByLease_Unit_Property_Organisation(authenticatedUser.getOrganisation());

        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

            List<Property> assigned = propertyAsignRepository
                    .findByCareTakerAndUnAssignedAtIsNull(authenticatedUser)
                    .stream()
                    .map(PropertyAsign :: getProperty)
                    .toList();

            if (assigned.isEmpty()) {
                return List.of();
            }

            rentRecordList = rentRecordRepository.findByLease_Unit_PropertyIn(assigned);

        }else {
            throw new BusinessRuleException("Invalid role");
        }

        return rentRecordList
                .stream()
                .map(rentRecordMapper ::toRentRecordResponseDto)
                .toList();
    }

    public RentRecordResponseDto getRentRecordById(User authenticatedUser, Long rentRecordId) {

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        RentRecord rentRecord = rentRecordRepository.findById(rentRecordId)
                .orElseThrow(()-> new ResourceNotFound("Rent Record not Found"));

        RentRecord rightRentRecord;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!rentRecord.getLease().getLandlord().equals(authenticatedUser)){
                throw new BusinessRuleException("Invalid landlord");
            }
            rightRentRecord = rentRecord;
        } else if (isOfficer) {
            if(!rentRecord.getLease().getOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("Invalid organisation");
            }
            rightRentRecord = rentRecord;
        } else if (authenticatedUser.getRole().equals(Role.CARETAKER)) {

            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, rentRecord.getLease().getTenant().getLandlord().getProperty());

            if(!isAssigned){
                throw new BusinessRuleException("You cant see this rent records since youre not assigned anymore");
            }
            rightRentRecord = rentRecord;

        }

        return rentRecordMapper.toRentRecordResponseDto(rentRecord);
    }

}
