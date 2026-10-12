package com.solaria.persistence.repository.identity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.enums.professional.TechnicalAffiliationType;
import com.solaria.persistence.domain.enums.shared.PhotoType;

/** Projeções de leitura evitam carregar dados privados ou uma consulta por vínculo. */
public interface SelfProfileRepository extends Repository<User, UUID> {

    @Query("""
            select p.name as name, contact.phone as phone, technician.id as technicianId
            from Person p left join p.contact contact
            left join Technician technician on technician.person = p
            where p.user.id = :userId
            """)
    Optional<PersonContext> findPerson(@Param("userId") UUID userId);

    @Query("""
            select distinct profession.id as id, profession.name as name
            from ProfessionalRegistration registration join registration.profession profession
            where registration.technician.id = :technicianId
            order by profession.name, profession.id
            """)
    List<ProfessionSummary> findProfessions(@Param("technicianId") UUID technicianId);

    @Query("""
            select affiliation.affiliationType as type, company.id as companyId,
                   company.tradeName as companyName
            from TechnicianAffiliation affiliation left join affiliation.company company
            where affiliation.technician.id = :technicianId and affiliation.active = true
            order by affiliation.id
            """)
    List<ActiveAffiliation> findActiveAffiliations(@Param("technicianId") UUID technicianId);

    @Query("""
            select company.id as companyId, company.tradeName as companyName, position.name as position
            from UserCompany membership join membership.company company join membership.position position
            where membership.user.id = :userId
            order by company.tradeName, company.id, position.name, membership.id
            """)
    List<CompanyMembership> findCompanyMemberships(@Param("userId") UUID userId);

    @Query("""
            select photo.type as type, photo.url as url
            from UserPhoto photo where photo.user.id = :userId and photo.type in :types
            """)
    List<PhotoSummary> findPhotos(@Param("userId") UUID userId, @Param("types") List<PhotoType> types);

    interface PersonContext {
        String getName();
        String getPhone();
        UUID getTechnicianId();
    }

    interface PhotoSummary {
        PhotoType getType();
        String getUrl();
    }

    interface ProfessionSummary {
        UUID getId();
        String getName();
    }

    interface ActiveAffiliation {
        TechnicalAffiliationType getType();
        UUID getCompanyId();
        String getCompanyName();
    }

    interface CompanyMembership {
        UUID getCompanyId();
        String getCompanyName();
        String getPosition();
    }
}
