package com.solaria.persistence.service.unit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.solaria.persistence.config.CloudinaryProperties;
import com.solaria.persistence.domain.entity.unit.EnergyBill;
import com.solaria.persistence.domain.entity.unit.LocalUnit;
import com.solaria.persistence.dto.request.unit.EnergyBillRequestDTO;
import com.solaria.persistence.dto.response.unit.EnergyBillResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.exception.ResourceNotFoundException;
import com.solaria.persistence.repository.unit.EnergyBillRepository;
import com.solaria.persistence.repository.unit.LocalUnitRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;
import com.solaria.persistence.service.shared.CloudinaryService.UploadResult;

import com.solaria.persistence.service.shared.CloudinaryService;

@Service
public class EnergyBillService {

    private static final String LOCAL_UNIT_NOT_FOUND = "Unidade Local não encontrada com ID: ";
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static final Set<String> ALLOWED_PHOTO_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_PHOTO_BYTES = 10L * 1024 * 1024;

    private final EnergyBillRepository energyBillRepository;
    private final LocalUnitRepository localUnitRepository;
    private final CloudinaryService cloudinaryService;
    private final CloudinaryProperties cloudinaryProperties;
    private final RbacAuthorizationService rbac;

    public EnergyBillService(EnergyBillRepository energyBillRepository,
            LocalUnitRepository localUnitRepository,
            CloudinaryService cloudinaryService,
            CloudinaryProperties cloudinaryProperties,
            RbacAuthorizationService rbac) {
        this.energyBillRepository = energyBillRepository;
        this.localUnitRepository = localUnitRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinaryProperties = cloudinaryProperties;
        this.rbac = rbac;
    }

    @Transactional
    public EnergyBillResponseDTO save(EnergyBillRequestDTO dto) {
        if (dto.getConsumption() == null || dto.getConsumption().compareTo(ZERO) <= 0) {
            throw new InvalidFieldException("Consumo inválido: " + dto.getConsumption());
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(ZERO) < 0) {
            throw new InvalidFieldException("Preço inválido: " + dto.getPrice());
        }

        LocalUnit localUnit = localUnitRepository.findForManagementById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        LOCAL_UNIT_NOT_FOUND + dto.getLocalUnitId()));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());

        EnergyBill energyBill = new EnergyBill();
        energyBill.setLocalUnit(localUnit);
        energyBill.setConsumption(dto.getConsumption());
        energyBill.setPrice(dto.getPrice());

        return toResponse(energyBillRepository.save(energyBill));
    }

    @Transactional
    public EnergyBillResponseDTO update(UUID id, EnergyBillRequestDTO dto) {
        if (dto.getConsumption() == null || dto.getConsumption().compareTo(ZERO) <= 0) {
            throw new InvalidFieldException("Consumo inválido: " + dto.getConsumption());
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(ZERO) < 0) {
            throw new InvalidFieldException("Preço inválido: " + dto.getPrice());
        }

        EnergyBill energyBill = energyBillRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta de Energia não encontrada com ID: " + id));
        rbac.requireOwnCompany(energyBill.getLocalUnit().getRequester().getCompany().getId());

        LocalUnit localUnit = localUnitRepository.findForManagementById(dto.getLocalUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        LOCAL_UNIT_NOT_FOUND + dto.getLocalUnitId()));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());

        energyBill.setLocalUnit(localUnit);
        energyBill.setConsumption(dto.getConsumption());
        energyBill.setPrice(dto.getPrice());

        return toResponse(energyBillRepository.save(energyBill));
    }

    @Transactional(readOnly = true)
    public EnergyBillResponseDTO findById(UUID id) {
        EnergyBill energyBill = energyBillRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta de Energia não encontrada com ID: " + id));
        rbac.requireOwnCompany(energyBill.getLocalUnit().getRequester().getCompany().getId());
        return toResponse(energyBill);
    }

    @Transactional(readOnly = true)
    public EnergyBillResponseDTO findById(UUID id, UUID companyId) {
        EnergyBill energyBill = energyBillRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado com ID: " + id));
        rbac.requireOwnCompany(energyBill.getLocalUnit().getRequester().getCompany().getId());
        rbac.requireOwnCompany(companyId);
        return toResponse(energyBill);
    }

    @Transactional(readOnly = true)
    public List<EnergyBillResponseDTO> findByLocalUnit(UUID localUnitId) {
        LocalUnit localUnit = localUnitRepository.findForManagementById(localUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(LOCAL_UNIT_NOT_FOUND + localUnitId));
        rbac.requireOwnCompany(localUnit.getRequester().getCompany().getId());
        return energyBillRepository.findByLocalUnitId(localUnitId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EnergyBillResponseDTO attachPhoto(UUID id, MultipartFile file) {
        EnergyBill energyBill = energyBillRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta de Energia não encontrada com ID: " + id));

        requireOwnCompany(energyBill);

        String folder = folder(id);
        String publicId = folder + "/photo";
        UploadResult result = cloudinaryService.upload(file, folder, publicId, true,
                ALLOWED_PHOTO_CONTENT_TYPES, MAX_PHOTO_BYTES);

        energyBill.setPhotoUrl(result.url());
        energyBill.setPhotoPublicId(result.publicId());

        // se por algum motivo, não conseguir salvar no BD, remove o asset do cloudnary
        try {
            return toResponse(energyBillRepository.save(energyBill));
        } catch (RuntimeException e) {
            cloudinaryService.delete(result.publicId());
            throw e;
        }
    }

    @Transactional
    public void removePhoto(UUID id) {
        EnergyBill energyBill = energyBillRepository.findForManagementById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta de Energia não encontrada com ID: " + id));

        requireOwnCompany(energyBill);

        if (energyBill.getPhotoPublicId() == null) {
            throw new ResourceNotFoundException("Conta de Energia não possui foto: " + id);
        }

        cloudinaryService.delete(energyBill.getPhotoPublicId());
        energyBill.setPhotoUrl(null);
        energyBill.setPhotoPublicId(null);
        energyBillRepository.save(energyBill);
    }

    private String folder(UUID companyId) {
        return cloudinaryProperties.getFolderPrefix() + "/empresas/" + companyId;
    }

    private void requireOwnCompany(EnergyBill energyBill) {
        rbac.requireOwnCompany(energyBill.getLocalUnit().getRequester().getCompany().getId());
    }

    // Monta o DTO à mão: converter a entidade com o ObjectMapper segue as relações bidirecionais
    // e estoura a pilha com "Infinite recursion" (GET respondia 500).
    private EnergyBillResponseDTO toResponse(EnergyBill energyBill) {
        EnergyBillResponseDTO response = new EnergyBillResponseDTO();
        response.setId(energyBill.getId());
        response.setLocalUnitId(energyBill.getLocalUnit().getId());
        response.setConsumption(energyBill.getConsumption());
        response.setPrice(energyBill.getPrice());
        response.setPhotoUrl(energyBill.getPhotoUrl());
        return response;
    }
}
