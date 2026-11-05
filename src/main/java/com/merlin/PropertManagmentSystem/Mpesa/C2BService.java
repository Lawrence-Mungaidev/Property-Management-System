package com.merlin.PropertManagmentSystem.Mpesa;


import com.merlin.PropertManagmentSystem.TransactionLog.Source;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLog;
import com.merlin.PropertManagmentSystem.TransactionLog.TransactionLogService;
import com.merlin.PropertManagmentSystem.Unit.Unit;
import com.merlin.PropertManagmentSystem.Unit.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class C2BService {

    private static final Logger log = LoggerFactory.getLogger(C2BService.class);
    private static final DateTimeFormatter TRANS_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UnitRepository unitRepository;
    private final TransactionLogService transactionLogService;


    public boolean validate(C2BCallbackPayload p) {
        return findUnit(p.businessShortCode(), p.billRefNumber()).isPresent();
    }


    public boolean confirm(C2BCallbackPayload p, Source source) {

        String account = normalize(p.billRefNumber());
        Optional<Unit> unitOpt = findUnit(p.businessShortCode(), account);

        TransactionLog txn;
        try {
            transactionLogService.saveTransactionLog(toLog(p, account, unitOpt.orElse(null)), account, source);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate C2B callback ignored: {}", p.transId());
            return false;
        }
        return true;
    }

    private Optional<Unit> findUnit(String shortCode, String rawAccount) {
        if (shortCode == null || rawAccount == null) return Optional.empty();
        return unitRepository.findByAccountNumberAndProperty_Landlord_PayBill(
                normalize(rawAccount), shortCode);
    }

    private String normalize(String account) {
        return account == null ? null : account.trim();
    }

    private TransactionLog toLog(C2BCallbackPayload p, String account, Unit unit) {
        TransactionLog t = new TransactionLog();
        t.setTransactionCode(p.transId());
        t.setAmount(p.transAmount());
        t.setAccountNumber(account);
        t.setMSISDN(p.msisdn());
        t.setPayerName(Stream.of(p.firstName(), p.middleName(), p.lastName())
                .filter(Objects::nonNull).collect(Collectors.joining(" ")));
        t.setTransactionTime(LocalDateTime.parse(p.transTime(), TRANS_TIME));
        t.setPayBill(p.businessShortCode());
        t.setReceivedAt(LocalDateTime.now(ZoneId.of("Africa/Nairobi")));
        t.setTransactionSource(Source.CALLBACK);
        t.setUnit(unit);
        return t;
    }
}