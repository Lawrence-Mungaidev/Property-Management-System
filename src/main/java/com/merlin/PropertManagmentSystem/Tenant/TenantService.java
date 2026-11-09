package com.merlin.PropertManagmentSystem.Tenant;

import com.merlin.PropertManagmentSystem.Exception.BusinessRuleException;
import com.merlin.PropertManagmentSystem.Exception.DuplicateRuleException;
import com.merlin.PropertManagmentSystem.Exception.ResourceNotFound;
import com.merlin.PropertManagmentSystem.Organisation.Organisation;
import com.merlin.PropertManagmentSystem.Property.Property;
import com.merlin.PropertManagmentSystem.Property.PropertyRepository;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsign;
import com.merlin.PropertManagmentSystem.PropertyAsign.PropertyAsignRepository;
import com.merlin.PropertManagmentSystem.User.Role;
import com.merlin.PropertManagmentSystem.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PropertyRepository propertyRepository;
    private final PropertyAsignRepository  propertyAsignRepository;

    public TenantResponseDto createTenant(TenantDto dto, User authenticatedUser){
        Tenant tenant = tenantMapper.toTenant(dto);

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        Property property = propertyRepository.findById(dto.propertyId()).
                orElseThrow(()-> new ResourceNotFound("The property isn't found"));


        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            boolean isOwner = property.getLandlord() != null
                    && property.getLandlord().getId().equals(authenticatedUser.getId());
            if(!isOwner){
                throw new BusinessRuleException("You don't own the property");
            }
            tenant.setLandlord(authenticatedUser);

            tenant.setProperty(property);
        } else if (isOfficer) {

            boolean isOwner = property.getOrganisation() != null
                    && property.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId());

           if(!isOwner){
               throw new BusinessRuleException("You don't own the property");
           }

            tenant.setOrganisation(authenticatedUser.getOrganisation());
            tenant.setProperty(property);

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property);

            if(!isAssigned){
                throw new BusinessRuleException("You are not assigned to this property");
            }

            if(property.getOrganisation() != null){
                tenant.setOrganisation(authenticatedUser.getOrganisation());
                tenant.setProperty(property);
            } else if (property.getLandlord() != null) {
                tenant.setLandlord(authenticatedUser.getLandlord());
                tenant.setProperty(property);
            }
        }else {
            throw new BusinessRuleException("You cant create a tenant");
        }

        String email = (dto.email() == null || dto.email().isBlank())
                ? null
                : dto.email().trim().toLowerCase();

        boolean exists = tenant.getLandlord() != null
                ? tenantRepository.existsByNationalIdAndLandlord(dto.nationalId(), tenant.getLandlord())
                : tenantRepository.existsByNationalIdAndOrganisation(dto.nationalId(), tenant.getOrganisation());

        if (exists) throw new DuplicateRuleException("This tenant already exists");

        if (email != null) {
            boolean emailExists = tenant.getLandlord() != null
                    ? tenantRepository.existsByEmailAndLandlord(email, tenant.getLandlord())
                    : tenantRepository.existsByEmailAndOrganisation(email, tenant.getOrganisation());

            if (emailExists) throw new DuplicateRuleException("This email already exists");
        }
        tenant.setEmail(email);

        var savedTenant = tenantRepository.save(tenant);

        return tenantMapper.toTenantResponseDto(savedTenant);
    }

    public TenantResponseDto updateTenant(TenantUpdateDto dto, User authenticatedUser, Long tenantId){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()-> new ResourceNotFound("The tenant isn't found"));

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(tenant.getLandlord() ==null || !tenant.getLandlord().getId().equals(authenticatedUser.getId())){
                throw new BusinessRuleException("You cannot update this tenant ");
            }
        } else if (isOfficer && authenticatedUser.isActive()) {

            if (tenant.getOrganisation() == null
                    || authenticatedUser.getOrganisation() == null
                    || !tenant.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())) {
                throw new ResourceNotFound("The tenant isn't found b");
            }

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

            Property property = tenant.getLandlord().getProperty();

            boolean isAssigned = propertyAsignRepository
                    .existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, property);

            if(!isAssigned){
                throw new BusinessRuleException("You are not assigned to this property");
            }

        }else {
            throw new BusinessRuleException("You are not assigned to this property");
        }


        if(dto.firstName() != null){
            tenant.setFirstName(dto.firstName());
        }

        if(dto.lastName() != null){
            tenant.setLastName(dto.lastName());
        }
        if(dto.email() != null){
            tenant.setEmail(dto.email());
        }
        if(dto.phoneNumber() != null){
            tenant.setPhoneNumber(dto.phoneNumber());
        }
        if(dto.nationalId() != null){
            tenant.setNationalId(dto.nationalId());
        }

        var savedTenant = tenantRepository.save(tenant);

        return tenantMapper.toTenantResponseDto(savedTenant);
    }

    public List<TenantResponseDto> getAllTenants(User authenticatedUser){
        List<Tenant> tenants = new ArrayList<>();

        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY);

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            tenants = tenantRepository.findAllByLandlord(authenticatedUser);
        } else if (isOfficer) {
            Organisation organisation = authenticatedUser.getOrganisation();

            tenants = tenantRepository.findAllByOrganisation(organisation);

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){

            List<Property> assignedProperties = propertyAsignRepository
                    .findByCareTakerAndUnAssignedAtIsNull(authenticatedUser);

            List<Tenant> assignedTenants;

            for(Property property : assignedProperties){
                assignedTenants = tenantRepository.findAllByProperty_Id(property.getId());
                tenants.addAll(assignedTenants);
            }

        }

        return tenants
                .stream()
                .map(tenantMapper :: toTenantResponseDto)
                .toList();
    }


    public TenantResponseDto getTenantById(Long tenantId, User authenticatedUser){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()-> new ResourceNotFound("The tenant isn't found"));


        boolean isOfficer = authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        boolean allowed = false;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(tenant.getLandlord().getId().equals(authenticatedUser.getId())){
                allowed = true;
            }


        } else if (isOfficer) {

            if(tenant.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())){
                allowed = true;
            }

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){
            if(!propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, tenant.getProperty())){
                throw new BusinessRuleException("You are not assigned to this property");
            }

            if(authenticatedUser.getLandlord() != null){
                if(tenant.getLandlord().getId().equals(authenticatedUser.getLandlord().getId())){
                    allowed = true;
                }
            }else if(authenticatedUser.getOrganisation() != null){
                if(tenant.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())){
                    allowed = true;
                }
            }


        }else {
            throw new BusinessRuleException("You cant view Tenants");
        }

        if(!allowed){
            throw new BusinessRuleException("You are not allowed to view Tenants");
        }


        return tenantMapper.toTenantResponseDto(tenant);
    }

    public void deleteTenant(Long tenantId, User authenticatedUser){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()-> new ResourceNotFound("The tenant isn't found"));

        if(!isAllowed(tenant, authenticatedUser)){
            throw new BusinessRuleException("You are not allowed to delete the tenant");
        }

        if(!tenant.getLeases().isEmpty()){
            throw new BusinessRuleException("Sorry you cant delete the tenant since they have active lease");
        }

        tenantRepository.delete(tenant);
    }

    private boolean isAllowed(Tenant tenant, User authenticatedUser){

        boolean isOfficer =  authenticatedUser.getRole().equals(Role.CHAIRMAN)
                || authenticatedUser.getRole().equals(Role.SECRETARY)
                || authenticatedUser.getRole().equals(Role.TREASURER);

        boolean allowed = false;

        if(authenticatedUser.getRole().equals(Role.LANDLORD)){
            if(tenant.getLandlord().getId().equals(authenticatedUser.getId())){
                allowed = true;
            }
        } else if (isOfficer && authenticatedUser.isActive()) {
            if(tenant.getOrganisation().getId().equals(authenticatedUser.getOrganisation().getId())){
                allowed = true;
            }

        }else if(authenticatedUser.getRole().equals(Role.CARETAKER)){
            boolean isAssigned = propertyAsignRepository.existsByCareTakerAndPropertyAndUnAssignedAtIsNull(authenticatedUser, tenant.getProperty());

            if(isAssigned){
                allowed = true;
            }
        }

        return allowed;
    }


}
