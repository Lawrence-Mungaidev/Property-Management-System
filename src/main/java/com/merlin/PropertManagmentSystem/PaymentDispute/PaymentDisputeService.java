package com.merlin.PropertManagmentSystem.PaymentDispute;

import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Credit.CreditRepository;
import com.merlin.PropertManagmentSystem.Credit.CreditService;
import com.merlin.PropertManagmentSystem.Credit.CreditStatus;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Lease.DepositStatus;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Lease.LeaseRepository;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.PaymentAllocation.AllocationType;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocation;
import com.merlin.PropertManagmentSystem.PaymentAllocation.PaymentAllocationRepository;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecord;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecordRepository;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecordService;
import com.merlin.PropertManagmentSystem.RentRecord.RentStatus;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Tenant.TenantRepository;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLogRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentDisputeService {

    private  final PaymentDisputeRepository paymentDisputeRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final CreditRepository creditRepository;
    private final RentRecordRepository rentRecordRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final LeaseRepository leaseRepository;
    private final TenantRepository tenantRepository;
    private final CreditService creditService;
    private final RentRecordService rentRecordService;
    private final PaymentDisputeMapper paymentDisputeMapper;

    public void createPaymentDispute(TransactionLog transactionLog, PaymentDisputeType disputeType, String note) {
        PaymentDispute paymentDispute = new PaymentDispute();
        paymentDispute.setTransactionLog(transactionLog);
        paymentDispute.setNote(note);
        paymentDispute.setPaymentDisputeType(disputeType);
        paymentDispute.setCreatedAt(LocalDateTime.now());

        paymentDisputeRepository.save(paymentDispute);

    }

@Transactional
    public PDResponseDTO reversePayment(User authenticatedUser, ReversePaymentDto dto){

        TransactionLog transactionLog = transactionLogRepository.findById(dto.transactionLogId())
                .orElseThrow(()-> new ResourceNotFound("TransactionLog not found"));

    Tenant tenant = tenantRepository.findById(dto.tenantId())
            .orElseThrow(()-> new ResourceNotFound("Tenant not found"));

        PaymentDispute paymentDispute = new PaymentDispute();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);


        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!authenticatedUser.getPayBill().equals(transactionLog.getPayBill())){
                throw new BusinessRuleException("Sorry you cant reverse this transaction");
            }

            if(!tenant.getLandlord().equals(authenticatedUser)){
                throw new BusinessRuleException("Sorry you cant reverse this transaction for this tenant");
            }


        } else if (isOfficer && authenticatedUser.isActive()) {
            if(!authenticatedUser.getOrganisation().getPayBill().equals(transactionLog.getPayBill())){
                throw new BusinessRuleException("Sorry you cant reverse this transaction");
            }

            if(!tenant.getOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("Sorry you cant reverse this transaction");
            }

        }else {
            throw new BusinessRuleException("You cannot reverse any transaction");
        }

        if(creditRepository.existsByTransactionLogAndCreditStatusNot(transactionLog, CreditStatus.REVERSED)){
            throw new BusinessRuleException("The credit is already reversed");
        }

        Credit credit = creditRepository.findByTransactionLog(transactionLog)
                .orElseThrow(() -> new BusinessRuleException("The credit is already reversed"));

        List<PaymentAllocation> paymentAllocationList = paymentAllocationRepository.findByCreditId(credit.getId());

        Lease correctLease = leaseRepository.findByTenantIdAndLeaseStatus(dto.tenantId(), LeaseStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFound("Lease not found. "));

        Lease originalLease = credit.getTransactionLog().getLease();



        for(PaymentAllocation paymentAllocation : paymentAllocationList){
            Lease lease = paymentAllocation.getLease();

            if(paymentAllocation.getAllocationType().equals(AllocationType.DEPOSIT)){

               lease.setDepositPaid(lease.getDepositPaid().subtract(paymentAllocation.getAmount()));
               credit.setReversed(true);
               credit.setCreditStatus(CreditStatus.REVERSED);
               setDepositStatus(lease);
               creditRepository.save(credit);


            }else{
                RentRecord rentRecord = paymentAllocation.getRentRecord();

                rentRecord.setAmountPaid(rentRecord.getAmountPaid().subtract(paymentAllocation.getAmount()));
                rentRecord.setRentStatus(statusFor(rentRecord.getAmountDue(),rentRecord.getAmountPaid()));
                rentRecordRepository.save(rentRecord);

                credit.setCreditStatus(CreditStatus.REVERSED);
                credit.setReversed(true);
                creditRepository.save(credit);

            }

            creditService.saveCredit(tenant, transactionLog);
            paymentAllocation.setReversed(true);


        }

        rentRecordService.applyPayment(correctLease);
        rentRecordService.applyPayment(originalLease);


        paymentDispute.setCorrectLease(correctLease);
        paymentDispute.setResolvedBy(authenticatedUser);
        paymentDispute.setResolvedAt(LocalDateTime.now());
        paymentDispute.setNote(dto.note());
        paymentDispute.setCreatedAt(LocalDateTime.now());

        creditRepository.save(credit);
        var savedPd = paymentDisputeRepository.save(paymentDispute);

        return paymentDisputeMapper.toPDResponseDTO(savedPd);

    }

    private void setDepositStatus(Lease lease){
        if(lease.getDepositPaid().equals(lease.getDepositAmount())){
            lease.setDepositStatus(DepositStatus.FULLY_PAID);
        } else if (lease.getDepositPaid().compareTo(lease.getDepositAmount()) >= 0) {
            lease.setDepositStatus(DepositStatus.PARTIALLY_PAID);
        }
        else {
            lease.setDepositStatus(DepositStatus.NOT_PAID);
        }

        leaseRepository.save(lease);
    }

    public static RentStatus statusFor(BigDecimal amountDue, BigDecimal amountPaid) {
        if (amountPaid.compareTo(amountDue) >= 0) {
            return RentStatus.PAID;
        }
        if (amountPaid.signum() <= 0) {
            return RentStatus.UNPAID;
        }
        return RentStatus.PARTIALLY_PAID;
    }

    public List<PDResponseDTO> getAllPaymentDispute(User authenticatedUser){

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        List<PaymentDispute> paymentDisputes;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){

            paymentDisputes = paymentDisputeRepository.findAllByLandlord(authenticatedUser);
        } else if (isOfficer && authenticatedUser.isActive()) {
            paymentDisputes = paymentDisputeRepository.findAllByOrganisation(authenticatedUser.getOrganisation());

        }else {
            throw new BusinessRuleException("You cannot view payment disputes");
        }

        return paymentDisputes
                .stream()
                .map(paymentDisputeMapper :: toPDResponseDTO)
                .toList();
    }

    public PDResponseDTO getPaymentDispute(Long  paymentDisputeId, User  authenticatedUser){
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        PaymentDispute paymentDispute = paymentDisputeRepository.findById(paymentDisputeId)
                .orElseThrow(() -> new ResourceNotFound("PaymentDispute not found"));

        PaymentDispute rightDispute;

        if(authenticatedUser.getRole().equals(Role.LANDLORD) ) {

            if(!paymentDispute.getLandlord().equals(authenticatedUser)){
                throw new BusinessRuleException("You cannot view payment disputes");
            }
            rightDispute =paymentDispute;

        } else if (isOfficer && authenticatedUser.isActive()) {
            if(!paymentDispute.getOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("You cannot view payment disputes");
            }

            rightDispute =paymentDispute;
        }else {
            throw new BusinessRuleException("You cannot view payment disputes");
        }

        return paymentDisputeMapper.toPDResponseDTO(rightDispute);
    }

}
