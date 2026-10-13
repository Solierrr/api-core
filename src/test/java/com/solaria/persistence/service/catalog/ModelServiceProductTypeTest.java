package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.solaria.persistence.domain.entity.catalog.Brand;
import com.solaria.persistence.domain.enums.catalog.InfrastructureType;
import com.solaria.persistence.domain.enums.catalog.PanelType;
import com.solaria.persistence.domain.enums.catalog.ProductType;
import com.solaria.persistence.dto.request.catalog.ModelRequestDTO;
import com.solaria.persistence.dto.response.catalog.ModelResponseDTO;
import com.solaria.persistence.exception.InvalidFieldException;
import com.solaria.persistence.repository.catalog.InventoryRepository;
import com.solaria.persistence.repository.catalog.ModelPhotoRepository;
import com.solaria.persistence.repository.catalog.ModelRepository;
import com.solaria.persistence.repository.catalog.OfferRepository;
import com.solaria.persistence.security.rbac.RbacAuthorizationService;

class ModelServiceProductTypeTest {
    private final ModelRepository models = mock(ModelRepository.class);
    private final RbacAuthorizationService rbac = mock(RbacAuthorizationService.class);
    private final BrandService brands = mock(BrandService.class);
    private final UUID brandId = UUID.randomUUID();
    private ModelService service;

    @BeforeEach
    void setup() {
        service = new ModelService(models, mock(InventoryRepository.class), mock(OfferRepository.class),
                mock(ModelPhotoRepository.class), rbac, brands);
        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("FixaSolar");
        when(rbac.getCurrentCompanyId()).thenReturn(UUID.randomUUID());
        when(brands.selectable(any(), any())).thenReturn(brand);
        when(models.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ModelRequestDTO request(ProductType productType) {
        ModelRequestDTO dto = new ModelRequestDTO();
        dto.setBrandId(brandId);
        dto.setModel("Modelo 1");
        dto.setProductType(productType);
        return dto;
    }

    private ModelRequestDTO panel() {
        ModelRequestDTO dto = request(ProductType.PANEL);
        dto.setType(PanelType.MONOCRYSTALLINE);
        dto.setPowerWp(new BigDecimal("550"));
        dto.setEfficiency(new BigDecimal("21.5"));
        dto.setWidth(new BigDecimal("1134"));
        dto.setLength(new BigDecimal("2278"));
        dto.setWeight(new BigDecimal("28"));
        return dto;
    }

    @Test
    void savesPanelWithPanelFieldsAndDefaultsProductType() {
        ModelResponseDTO saved = service.save(panel());
        assertEquals(ProductType.PANEL, saved.getProductType());
        assertEquals(new BigDecimal("550"), saved.getPowerWp());
        assertNull(saved.getInfrastructureType());
    }

    @Test
    void savesInfrastructureWithoutPanelFields() {
        ModelRequestDTO dto = request(ProductType.INFRASTRUCTURE);
        dto.setInfrastructureType(InfrastructureType.RAIL);
        dto.setMaterial("  Aluminio  ");
        ModelResponseDTO saved = service.save(dto);
        assertEquals(ProductType.INFRASTRUCTURE, saved.getProductType());
        assertEquals(InfrastructureType.RAIL, saved.getInfrastructureType());
        assertNull(saved.getPowerWp());
        assertNull(saved.getType());
        verify(models).save(argThat(model -> "Aluminio".equals(model.getMaterial())));
    }

    @Test
    void rejectsPanelMissingRequiredFieldsOrWithInfrastructureType() {
        ModelRequestDTO missing = panel();
        missing.setPowerWp(null);
        assertThrows(InvalidFieldException.class, () -> service.save(missing));
        ModelRequestDTO withInfrastructure = panel();
        withInfrastructure.setInfrastructureType(InfrastructureType.CLAMP);
        assertThrows(InvalidFieldException.class, () -> service.save(withInfrastructure));
        verify(models, never()).save(any());
    }

    @Test
    void rejectsInfrastructureWithoutTypeOrWithPanelFields() {
        assertThrows(InvalidFieldException.class, () -> service.save(request(ProductType.INFRASTRUCTURE)));
        ModelRequestDTO withPower = request(ProductType.INFRASTRUCTURE);
        withPower.setInfrastructureType(InfrastructureType.BRACKET);
        withPower.setPowerWp(new BigDecimal("400"));
        assertThrows(InvalidFieldException.class, () -> service.save(withPower));
        ModelRequestDTO negative = request(ProductType.INFRASTRUCTURE);
        negative.setInfrastructureType(InfrastructureType.HOOK);
        negative.setWeight(new BigDecimal("-1"));
        assertThrows(InvalidFieldException.class, () -> service.save(negative));
        verify(models, never()).save(any());
    }
}
