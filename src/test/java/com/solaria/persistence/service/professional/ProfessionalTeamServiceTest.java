package com.solaria.persistence.service.professional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.domain.entity.company.Company;
import com.solaria.persistence.domain.entity.identity.*;
import com.solaria.persistence.domain.entity.professional.*;
import com.solaria.persistence.domain.enums.company.CompanyStatus;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.exception.BusinessRuleException;
import com.solaria.persistence.repository.company.CompanyRepository;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

class ProfessionalTeamServiceTest {
    private final ProfessionalTeamRepository teams=mock(ProfessionalTeamRepository.class);
    private final ProfessionalTeamMemberRepository members=mock(ProfessionalTeamMemberRepository.class);
    private final TechnicianRepository technicians=mock(TechnicianRepository.class);
    private final TechnicianAffiliationRepository affiliations=mock(TechnicianAffiliationRepository.class);
    private final ProfessionalOnboardingRepository profiles=mock(ProfessionalOnboardingRepository.class);
    private final CompanyRepository companies=mock(CompanyRepository.class);
    private final RbacAuthorizationService rbac=mock(RbacAuthorizationService.class);
    private final ProfessionalTeamService service=new ProfessionalTeamService(teams,members,technicians,affiliations,profiles,companies,rbac);
    private final Company company=new Company();
    private final Technician technician=new Technician();
    @BeforeEach void setup() {
        company.setId(UUID.randomUUID()); company.setStatus(CompanyStatus.APPROVED);
        var person=new Person(); person.setName("Nome"); var user=new User(); user.setActive(true); person.setUser(user);
        technician.setId(UUID.randomUUID()); technician.setStatus(TechnicianStatus.APPROVED); technician.setPerson(person);
        when(companies.findById(company.getId())).thenReturn(Optional.of(company));
        when(technicians.findById(technician.getId())).thenReturn(Optional.of(technician));
        var link=new TechnicianAffiliation(); link.setCompany(company); link.setActive(true);
        when(affiliations.findByTechnicianId(technician.getId())).thenReturn(List.of(link));
        when(members.findByTeamId(any())).thenReturn(List.of());
    }
    private ProfessionalTeamService.Request request(List<UUID> ids) {
        return new ProfessionalTeamService.Request(company.getId(),"Equipe",technician.getId(),ids,true);
    }
    @Test void oneTechnicianCanParticipateInSeveralTeams() {
        service.save(null,request(List.of(technician.getId()))); service.save(null,request(List.of(technician.getId())));
        verify(teams,times(2)).saveAndFlush(any()); verify(members,times(2)).save(any());
    }
    @Test void duplicateMembersAndAbsentResponsibleAreRejected() {
        assertThrows(BusinessRuleException.class,()->service.save(null,request(List.of(technician.getId(),technician.getId()))));
        assertThrows(BusinessRuleException.class,()->service.save(null,request(List.of(UUID.randomUUID()))));
        verify(teams,never()).saveAndFlush(any());
    }
    @Test void revokedCompanyLinkOrSuspendedProfilePreventsMembership() {
        when(affiliations.findByTechnicianId(technician.getId())).thenReturn(List.of());
        assertThrows(BusinessRuleException.class,()->service.save(null,request(List.of(technician.getId()))));
        verify(members,never()).save(any());
    }
    @Test void anotherCompanyCannotManageTeam() {
        doThrow(new BusinessRuleException("denied")).when(rbac).requireOwnCompany(company.getId());
        assertThrows(BusinessRuleException.class,()->service.save(null,request(List.of(technician.getId()))));
        verifyNoInteractions(teams,members);
    }
}
