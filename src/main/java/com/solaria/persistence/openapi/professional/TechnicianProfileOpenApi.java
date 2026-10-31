package com.solaria.persistence.openapi.professional;

import org.springframework.http.ResponseEntity;

import com.solaria.persistence.dto.request.professional.TechnicianProfileRequest;
import com.solaria.persistence.dto.response.professional.TechnicianProfileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Perfil do técnico", description = "Informações complementares que o próprio técnico preenche depois do cadastro")
public interface TechnicianProfileOpenApi {

    @Operation(
        summary = "Consulta o perfil do técnico autenticado",
        description = "Sem nada preenchido, devolve o perfil vazio com `missing` e `progress`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "A conta não tem perfil de técnico")
    })
    ResponseEntity<TechnicianProfileResponse> get();

    @Operation(
        summary = "Salva o rascunho do perfil",
        description = "Substitui todos os campos pelo que foi enviado. Pode salvar sem endereço: o perfil só fica "
                + "concluído pela operação de conclusão."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho salvo com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "A conta não tem perfil de técnico")
    })
    ResponseEntity<TechnicianProfileResponse> save(TechnicianProfileRequest request);

    @Operation(
        summary = "Conclui o perfil",
        description = "Exige endereço completo e ao menos uma especialidade; sem isso, responde com os itens que faltam."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil concluído"),
            @ApiResponse(responseCode = "400", description = "Faltam informações obrigatórias"),
            @ApiResponse(responseCode = "404", description = "A conta não tem perfil de técnico")
    })
    ResponseEntity<TechnicianProfileResponse> complete();
}
