package com.jovine360.talenthub.organization.application.service;

import com.jovine360.talenthub.organization.api.CreateOrganizationRequest;
import com.jovine360.talenthub.organization.api.OrganizationApi;
import com.jovine360.talenthub.organization.api.OrganizationDto;
import com.jovine360.talenthub.organization.domain.model.Organization;
import com.jovine360.talenthub.organization.domain.repository.OrganizationRepository;
import com.jovine360.talenthub.organization.domain.service.OrgCodeGeneratorService;
import com.jovine360.talenthub.shared.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrganizationService implements OrganizationApi {

    private final OrganizationRepository orgRepository;
    private final OrgCodeGeneratorService codeGenerator;

    public OrganizationService(OrganizationRepository orgRepository,
                                OrgCodeGeneratorService codeGenerator) {
        this.orgRepository = orgRepository;
        this.codeGenerator = codeGenerator;
    }

    public OrganizationDto create(String adminUserId, CreateOrganizationRequest req) {
        Organization org = new Organization();
        org.setName(req.name());
        org.setOrgType(req.orgType());
        org.setCountryCode(req.countryCode());
        org.setCity(req.city());
        org.setAdminUserId(adminUserId);
        org.setCode(codeGenerator.generate(req.name()));
        return toDto(orgRepository.save(org));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationDto> findById(String orgId) {
        return orgRepository.findById(orgId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationDto> findByCode(String code) {
        return orgRepository.findByCode(code).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean orgExists(String orgId) {
        return orgRepository.existsById(orgId);
    }

    @Transactional(readOnly = true)
    public OrganizationDto getById(String id) {
        return orgRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Organization", id));
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getMyOrganizations(String adminUserId) {
        return orgRepository.findByAdminUserId(adminUserId).stream().map(this::toDto).toList();
    }

    private OrganizationDto toDto(Organization o) {
        return new OrganizationDto(o.getId(), o.getCode(), o.getName(),
                o.getOrgType() != null ? o.getOrgType().name() : null,
                o.getCountryCode(), o.getCity(), o.getAdminUserId(),
                o.getLogoUrl(), o.getStatus() != null ? o.getStatus().name() : null);
    }
}
