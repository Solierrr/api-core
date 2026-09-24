package com.solaria.persistence.repository.professional;

import com.solaria.persistence.domain.entity.professional.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    List<Shift> findByTechnicianId(UUID technicianId);
}
