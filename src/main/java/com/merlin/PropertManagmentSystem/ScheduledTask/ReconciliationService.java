package com.merlin.PropertManagmentSystem.ScheduledTask;

import com.merlin.PropertManagmentSystem.Mpesa.C2BCallbackPayload;
import com.merlin.PropertManagmentSystem.Mpesa.C2BService;
import com.merlin.PropertManagmentSystem.Mpesa.PullTransactionsClient;
import com.merlin.PropertManagmentSystem.Organisation.OrganisationRepository;
import com.merlin.PropertManagmentSystem.TransactionLog.Source;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final PullTransactionsClient pullTransactions;
    private final C2BService c2bService;

    private record PullResult(int recovered, int failed) {}

    @Scheduled(cron = "0 0 */4 * * *", zone = "Africa/Nairobi")
    public void reconcile() {
        LocalDateTime to = LocalDateTime.now(NAIROBI);
        reconcileRange(to.minusHours(24), to);
    }

    private void reconcileRange(LocalDateTime from, LocalDateTime to) {
        for (String payBill : payBills()) {
            try {
                PullResult r = pullAll(payBill, from, to);
                if (r.recovered() > 0 || r.failed() > 0) {
                    log.warn("Reconciliation {}: recovered {}, failed {} ({} to {})",
                            payBill, r.recovered(), r.failed(), from, to);
                }
            } catch (Exception e) {
                log.error("Pull failed for {}", payBill, e);
            }
        }
    }

    private List<String> payBills() {
        return Stream.concat(
                        userRepository.findByPayBillIsNotNull().stream(),
                        organisationRepository.findByPayBillIsNotNull().stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private PullResult pullAll(String shortCode, LocalDateTime from, LocalDateTime to) {
        int recovered = 0;
        int failed = 0;
        int offset = 0;

        for (int page = 0; page < 100; page++) {          // hard cap against a bad response
            List<C2BCallbackPayload> batch = pullTransactions.pullTransactions(shortCode, from, to, offset);
            if (batch.isEmpty()) break;

            for (C2BCallbackPayload p : batch) {
                try {
                    if (c2bService.confirm(p, Source.RECONCILIATION)) recovered++;
                } catch (Exception e) {
                    failed++;
                    log.error("Failed to process pulled transaction {}", p.transId(), e);
                }
            }
            offset += batch.size();                       // see the offset test: may need += 1
        }
        return new PullResult(recovered, failed);
    }
}