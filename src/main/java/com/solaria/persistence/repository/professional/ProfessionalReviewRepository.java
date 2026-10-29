package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.ProfessionalReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProfessionalReviewRepository extends JpaRepository<ProfessionalReview, UUID> {

    boolean existsByReviewer_IdAndProfessional_IdAndService_Id(UUID reviewerId, UUID professionalId, UUID serviceId);

    List<ProfessionalReview> findByProfessional_IdAndActiveTrue(UUID professionalId);

    @Query("select r.professional.id as technicianId, avg(r.rating) as averageRating, count(r) as reviewCount "
            + "from ProfessionalReview r where r.active = true and r.professional.id in :technicianIds "
            + "group by r.professional.id")
    List<ReviewSummary> summarizeActive(Collection<UUID> technicianIds);

    interface ReviewSummary {
        UUID getTechnicianId();

        Double getAverageRating();

        long getReviewCount();
    }

}
