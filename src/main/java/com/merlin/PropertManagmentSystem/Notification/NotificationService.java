package com.merlin.PropertManagmentSystem.Notification;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Messaging.EmailService;
import com.merlin.PropertManagmentSystem.Messaging.SmsService;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SmsService smsService;


    public void systemNotification(String message, User receiver, NotificationType notificationType, channel channel) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setReceiver(receiver);
        notification.setNotificationType(notificationType);
        notification.setSentAt(LocalDateTime.now());
        notification.setChannel(channel);

        if(channel.equals(com.merlin.PropertManagmentSystem.Notification.channel.EMAIL)) {
            emailService.sendTextEmail(receiver.getEmail(), receiver.getFirstName(), "PROPERTY MANAGEMENT", message );
        }else {
            smsService.sendSms(receiver.getPhoneNumber(),  message);
        }
        notification.setStatus(Status.SENT);

        notificationRepository.save(notification);
    }
    
    public void markAsRead(Long notificationId, User authentcatedUser){
        boolean isOfficer = authentcatedUser.getRole().equals(Role.CHAIRMAN)
                || authentcatedUser.getRole().equals(Role.SECRETARY)
                || authentcatedUser.getRole().equals(Role.TREASURER);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFound("Notification Not Found"));
        
        Notification rightNotification = null;
        
        if(authentcatedUser.getRole().equals(Role.LANDLORD)) {
            if(!notification.getLandlord().equals(authentcatedUser)){
                throw new BusinessRuleException("You cant read the notification");
            }
            rightNotification = notification;
        } else if (isOfficer && authentcatedUser.isActive()) {
            if(!notification.getOrganisation().equals(authentcatedUser.getOrganisation())){
                throw new BusinessRuleException("You cant read the notification");
            }
            
            rightNotification = notification;            
        }else if(authentcatedUser.getRole().equals(Role.CARETAKER)){
            
            if(authentcatedUser.getLandlord() != null ){
                if(notification.getLandlord().equals(authentcatedUser.getLandlord())){
                    rightNotification = notification;
                }else {
                    throw new BusinessRuleException("You cant read the notification");
                }
                
            }
            
            if(authentcatedUser.getOrganisation() != null){
                if(notification.getOrganisation().equals(authentcatedUser.getOrganisation())){
                    rightNotification = notification;
                }else {
                    throw new BusinessRuleException("You cant read the notification");
                }
            }
            
            
        }else {
            throw new BusinessRuleException("You cant read the notification");
        }
        
        
        rightNotification.setRead(true);
        
        notificationRepository.save(rightNotification);

    }


    public List<NotificationResponseDto> sentNotification(User authenticatedUser){
        return notificationRepository.findByStatusAndReceiver(Status.SENT, authenticatedUser)
                .stream()
                .map(notificationMapper :: toNotificationResponseDto)
                .toList();
    }

    public List<NotificationResponseDto> myNotifications(User authenticatedUser){
        return notificationRepository.findByReceiver(authenticatedUser)
                .stream()
                .map(notificationMapper :: toNotificationResponseDto)
                .toList();
    }


}
