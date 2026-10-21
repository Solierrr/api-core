package com.solaria.persistence.service.catalog;

import com.solaria.persistence.domain.entity.catalog.*;
import com.solaria.persistence.domain.enums.catalog.*;
import com.solaria.persistence.dto.request.catalog.*;
import com.solaria.persistence.dto.response.catalog.*;
import com.solaria.persistence.exception.*;
import com.solaria.persistence.repository.catalog.*;
import com.solaria.persistence.security.CurrentUserService;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brands;
    private final BrandRequestRepository requests;
    private final ModelRepository models;
    private final RbacAuthorizationService rbac;
    private final CurrentUserService users;

    public static String normalize(String name) {
        return name.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private Brand brand(UUID id) {
        return brands.lockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca inexistente"));
    }

    private void owner(Brand b) {
        if (b.getOwnerCompanyId() == null)
            throw new UnauthorizedAccessException("Marca sem autoria empresarial");
        rbac.requireOwnCompany(b.getOwnerCompanyId());
    }

    private void unlocked(Brand b) {
        if (requests.existsByBrandIdAndStatus(b.getId(), BrandRequestStatus.PENDING))
            throw new BusinessRuleException(
                    "Cancele a solicitação pendente antes de alterar a marca");
    }

    private void validate(BrandRequestDTO d) {
        if (d.getName() == null || d.getName().isBlank() || d.getName().strip().length() > 255)
            throw new InvalidFieldException("Informe o nome da marca (até 255 caracteres)");
        for (String value : Arrays.asList(d.getLogoUrl(), d.getReferenceUrl())) {
            if (value == null || value.isBlank()) continue;
            try {
                URI uri = URI.create(value);
                if (!Set.of("https", "http").contains(uri.getScheme())
                        || uri.getHost() == null
                        || uri.getUserInfo() != null
                        || value.length() > 2048) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new InvalidFieldException("Informe um endereço http ou https válido");
            }
        }
    }

    private void apply(Brand b, BrandRequestDTO d) {
        b.setName(d.getName().strip());
        b.setNormalizedName(normalize(d.getName()));
        b.setLogoUrl(d.getLogoUrl());
        b.setReferenceUrl(d.getReferenceUrl());
        b.setUpdatedAt(Instant.now());
    }

    private void duplicate(Brand b, String name) {
        if (brands.existsByOwnerCompanyIdAndNormalizedNameAndArchivedFalseAndIdNot(
                b.getOwnerCompanyId(),
                normalize(name),
                b.getId() == null ? new UUID(0, 0) : b.getId()))
            throw new DuplicateResourceException("Sua empresa já possui uma marca com esse nome");
    }

    @Transactional(readOnly = true)
    public List<BrandResponseDTO> list() {
        Set<UUID> pending = new HashSet<>(requests.findPendingBrandIds());
        return brands
                .findByVisibilityAndArchivedFalseOrOwnerCompanyId(
                        BrandVisibility.PUBLIC, rbac.getCurrentCompanyId())
                .stream()
                .map(b -> BrandResponseDTO.from(b, pending.contains(b.getId())))
                .toList();
    }

    @Transactional
    public BrandResponseDTO create(BrandRequestDTO d) {
        validate(d);
        Brand b = new Brand();
        b.setOwnerCompanyId(rbac.getCurrentCompanyId());
        duplicate(b, d.getName());
        apply(b, d);
        return BrandResponseDTO.from(brands.saveAndFlush(b));
    }

    @Transactional
    public BrandResponseDTO update(UUID id, BrandRequestDTO d) {
        Brand b = brand(id);
        owner(b);
        unlocked(b);
        if (b.getVisibility() == BrandVisibility.PUBLIC || b.isArchived())
            throw new BusinessRuleException(
                    "Marca pública requer solicitação de correção; marca arquivada não pode ser"
                        + " editada");
        validate(d);
        duplicate(b, d.getName());
        apply(b, d);
        return BrandResponseDTO.from(brands.saveAndFlush(b));
    }

    @Transactional
    public void archive(UUID id) {
        Brand b = brand(id);
        owner(b);
        unlocked(b);
        if (b.getVisibility() == BrandVisibility.PUBLIC)
            throw new BusinessRuleException("Marcas públicas não podem ser arquivadas");
        b.setArchived(true);
        b.setUpdatedAt(Instant.now());
        brands.save(b);
    }

    @Transactional
    public BrandRequestResponseDTO submit(UUID id, BrandRequestDTO d) {
        Brand b = brand(id);
        if (b.getVisibility() == BrandVisibility.PRIVATE) owner(b);
        else rbac.getCurrentCompanyId();
        unlocked(b);
        if (b.isArchived())
            throw new BusinessRuleException("Marca arquivada não aceita solicitações");
        validate(d);
        BrandRequest r = new BrandRequest();
        r.setBrand(b);
        r.setRequesterCompanyId(rbac.getCurrentCompanyId());
        r.setRequesterUserId(users.getCurrentUser().getId());
        r.setKind(
                b.getVisibility() == BrandVisibility.PUBLIC
                        ? BrandRequestKind.CORRECTION
                        : BrandRequestKind.PUBLICATION);
        r.setProposedName(d.getName().strip());
        r.setProposedLogoUrl(d.getLogoUrl());
        r.setProposedReferenceUrl(d.getReferenceUrl());
        return BrandRequestResponseDTO.from(requests.saveAndFlush(r));
    }

    @Transactional(readOnly = true)
    public List<BrandRequestResponseDTO> listRequests(boolean admin) {
        if (admin) rbac.requireEndpointAccess("GET /api/brand-review");
        return (admin
                        ? requests.findAllByOrderByCreatedAtDesc()
                        : requests.findByRequesterCompanyIdOrderByCreatedAtDesc(
                                rbac.getCurrentCompanyId()))
                .stream().map(BrandRequestResponseDTO::from).toList();
    }

    private BrandRequest pending(UUID id, Long revision) {
        BrandRequest initial =
                requests.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Solicitação inexistente"));
        brand(initial.getBrand().getId());
        BrandRequest r =
                requests.lockById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Solicitação inexistente"));
        if (r.getStatus() != BrandRequestStatus.PENDING
                || revision == null
                || r.getRevision() != revision)
            throw new BusinessRuleException(
                    "Solicitação já decidida ou versão desatualizada; atualize os dados");
        return r;
    }

    @Transactional
    public BrandRequestResponseDTO cancel(UUID id, BrandDecisionDTO d) {
        BrandRequest r = pending(id, d.revision());
        if (r.getRequesterCompanyId() == null)
            throw new UnauthorizedAccessException(
                    "Solicitação legada exige análise administrativa");
        rbac.requireOwnCompany(r.getRequesterCompanyId());
        r.setStatus(BrandRequestStatus.CANCELLED);
        r.setDecidedAt(Instant.now());
        r.setDecidedBy(users.getCurrentUser().getId());
        return BrandRequestResponseDTO.from(requests.saveAndFlush(r));
    }

    @Transactional
    public BrandRequestResponseDTO decide(UUID id, BrandDecisionDTO d, boolean approved) {
        rbac.requireEndpointAccess("POST /api/brand-review/{id}/decision");
        BrandRequest r = pending(id, d.revision());
        Brand b = r.getBrand();
        if (!approved && (d.reason() == null || d.reason().isBlank()))
            throw new InvalidFieldException("Informe o motivo da reprovação");
        if (approved) {
            Brand canonical =
                    brands.findByNormalizedNameAndVisibility(
                                    normalize(r.getProposedName()), BrandVisibility.PUBLIC)
                            .orElse(null);
            if (canonical != null && !canonical.getId().equals(b.getId())) {
                if (r.getKind() == BrandRequestKind.CORRECTION)
                    throw new BusinessRuleException("Já existe uma marca pública com esse nome");
                models.reassignBrand(b.getId(), canonical.getId());
                b.setArchived(true);
                r.setResultBrandId(canonical.getId());
            } else {
                BrandRequestDTO proposal = new BrandRequestDTO();
                proposal.setName(r.getProposedName());
                proposal.setLogoUrl(r.getProposedLogoUrl());
                proposal.setReferenceUrl(r.getProposedReferenceUrl());
                validate(proposal);
                apply(b, proposal);
                b.setVisibility(BrandVisibility.PUBLIC);
                b.setLegacy(false);
                r.setResultBrandId(b.getId());
            }
            brands.saveAndFlush(b);
        }
        r.setStatus(approved ? BrandRequestStatus.APPROVED : BrandRequestStatus.REJECTED);
        r.setDecisionReason(d.reason() == null ? null : d.reason().strip());
        r.setDecidedBy(users.getCurrentUser().getId());
        r.setDecidedAt(Instant.now());
        return BrandRequestResponseDTO.from(requests.saveAndFlush(r));
    }

    @Transactional
    public Brand selectable(UUID id, UUID companyId) {
        if (id == null) throw new InvalidFieldException("Escolha uma marca cadastrada");
        rbac.requireOwnCompany(companyId);
        Brand b =
                brands.lockById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Marca inexistente"));
        if (!canSelect(b, companyId))
            throw new BusinessRuleException("Marca indisponível para novos vínculos nesta empresa");
        return b;
    }

    public boolean canSelect(Brand b, UUID companyId) {
        return !b.isArchived()
                && !b.isLegacy()
                && (b.getVisibility() == BrandVisibility.PUBLIC
                        || Objects.equals(b.getOwnerCompanyId(), companyId));
    }
}
