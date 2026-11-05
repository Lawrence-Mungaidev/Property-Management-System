package com.merlin.PropertManagmentSystem.NotificationBatch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationBatchRepository extends JpaRepository<NotificationBatch,Long> {

    List<NotificationBatch> findByInitiatedBy_Id(Long userId);
}
