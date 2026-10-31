package com.solaria.persistence.openapi.execution;

import com.solaria.persistence.dto.request.execution.*;
import com.solaria.persistence.dto.response.execution.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@Tag(
        name = "Projetos",
        description =
                "Projetos privados empresariais e profissionais; acesso por propriedade e"
                    + " participação")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectOpenApi {
    @Operation(summary = "Unidades e equipes do próprio contexto empresarial")
    Map<String, Object> options(UUID companyId);

    @Operation(summary = "Lista projetos acessíveis no contexto")
    List<ProjectResponse> list(UUID companyId);

    @Operation(summary = "Cria projeto empresarial ou profissional")
    ProjectResponse create(ProjectRequest dto);

    @Operation(summary = "Consulta um projeto autorizado")
    ProjectResponse detail(UUID id);

    @Operation(summary = "Edita dados; propriedade é imutável")
    ProjectResponse update(UUID id, ProjectRequest dto);

    @Operation(summary = "Altera situação ou reabre o projeto")
    ProjectResponse status(UUID id, ProjectDecisionRequest dto);

    @Operation(summary = "Arquiva sem excluir o histórico")
    void archive(UUID id);

    @Operation(summary = "Lista envolvidos do projeto")
    List<ProjectMemberResponse> members(UUID id);

    @Operation(summary = "Edita papel e função de um participante")
    ProjectMemberResponse member(UUID id, UUID userId, ProjectMemberRequest dto);

    @Operation(summary = "Remove participante e revoga acesso ao projeto")
    void removeMember(UUID id, UUID userId);

    @Operation(summary = "Convida por e-mail ou snapshot de equipe")
    List<ProjectInvitationResponse> invite(UUID id, ProjectInvitationRequest dto);

    @Operation(summary = "Lista convites recebidos e válidos")
    List<ProjectInvitationResponse> myInvites();

    @Operation(summary = "Lista convites enviados; apenas gestores")
    List<ProjectInvitationResponse> invitations(UUID id);

    @Operation(summary = "Aceita ou recusa convite autenticado")
    ProjectInvitationResponse respond(UUID id, boolean accept);

    @Operation(summary = "Revoga convite pendente")
    ProjectInvitationResponse revoke(UUID id);

    @Operation(summary = "Reenvia convite; renova validade por sete dias")
    ProjectInvitationResponse resend(UUID id);

    @Operation(summary = "Lista links de convite do projeto, sem o endereço; apenas gestores")
    List<ProjectInviteLinkResponse> inviteLinks(UUID id);

    @Operation(
            summary = "Cria link de convite para Membro ou Observador",
            description =
                    "O endereço completo só é devolvido nesta resposta; o servidor guarda apenas o hash")
    ProjectInviteLinkResponse createInviteLink(UUID id, ProjectInviteLinkRequest dto);

    @Operation(summary = "Desativa um link de convite")
    void revokeInviteLink(UUID id, UUID linkId);

    @Operation(summary = "Consulta o projeto de um link de convite válido")
    ProjectInviteLinkPreviewResponse previewInviteLink(ProjectInviteLinkTokenRequest dto);

    @Operation(summary = "Entra no projeto pelo link de convite")
    ProjectResponse redeemInviteLink(ProjectInviteLinkTokenRequest dto);

    @Operation(summary = "Lista atividades da agenda")
    List<ProjectActivityResponse> activities(UUID id);

    @Operation(summary = "Cria atividade; serviços mantêm vínculo técnico")
    ProjectActivityResponse activity(UUID id, ProjectActivityRequest dto);

    @Operation(summary = "Edita atividade planejada")
    ProjectActivityResponse updateActivity(UUID id, UUID activityId, ProjectActivityRequest dto);

    @Operation(summary = "Inicia, conclui ou cancela atividade")
    ProjectActivityResponse activityStatus(UUID id, UUID activityId, ProjectDecisionRequest dto);

    @Operation(summary = "Histórico auditável de alterações")
    List<Map<String, Object>> history(UUID id);

    @Operation(summary = "Lista fotos privadas")
    List<ProjectPhotoResponse> photoList(UUID id);

    @Operation(summary = "Envia imagem JPEG/PNG de até 8 MB")
    ProjectPhotoResponse upload(
            UUID id, org.springframework.web.multipart.MultipartFile file, boolean cover);

    @Operation(summary = "Lê conteúdo privado de uma foto")
    org.springframework.http.ResponseEntity<byte[]> photo(UUID id, UUID photoId);

    @Operation(summary = "Remove uma foto")
    void removePhoto(UUID id, UUID photoId);

    @Operation(summary = "Define capa do projeto")
    ProjectPhotoResponse cover(UUID id, UUID photoId);
}
