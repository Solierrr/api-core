package com.solaria.persistence.service.execution;

import com.solaria.persistence.domain.entity.execution.ProjectMember;
import com.solaria.persistence.domain.entity.execution.TechnicalProject;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.repository.execution.ProjectMemberRepository;
import com.solaria.persistence.repository.execution.TechnicalProjectRepository;
import com.solaria.persistence.repository.identity.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectRosterTest {
    @Mock TechnicalProjectRepository projects;
    @Mock ProjectMemberRepository members;
    @Mock UserRepository users;
    @Mock ProjectAccess access;
    @InjectMocks ProjectService service;

    @Test void rosterDoesNotExposeAuthIdOfMemberWhoseProjectAccessWasRevoked() {
        var project = new TechnicalProject(); project.setId(UUID.randomUUID());
        var allowed = new User(); allowed.setId(UUID.randomUUID()); allowed.setAuth_id(UUID.randomUUID());
        var denied = new User(); denied.setId(UUID.randomUUID()); denied.setAuth_id(UUID.randomUUID());
        var first = new ProjectMember(); first.setId(UUID.randomUUID()); first.setUserId(allowed.getId());
        var second = new ProjectMember(); second.setId(UUID.randomUUID()); second.setUserId(denied.getId());
        when(projects.findById(project.getId())).thenReturn(Optional.of(project));
        when(members.findByProjectIdAndActiveTrue(project.getId())).thenReturn(List.of(first, second));
        when(users.findById(allowed.getId())).thenReturn(Optional.of(allowed));
        when(users.findById(denied.getId())).thenReturn(Optional.of(denied));
        when(access.canRead(project, allowed)).thenReturn(true);
        assertEquals(List.of(allowed.getAuth_id()), service.memberList(project.getId()).stream().map(member -> member.authId()).toList());
    }
}
