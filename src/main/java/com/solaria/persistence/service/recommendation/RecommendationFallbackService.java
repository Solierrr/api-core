package com.solaria.persistence.service.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solaria.persistence.domain.entity.catalog.Model;
import com.solaria.persistence.domain.entity.catalog.Offer;
import com.solaria.persistence.domain.entity.catalog.Supplier;
import com.solaria.persistence.domain.entity.professional.Technician;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.integration.recommendation.RecommendationClientProperties;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.repository.catalog.OfferRepository.SupplierOfferCount;
import com.solaria.persistence.repository.catalog.SupplierRepository;
import com.solaria.persistence.repository.professional.ProfessionalRegistrationRepository;
import com.solaria.persistence.repository.professional.ProfessionalReviewRepository;
import com.solaria.persistence.repository.professional.ProfessionalReviewRepository.ReviewSummary;
import com.solaria.persistence.repository.professional.TechnicianRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Ranking simples no Postgres, no mesmo formato do api-recommendation, para quando não há grafo; só dados públicos do catálogo
@Service
public class RecommendationFallbackService {

    private static final int MAX_LIMIT = 50;
    private static final String OFFERS_WARNING = "O grafo está indisponível; o resultado é um ranking simples calculado no banco, "
            + "das ofertas de melhor custo por Wp, sem aplicar a estratégia pedida.";
    private static final String SUPPLIERS_WARNING = "O grafo está indisponível; o resultado é um ranking simples calculado no banco, "
            + "dos fornecedores com mais ofertas ativas, sem aplicar a estratégia pedida.";
    private static final String PROFESSIONALS_WARNING = "O grafo está indisponível; o resultado é um ranking simples calculado no banco, "
            + "pela avaliação dos clientes; serviços e certificações não foram contabilizados.";

    private final OfferRepository offers;
    private final SupplierRepository suppliers;
    private final TechnicianRepository technicians;
    private final ProfessionalRegistrationRepository registrations;
    private final ProfessionalReviewRepository reviews;
    private final RecommendationClientProperties properties;
    private final ObjectMapper json;
    private final Clock clock;

    @Autowired
    public RecommendationFallbackService(OfferRepository offers, SupplierRepository suppliers,
            TechnicianRepository technicians, ProfessionalRegistrationRepository registrations,
            ProfessionalReviewRepository reviews, RecommendationClientProperties properties, ObjectMapper json) {
        this(offers, suppliers, technicians, registrations, reviews, properties, json, Clock.systemUTC());
    }

    RecommendationFallbackService(OfferRepository offers, SupplierRepository suppliers,
            TechnicianRepository technicians, ProfessionalRegistrationRepository registrations,
            ProfessionalReviewRepository reviews, RecommendationClientProperties properties, ObjectMapper json,
            Clock clock) {
        this.offers = offers;
        this.suppliers = suppliers;
        this.technicians = technicians;
        this.registrations = registrations;
        this.reviews = reviews;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public JsonNode offers(String strategy, UUID companyId) {
        List<Offer> ranked = offers.findFeedOffers(OffsetDateTime.now(clock), PageRequest.of(0, limit()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (Offer offer : ranked) {
            Model model = offer.getModel();
            BigDecimal pricePerWp = offer.getUnitPrice().divide(model.getPowerWp(), 4, RoundingMode.HALF_UP);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", items.size() + 1);
            item.put("offer_id", offer.getId());
            item.put("supplier_id", offer.getSupplier().getId());
            item.put("supplier_trade_name", offer.getSupplier().getCompany().getTradeName());
            item.put("brand", model.getBrand());
            item.put("model", model.getModel());
            item.put("power_wp", model.getPowerWp());
            item.put("efficiency", model.getEfficiency());
            item.put("unit_price", offer.getUnitPrice());
            item.put("effective_availability", offer.getAvailability());
            item.put("model_id", model.getId());
            item.put("dimension", model.getWidth() == null || model.getLength() == null ? null
                    : model.getWidth().multiply(model.getLength()));
            item.put("weight", model.getWeight());
            item.put("accepted_proposal_quantity", 0);
            item.put("distance_km", null);
            item.put("ranking_value", pricePerWp);
            item.put("ranking_unit", "BRL_per_Wp");
            item.put("reasons", List.of("Entre as ofertas de melhor custo por Wp"));
            items.add(item);
        }
        return response("offers", strategy, companyId, items, OFFERS_WARNING);
    }

    @Transactional(readOnly = true)
    public JsonNode suppliers(String strategy, UUID companyId) {
        List<SupplierOfferCount> counts = offers.countFeedOffersBySupplier(OffsetDateTime.now(clock),
                PageRequest.of(0, limit()));
        Map<UUID, Supplier> byId = suppliers
                .findByIdIn(counts.stream().map(SupplierOfferCount::getSupplierId).toList()).stream()
                .collect(Collectors.toMap(Supplier::getId, Function.identity()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOfferCount count : counts) {
            Supplier supplier = byId.get(count.getSupplierId());
            if (supplier == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", items.size() + 1);
            item.put("supplier_id", supplier.getId());
            item.put("company_id", supplier.getCompany().getId());
            item.put("trade_name", supplier.getCompany().getTradeName());
            item.put("business_type", supplier.getBusinessType());
            item.put("offer_count", count.getOfferCount());
            item.put("accepted_proposal_quantity", 0);
            item.put("distance_km", null);
            item.put("ranking_value", count.getOfferCount());
            item.put("ranking_unit", "offers");
            item.put("reasons", List.of("Entre os fornecedores com mais ofertas ativas"));
            items.add(item);
        }
        return response("suppliers", strategy, companyId, items, SUPPLIERS_WARNING);
    }

    @Transactional(readOnly = true)
    public JsonNode professionals(String strategy, UUID companyId, UUID professionId) {
        List<Technician> approved = technicians.findByStatusAndPersonUserActiveTrue(TechnicianStatus.APPROVED);
        OffsetDateTime now = OffsetDateTime.now(clock);
        Map<UUID, List<String>> professions = approved.isEmpty() ? Map.of()
                : registrations.findByTechnicianIdIn(approved.stream().map(Technician::getId).toList()).stream()
                        .filter(registration -> registration.getProfession() != null
                                && registration.getProfession().getName() != null
                                && (registration.getExpirationDate() == null
                                        || !registration.getExpirationDate().isBefore(now))
                                && (professionId == null
                                        || professionId.equals(registration.getProfession().getId())))
                        .collect(Collectors.groupingBy(registration -> registration.getTechnician().getId(),
                                Collectors.mapping(registration -> registration.getProfession().getName(),
                                        Collectors.collectingAndThen(Collectors.toCollection(TreeSet::new),
                                                List::copyOf))));
        Map<UUID, ReviewSummary> summaries = professions.isEmpty() ? Map.of()
                : reviews.summarizeActive(professions.keySet()).stream()
                        .collect(Collectors.toMap(ReviewSummary::getTechnicianId, Function.identity()));
        Comparator<Technician> order = Comparator
                .comparingDouble((Technician technician) -> rating(summaries.get(technician.getId()))).reversed()
                .thenComparing(Comparator
                        .comparingLong((Technician technician) -> count(summaries.get(technician.getId())))
                        .reversed())
                .thenComparing(technician -> technician.getPerson().getName(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Technician::getSlug);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Technician technician : approved.stream().filter(item -> professions.containsKey(item.getId()))
                .sorted(order).limit(limit()).toList()) {
            ReviewSummary summary = summaries.get(technician.getId());
            double rating = BigDecimal.valueOf(rating(summary)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", items.size() + 1);
            item.put("technician_id", technician.getId());
            item.put("name", technician.getPerson().getName());
            item.put("professions", professions.get(technician.getId()));
            item.put("average_rating_global", rating);
            item.put("review_count_global", count(summary));
            item.put("completed_service_count_global", 0);
            item.put("certification_names", List.of());
            item.put("assigned_service_count_global", 0);
            item.put("canceled_service_count_global", 0);
            item.put("valid_certification_count", 0);
            item.put("ranking_value", rating);
            item.put("ranking_unit", "rating");
            item.put("reasons", List.of("Entre os profissionais mais bem avaliados"));
            items.add(item);
        }
        return response("professionals", strategy, companyId, items, PROFESSIONALS_WARNING);
    }

    private JsonNode response(String feed, String strategy, UUID companyId, List<Map<String, Object>> items,
            String warning) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("feed", feed);
        context.put("strategy", strategy);
        context.put("company_id", companyId);
        context.put("generated_at", clock.instant().toString());
        context.put("sync_version", null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("source", "fallback");
        body.put("context", context);
        body.put("items", items);
        body.put("warnings", List.of(warning));
        return json.valueToTree(body);
    }

    private int limit() {
        return Math.max(1, Math.min(MAX_LIMIT, properties.resultLimit()));
    }

    private static double rating(ReviewSummary summary) {
        return summary == null || summary.getAverageRating() == null ? 0.0 : summary.getAverageRating();
    }

    private static long count(ReviewSummary summary) {
        return summary == null ? 0L : summary.getReviewCount();
    }
}
