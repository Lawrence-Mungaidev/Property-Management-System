package com.merlin.PropertManagmentSystem.TransactionLog;

import com.merlin.PropertManagmentSystem.Credit.Credit;
import com.merlin.PropertManagmentSystem.Credit.CreditService;
import com.merlin.PropertManagmentSystem.Credit.CreditRepository;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Lease.Lease;
import com.merlin.PropertManagmentSystem.Lease.LeaseRepository;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Notification.NotificationService;
import com.merlin.PropertManagmentSystem.PaymentDispute.PaymentDispute;
import com.merlin.PropertManagmentSystem.PaymentDispute.PaymentDisputeService;
import com.merlin.PropertManagmentSystem.PaymentDispute.PaymentDisputeType;
import com.merlin.PropertManagmentSystem.RentRecord.RentRecordService;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogMapper  transactionLogMapper;
    private final UnitRepository unitRepository;
    private final CreditService creditService;
    private final LeaseRepository leaseRepository;
    private final PaymentDisputeService paymentDisputeService;
    private final RentRecordService rentRecordService;

    public void saveTransactionLog(TransactionLog transactionLog,String account, Source source) {

        TransactionLog trnLog;
        PaymentDispute paymentDispute;


        if(transactionLogRepository.existsByTransactionCode(transactionLog.getTransactionCode())){
            log.info("Duplicate C2B callback ignored:");

        }

        if(!unitRepository.existsByAccountNumberAndProperty_Landlord_PayBill(account,transactionLog.getPayBill())){
            log.info("No unit exists by that account");

            String unMatchedUnit = "No unit exists by that account";

            paymentDisputeService.createPaymentDispute(transactionLog, PaymentDisputeType.MIS_DIRECTED, unMatchedUnit);


        }

        Unit unit = unitRepository.findByAccountNumberAndProperty_Landlord_PayBill(account, transactionLog.getPayBill())
                .orElseThrow(() -> new ResourceNotFound("Unit not found"));


        if(leaseRepository.existsByUnitAndLeaseStatusIn(unit, List.of(LeaseStatus.ENDED))){
            log.info("The unit is vacant");

            String vacantUnit = "The unit is vacant";

            paymentDisputeService.createPaymentDispute(transactionLog, PaymentDisputeType.VACANT, vacantUnit);

        }
        transactionLog.setTransactionSource(source);

        var savedTransaction = transactionLogRepository.save(transactionLog);

        Lease lease = leaseRepository.findByUnitAndLeaseStatus(unit, LeaseStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException("No active lease for this unit"));

        Tenant tenant = lease.getTenant();

        creditService.saveCredit(tenant, savedTransaction);
        rentRecordService.applyPayment(lease);
    }

    private Optional<Unit> findUnit(String shortCode, String rawAccount) {
        if (shortCode == null || rawAccount == null) return Optional.empty();
        return unitRepository.findByAccountNumberAndProperty_Landlord_PayBill(
                trimAccountNumber(rawAccount), shortCode);
    }

    private String trimAccountNumber(String account) {
        return account == null ? null : account.trim();
    }

    public List<TransactionLogResponseDto> getAllTransactionLog(User authenticatedUser){

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        List<TransactionLog> transactionLogs;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            transactionLogs = transactionLogRepository.findByLandLord(authenticatedUser);
        } else if (isOfficer && authenticatedUser.isActive()) {

            transactionLogs = transactionLogRepository.findByOrganisation(authenticatedUser.getOrganisation());

        }else {
            throw new BusinessRuleException("You cant view the transaction logs");
        }


        return transactionLogs
                .stream()
                .map(transactionLogMapper :: toTenantResponseDto)
                .toList();
    }

    public TransactionLogResponseDto searchTransactionLog(String transactionCode, User authenticatedUser){

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        TransactionLog transactionLog;

        if(authenticatedUser.getRole().equals(Role.LANDLORD) || isOfficer && authenticatedUser.isActive()){
            transactionLog = transactionLogRepository.findByTransactionCode(transactionCode)
                    .orElseThrow(() -> new ResourceNotFound("No transaction code found"));
        }else {
            throw new BusinessRuleException("You cant view the transaction logs");
        }

        return transactionLogMapper.toTenantResponseDto(transactionLog);
    }

    public TransactionLogResponseDto getTransactionLogById(Long transactionLogId, User authenticatedUser){
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        TransactionLog transactionLog;

        if(authenticatedUser.getRole().equals(Role.LANDLORD) || isOfficer && authenticatedUser.isActive()){
            transactionLog = transactionLogRepository.findById(transactionLogId)
                    .orElseThrow(() -> new ResourceNotFound("No transaction code found"));
        }else {
            throw new BusinessRuleException("You cant view the transaction logs");
        }

        return transactionLogMapper.toTenantResponseDto(transactionLog);
    }


}
