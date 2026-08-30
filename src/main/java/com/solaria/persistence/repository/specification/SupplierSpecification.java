package com.solaria.persistence.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.solaria.persistence.domain.entity.Address;
import com.solaria.persistence.domain.entity.Company;
import com.solaria.persistence.domain.entity.Supplier;
import com.solaria.persistence.dto.request.SupplierSearchFilterDTO;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class SupplierSpecification {

    private SupplierSpecification() {
    }

    public static Specification<Supplier> withFilters(SupplierSearchFilterDTO filters) {
        return (root, ignoredQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Supplier, Company> company = root.join("company");

            if (hasText(filters.getQuery())) {
                String searchPattern = createLikePattern(filters.getQuery());

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(company.<String>get("tradeName")),
                                searchPattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(company.<String>get("corporateName")),
                                searchPattern,
                                '\\'),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.<String>get("businessType")),
                                searchPattern,
                                '\\')));
            }

            if (hasText(filters.getBusinessType())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("businessType")),
                        normalize(filters.getBusinessType())));
            }

            boolean hasLocationFilter = hasText(filters.getState())
                    || hasText(filters.getCity())
                    || hasText(filters.getNeighborhood());

            if (hasLocationFilter) {
                Join<Company, Address> address = company.join("address", JoinType.LEFT);

                if (hasText(filters.getState())) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(
                                    address.<String>get("state")),
                            normalize(filters.getState())));
                }

                if (hasText(filters.getCity())) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(
                                    address.<String>get("city")),
                            normalize(filters.getCity())));
                }

                if (hasText(filters.getNeighborhood())) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(
                                    address.<String>get("neighborhood")),
                            normalize(filters.getNeighborhood())));
                }
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String createLikePattern(String value) {
        String escapedValue = normalize(value)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapedValue + "%";
    }
}
