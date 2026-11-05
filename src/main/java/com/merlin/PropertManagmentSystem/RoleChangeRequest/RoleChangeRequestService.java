package com.merlin.PropertManagmentSystem.RoleChangeRequest;

import com.merlin.PropertManagmentSystem.Messaging.EmailService;
import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Notification.NotificationService;
import com.merlin.PropertManagmentSystem.Notification.NotificationType;
import com.merlin.PropertManagmentSystem.Notification.channel;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Organisation.OrganisationRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import com.merlin.PropertManagmentSystem.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleChangeRequestService {

    private final RoleChangeRequestRepository roleChangeRequestRepository;
    private final RoleChangeRequestMapper roleChangeRequestMapper;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final OrganisationRepository organisationRepository;

    public RoleCRequestResponseDto CMRoleChangeRequest(User authenticatedUser, RoleChangeRequestDto dto){
        RoleChangeRequest roleChangeRequest = roleChangeRequestMapper.toRoleChangeRequest(dto);

        User targetUser = userRepository.findById(dto.targetUser())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!authenticatedUser.getRole().equals(Role.CHAIRMAN)) {
            throw new BusinessRuleException("You cannot change the role for this user");
        }

        if(targetUser.getOrganisation() == null || !targetUser.getOrganisation().equals(authenticatedUser.getOrganisation())){
            throw new BusinessRuleException("You cannot change the role for this user. If this problem persist please call the developer");
        }
        roleChangeRequest.setStatus(RoleChangeStatus.APPROVED);
        targetUser.setRole(dto.proposedRole());
        userRepository.save(targetUser);

        roleChangeRequest.setCurrentRole(targetUser.getRole());
        roleChangeRequest.setReason(dto.reason());
        roleChangeRequest.setApprovedBy(authenticatedUser);
        roleChangeRequest.setApprovedOn(LocalDateTime.now());
        roleChangeRequest.setInitiatedBy(authenticatedUser);
        roleChangeRequest.setOrganisation(authenticatedUser.getOrganisation());

        String message = targetUser.getFirstName() + " " + targetUser.getLastName() + " Role has been changed successfully ";

        notificationService.systemNotification(message,authenticatedUser, NotificationType.SUCCESSFUL_ROLE_CHANGE, channel.EMAIL);
        emailService.sendTextEmail(authenticatedUser.getEmail(), authenticatedUser.getFirstName(),"ROLE CHANGE SUCCESS", message);


        return roleChangeRequestMapper.toRoleChangeRequestDto(roleChangeRequest);
    }

    public RoleCRequestResponseDto SCRoleChangeRequest(User authenticatedUser, RoleChangeRequestDto dto){

        RoleChangeRequest roleChangeRequest = roleChangeRequestMapper.toRoleChangeRequest(dto);

        if(!authenticatedUser.getRole().equals(Role.SECRETARY)){
            throw new BusinessRuleException("You cannot make a request to change the role for this user");
        }

        User targetedUser = userRepository.findById(dto.targetUser())
                .orElseThrow(() -> new ResourceNotFound("User not found"));

        if(!targetedUser.getOrganisation().equals(authenticatedUser.getOrganisation())){
            throw new BusinessRuleException("You cannot change the role for this user");
        }

        if(!targetedUser.getRole().equals(Role.CHAIRMAN)){
            throw new BusinessRuleException("You cannot change the role for this user");
        }

        if(dto.proposedRole() != Role.MEMBER){
            throw new BusinessRuleException("You can only request the chairman to be a MEMBER");
        }

        boolean pendingRequest = roleChangeRequestRepository.existsByInitiatedByAndStatus(authenticatedUser, RoleChangeStatus.APPROVED);

        if(pendingRequest){
            throw new BusinessRuleException("Theirs a current pending report");
        }

        User targetUser =  userRepository.findById(dto.targetUser())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if( targetUser.getOrganisation() == null || !targetUser.getOrganisation().equals(authenticatedUser.getOrganisation())){
            throw new BusinessRuleException("You cannot make a request to change the role for this user");
        }

        roleChangeRequest.setStatus(RoleChangeStatus.PENDING_APPROVAL);
        roleChangeRequest.setInitiatedBy(authenticatedUser);
        roleChangeRequest.setProposedRole(dto.proposedRole());

        var  savedRC = roleChangeRequestRepository.save(roleChangeRequest);

        return  roleChangeRequestMapper.toRoleChangeRequestDto(savedRC);
    }


    public void approveRoleChange(User authenticatedUser, Long roleChangeRequestId){
        if(!authenticatedUser.getRole().equals(Role.TREASURER)){
            throw new BusinessRuleException("Only a treasurer can approve this role");
        }

        RoleChangeRequest roleChangeRequest = roleChangeRequestRepository.findById(roleChangeRequestId)
                .orElseThrow(() -> new ResourceNotFound("Role Change Request not found"));

        User targetedUser = roleChangeRequest.getTargetUser();

        if(!targetedUser.getOrganisation().equals(authenticatedUser.getOrganisation())){
            throw new BusinessRuleException("You cannot change the role for this user");
        }

        if(roleChangeRequest.getStatus().equals(RoleChangeStatus.APPROVED)){
            throw new BusinessRuleException("The role has already been approved");
        }

        roleChangeRequest.setStatus(RoleChangeStatus.APPROVED);

        targetedUser.setRole(Role.MEMBER);
        userRepository.save(targetedUser);

        Organisation organisation = targetedUser.getOrganisation();

        organisation.setNeedsChairMan(true);

        organisationRepository.save(organisation);

        roleChangeRequest.setApprovedBy(authenticatedUser);
        roleChangeRequest.setApprovedOn(LocalDateTime.now());

        roleChangeRequestRepository.save(roleChangeRequest);
    }


    public void  rejectRoleChange(User authenticatedUser, Long roleChangeRequestId, String rejectReason){
        if(!authenticatedUser.getRole().equals(Role.TREASURER)){
            throw new BusinessRuleException("Only a treasurer can approve this role");
        }

        RoleChangeRequest roleChangeRequest = roleChangeRequestRepository.findById(roleChangeRequestId)
                .orElseThrow(() -> new ResourceNotFound("Role Change Request not found"));

        User targetedUser = roleChangeRequest.getTargetUser();

        if(!targetedUser.getOrganisation().equals(authenticatedUser.getOrganisation())){
            throw new BusinessRuleException("You cannot change the role for this user");
        }

        if(roleChangeRequest.getStatus().equals(RoleChangeStatus.APPROVED)){
            throw new BusinessRuleException("The role has already been approved");
        }

        roleChangeRequest.setStatus(RoleChangeStatus.REJECTED);
        roleChangeRequest.setReason(rejectReason);

        roleChangeRequest.setRejectedBy(authenticatedUser);
        roleChangeRequest.setRejectedOn(LocalDateTime.now());

        roleChangeRequestRepository.save(roleChangeRequest);
    }
    public List<RoleCRequestResponseDto> RoleChangeRequestList(User authenticatedUser){

        Organisation  organisation = authenticatedUser.getOrganisation();

        return  roleChangeRequestRepository.findByOrganisation(organisation)
                .stream()
                .map(roleChangeRequestMapper :: toRoleChangeRequestDto)
                .toList();
    }

    public void cancelRoleChange(User authenticatedUser, Long roleChangeRequestId){
        if(!authenticatedUser.getRole().equals(Role.SECRETARY)){
            throw new BusinessRuleException("You cannot cancel the request");
        }

        RoleChangeRequest roleChangeRequest = roleChangeRequestRepository.findById(roleChangeRequestId)
                .orElseThrow(() -> new ResourceNotFound("Role Change Request not found"));

        if(!authenticatedUser.getOrganisation().equals(roleChangeRequest.getOrganisation())){
            throw new BusinessRuleException("You cannot cancel the request");
        }

        roleChangeRequest.setStatus(RoleChangeStatus.CANCELLED);

         roleChangeRequestRepository.save(roleChangeRequest);
    }

    public RoleCRequestResponseDto getRoleChangeRequest(User authenticatedUser,  Long roleChangeRequestId){
        RoleChangeRequest roleChangeRequest = roleChangeRequestRepository.findById(roleChangeRequestId)
                .orElseThrow(() -> new ResourceNotFound("Role Change Request not found"));

        if(!authenticatedUser.getOrganisation().equals(roleChangeRequest.getOrganisation())){
            throw new BusinessRuleException("You cannot view the requests");
        }

        return roleChangeRequestMapper.toRoleChangeRequestDto(roleChangeRequest);
    }


}
