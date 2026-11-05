package com.merlin.PropertManagmentSystem.NotificationBatch;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Lease.LeaseStatus;
import com.merlin.PropertManagmentSystem.Messaging.SmsService;
import com.merlin.PropertManagmentSystem.Notification.*;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.Tenant.Tenant;
import com.merlin.PropertManagmentSystem.Tenant.TenantRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationBatchService {

    private final NotificationBatchRepository notificationBatchRepository;
    private final NotificationBatchMapper notificationBatchMapper;
    private final NotificationRepository notificationRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;
    private final SmsService smsService;
    private final UserRepository userRepository;

    public NotificationBatchResponseDto createNotificationBatch(NotificationBatchDto dto, User authenticatedUser) {
        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);


        NotificationBatch  notificationBatch = notificationBatchMapper.toNotificationBatchDto(dto);
        notificationBatch.setInitiatedBy(authenticatedUser);

        Property property = propertyRepository.findById(dto.propertyId())
                .orElseThrow(() -> new BusinessRuleException("Property not found"));

        List<Tenant> tenantList;

        if (authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(!property.getLandlord().equals(authenticatedUser)){
                throw new BusinessRuleException("The property isn't yours. Please check again");
            }

            notificationBatch.setBelongsToLandlord(authenticatedUser);

            tenantList = tenantRepository.findDistinctByLandlordAndLeases_LeaseStatus(authenticatedUser, LeaseStatus.ACTIVE);

        } else if (isOfficer && authenticatedUser.isActive()) {
            Organisation organisation = authenticatedUser.getOrganisation();

            if(!property.getOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("The property isn't yours. Please check again");
            }
            notificationBatch.setBelongsToOrganisation(organisation);

            tenantList = tenantRepository.findDistinctByOrganisationAndLeases_LeaseStatus(organisation, LeaseStatus.ACTIVE);

        }else {
            throw new BusinessRuleException("Invalid role");
        }

        for (Tenant tenant : tenantList) {
            Notification notification = new Notification();
            notification.setNotificationBatch(notificationBatch);
            notification.setSentAt(LocalDateTime.now());
            notification.setNotificationType(dto.notificationType());
            notification.setChannel(channel.SMS);
            if(tenant.getPhoneNumber() != null){
                smsService.sendSms(tenant.getPhoneNumber(),dto.message());
                notification.setStatus(Status.SENT);
            }

            notification.setReceiverTenant(tenant);
            notification.setStatus(Status.PENDING);

            notificationRepository.save(notification);
        }

        notificationBatch.setMessage(dto.message());
        notificationBatch.setNbType(dto.nbType());

        var savedNB =notificationBatchRepository.save(notificationBatch) ;

        return notificationBatchMapper.toNotificationBatchResponseDto(savedNB);
    }


    public NotificationBatchResponseDto adminSendingBatch(NotificationBatchDto dto, User authenticatedUser) {

        if(!authenticatedUser.getRole().equals(Role.ADMIN)){
            throw new BusinessRuleException("Invalid role");
        }

        NotificationBatch  notificationBatch = notificationBatchMapper.toNotificationBatchDto(dto);

        List<User> landlords = userRepository.findAllByRole(Role.LANDLORD);
        List<User> chairMans = userRepository.findAllByRole(Role.CHAIRMAN);

        List<User> all = new ArrayList<>();
        all.addAll(landlords);
        all.addAll(chairMans);

        for(User user : all){
            Notification notification = new Notification();
            notification.setNotificationBatch(notificationBatch);
            notification.setAdmin(authenticatedUser);
            notification.setChannel(channel.SMS);
            if(user.getPhoneNumber() != null){
                smsService.sendSms(user.getPhoneNumber(),dto.message());
                notification.setStatus(Status.SENT);
            }
            notification.setStatus(Status.PENDING);
            notificationRepository.save(notification);

        }

        notificationBatch.setMessage(dto.message());
        notificationBatch.setNbType(dto.nbType());
        var savedNB =notificationBatchRepository.save(notificationBatch) ;

        return notificationBatchMapper.toNotificationBatchResponseDto(savedNB);
    }


    public List<NotificationBatchResponseDto> getAllNotifications(User authenticatedUser) {

        return notificationBatchRepository.findByInitiatedBy_Id(authenticatedUser.getId())
                .stream()
                .map(notificationBatchMapper :: toNotificationBatchResponseDto)
                .toList();
    }

    public NotificationBatchResponseDto  getNotificationById(Long notificationBatchId,  User authenticatedUser) {
        NotificationBatch notificationBatch = notificationBatchRepository.findById(notificationBatchId)
                .orElseThrow(() -> new ResourceNotFound("Notification batch not found"));

        NotificationBatch notificationBatch1;

        boolean isOfficer =  authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY) || authenticatedUser.getRole().equals(Role.TREASURER);

        if(notificationBatch.getBelongsToAdmin().equals(authenticatedUser)){
             notificationBatch1 = notificationBatch;
        } else if (notificationBatch.getBelongsToLandlord().equals(authenticatedUser) && authenticatedUser.getRole().equals(Role.LANDLORD)) {

            notificationBatch1 = notificationBatch;

        } else if (isOfficer && authenticatedUser.isActive()) {

            if(!notificationBatch.getBelongsToOrganisation().equals(authenticatedUser.getOrganisation())){
                throw new BusinessRuleException("The property isn't yours. Please check again");
            }

            notificationBatch1 = notificationBatch;

        }else {
            throw new BusinessRuleException("Invalid role");
        }

        return notificationBatchMapper.toNotificationBatchResponseDto(notificationBatch1);
    }

}
