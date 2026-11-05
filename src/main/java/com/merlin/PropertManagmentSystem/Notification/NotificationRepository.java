package com.merlin.PropertManagmentSystem.Notification;

import com.merlin.PropertManagmentSystem.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatusAndReceiver(Status status,  User user);
    List<Notification> findByReceiver(User user);

}
