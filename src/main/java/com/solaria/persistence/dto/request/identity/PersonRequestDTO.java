package com.solaria.persistence.dto.request.identity;

import com.solaria.persistence.util.RegexValidator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PersonRequestDTO {

    @NotNull(message = "ID do Usuário é obrigatório")
    private UUID userId;

    @NotNull(message = "ID do Contato é obrigatório")
    private UUID contactId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 60)
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @Size(max = 11)
    @Pattern(regexp = RegexValidator.CPF_REGEX, message = "CPF inválido")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate birthDate;

}
