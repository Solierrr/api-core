package com.solaria.persistence.openapi.professional;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.ProfessionalRegistrationRequestDTO;
import com.solaria.persistence.dto.response.professional.ProfessionalRegistrationResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Registros Profissionais", description = "Gerenciamento dos registros/matrículas profissionais dos técnicos (ex. CREA, CFT)")
public interface ProfessionalRegistrationOpenApi {

    @Operation(summary = "Cria um novo registro profissional")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro profissional criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Número de registro profissional já cadastrado")
    })
    ResponseEntity<ProfessionalRegistrationResponseDTO> save(ProfessionalRegistrationRequestDTO dto);

    @Operation(summary = "Atualiza um registro profissional existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro profissional atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro profissional não encontrado")
    })
    ResponseEntity<ProfessionalRegistrationResponseDTO> update(UUID id, ProfessionalRegistrationRequestDTO dto);

    @Operation(
        summary = "Exclui um registro profissional pelo identificador",
        description = "Não é permitida a exclusão se houver CertificationRecord vinculado ao registro profissional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro profissional excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro profissional não encontrado")
    })
    ResponseEntity<Void> deleteById(UUID id);

    @Operation(summary = "Busca um registro profissional pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro profissional encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro profissional não encontrado")
    })
    ResponseEntity<ProfessionalRegistrationResponseDTO> findById(UUID id);

    @Operation(summary = "Lista os registros profissionais de um técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de registros profissionais retornada com sucesso")
    })
    ResponseEntity<List<ProfessionalRegistrationResponseDTO>> findByTechnician(UUID technicianId);
}
