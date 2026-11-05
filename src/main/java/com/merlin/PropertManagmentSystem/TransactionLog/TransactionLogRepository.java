package com.merlin.PropertManagmentSystem.TransactionLog;

import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    boolean existsByTransactionCode(String transactionCode);

    List<TransactionLog> findByLandLord(User authenticatedUser);
    List<TransactionLog> findByOrganisation(Organisation organisation);
    Optional<TransactionLog> findByTransactionCode(String transactionCode);

}
