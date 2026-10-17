package com.solaria.persistence.service.professional;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.solaria.persistence.domain.entity.identity.Person;
import com.solaria.persistence.domain.entity.identity.User;
import com.solaria.persistence.domain.entity.professional.*;
import com.solaria.persistence.domain.entity.shared.Contact;
import com.solaria.persistence.domain.enums.professional.TechnicianStatus;
import com.solaria.persistence.dto.professional.ProfessionalOnboardingContract.*;
import com.solaria.persistence.repository.identity.*;
import com.solaria.persistence.repository.professional.*;
import com.solaria.persistence.repository.shared.ContactRepository;
import com.solaria.persistence.security.CurrentUserService;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional
public class ProfessionalOnboardingService {
    private final ProfessionalOnboardingRepository profiles;
    private final ProfessionalSubmissionRepository submissions;
    private final ProfessionalDocumentRepository documents;
    private final ProfessionRepository professions;
    private final UserRepository users;
    private final PersonRepository persons;
    private final TechnicianRepository technicians;
    private final ProfessionalRegistrationRepository registrations;
    private final ContactRepository contacts;
    private final PlatformAdminRepository admins;
    private final CurrentUserService current;
    private final ObjectMapper json;
    private final ProfessionalDocumentStorageService storage;

    public ProfessionalOnboardingService(ProfessionalOnboardingRepository profiles,
            ProfessionalSubmissionRepository submissions, ProfessionalDocumentRepository documents,
            ProfessionRepository professions, UserRepository users, PersonRepository persons,
            TechnicianRepository technicians, ProfessionalRegistrationRepository registrations,
            ContactRepository contacts, PlatformAdminRepository admins, CurrentUserService current,
            ObjectMapper json, ProfessionalDocumentStorageService storage) {
        this.profiles = profiles;
        this.submissions = submissions;
        this.documents = documents;
        this.professions = professions;
        this.users = users;
        this.persons = persons;
        this.technicians = technicians;
        this.registrations = registrations;
        this.contacts = contacts;
        this.admins = admins;
        this.current = current;
        this.json = json;
        this.storage = storage;
    }

    public Profile me() {
        var profile = findOrCreate();
        return profileInfo(profile);
    }

    public Profile saveDraft(Draft draft) {
        var profile = findOrCreate();
        if (draft.revision() != profile.getRevision()) conflict();
        if (submissions.findByOnboardingIdOrderByCreatedAtDesc(profile.getId()).stream()
                .anyMatch(s -> Set.of("QUEUED", "VALIDATING", "RETRY_PENDING", "MANUAL_REVIEW").contains(s.getStatus()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A submission is awaiting a decision");
        }
        profile.setRevision(profile.getRevision() + 1);
        profile.setDraftJson(json.writeValueAsString(draft));
        profiles.save(profile);
        return profileInfo(profile);
    }

    public Policy policy(UUID professionId) {
        var profession = professions.findById(professionId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profession not found"));
        boolean registration = Boolean.TRUE.equals(profession.getRequiresRegistration());
        var required = new java.util.ArrayList<String>(List.of("IDENTITY"));
        var criteria = new java.util.ArrayList<String>(List.of("IDENTITY_MATCH"));
        if (registration) { required.add("REGISTRATION"); criteria.add("REGISTRATION"); }
        if (Boolean.TRUE.equals(profession.getRequiresNr10())) { required.add("NR10"); criteria.add("NR10"); }
        if (Boolean.TRUE.equals(profession.getRequiresNr35())) { required.add("NR35"); criteria.add("NR35"); }
        boolean configured = profession.getName() != null && !profession.getName().isBlank() && profession.getRequiresRegistration() != null
                && profession.getRequiresNr10() != null && profession.getRequiresNr35() != null;
        return new Policy(1, configured, registration, profession.getName(), List.copyOf(required), List.copyOf(criteria));
    }

    public SubmissionInfo submit(long revision) {
        var profile = findOrCreate();
        if (profile.getRevision() != revision) conflict();
        var previous = submissions.findByOnboardingIdOrderByCreatedAtDesc(profile.getId());
        var same = previous.stream().filter(s -> s.getRevision() == revision).findFirst();
        if (same.isPresent()) return submissionInfo(same.get());
        Draft draft = readDraft(profile.getDraftJson());
        Policy policy = policy(draft.professionId());
        validateDraft(draft, policy, profile.getUserId());
        var submission = new ProfessionalSubmission();
        submission.setId(UUID.randomUUID());
        submission.setOnboardingId(profile.getId());
        submission.setRevision(revision);
        submission.setBaseApprovedVersion(profile.getApprovedVersion());
        submission.setSnapshotJson(profile.getDraftJson());
        submission.setPolicyJson(json.writeValueAsString(policy));
        if (!policy.configured() || draft.documentIds().stream().map(id -> documents.findById(id).orElseThrow()).anyMatch(doc -> !doc.getContentType().startsWith("image/"))) submission.setStatus("MANUAL_REVIEW");
        submissions.save(submission);
        return submissionInfo(submission);
    }

    public DocumentInfo upload(String kind, MultipartFile file) throws IOException {
        User user = current.getCurrentUser();
        users.findByIdForUpdate(user.getId()).orElseThrow();
        if (!Set.of("IDENTITY", "NR10", "NR35", "REGISTRATION", "CERTIFICATE").contains(kind)
                || file.isEmpty() || file.getSize() > 10 * 1024 * 1024
                || !ProfessionalDocumentTypes.ALLOWED.contains(file.getContentType())
                || documents.countByUserIdAndCreatedAtAfter(user.getId(), Instant.now().minusSeconds(86400)) >= 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid document or document limit reached");
        }
        byte[] bytes = file.getBytes();
        ProfessionalDocumentTypes.validate(file.getContentType(), bytes);
        var doc = new ProfessionalDocument();
        doc.setId(UUID.randomUUID());
        doc.setUserId(user.getId());
        doc.setKind(kind);
        doc.setContentType(file.getContentType());
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            fileName = fileName.replace('\\', '_').replace('/', '_').replace('\r', '_').replace('\n', '_');
            doc.setOriginalFileName(fileName.substring(0, Math.min(255, fileName.length())));
        }

        try {
            doc.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
        doc.setContent(bytes);
        documents.saveAndFlush(doc);
        return new DocumentInfo(doc.getId(), kind, doc.getSha256(), doc.getContentType(), doc.getOriginalFileName());
    }

    public ProfessionalDocument document(UUID id) {
        var doc = documents.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = current.getCurrentUser();
        if (!doc.getUserId().equals(user.getId()) && !admins.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return doc;
    }

    public byte[] documentContent(UUID id) {
        return storage.read(document(id));
    }

    public List<SubmissionInfo> reviews(int page) {
        requireAdmin();
        return submissions.findByStatusOrderByCreatedAtAsc("MANUAL_REVIEW", PageRequest.of(Math.max(0, page), 25))
                .stream().map(this::submissionInfo).toList();
    }

    public List<ReviewSummary> reviewQueue() {
        requireAdmin();
        return submissions.findByStatusOrderByCreatedAtAsc("MANUAL_REVIEW", org.springframework.data.domain.Pageable.unpaged()).stream()
                .sorted(java.util.Comparator.comparing(ProfessionalSubmission::getCreatedAt).thenComparing(ProfessionalSubmission::getId))
                .map(item -> { var draft = readDraft(item.getSnapshotJson()); return new ReviewSummary(item.getId(), draft.name(), draft.registrationNumber()); }).toList();
    }

    public SubmissionInfo submission(UUID id) {
        var submission = submissions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var profile = profiles.findById(submission.getOnboardingId()).orElseThrow();
        User user = current.getCurrentUser();
        if (!profile.getUserId().equals(user.getId()) && !admins.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return submissionInfo(submission);
    }

    public SubmissionInfo decide(UUID id, Decision decision) {
        requireAdmin();
        var submission = submissions.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!"MANUAL_REVIEW".equals(submission.getStatus()) || submission.getRevision() != decision.revision()) conflict();
        var profile = profiles.lockById(submission.getOnboardingId()).orElseThrow();
        if (decision.approved()) apply(profile, submission);
        submission.setStatus(decision.approved() ? "APPROVED" : "REJECTED");
        submission.setDecidedAt(Instant.now());
        submission.setDecidedBy(current.getCurrentUser().getId());
        submission.setDecisionOrigin("ADMIN");
        submission.setDecisionReason(decision.reason());
        submissions.save(submission);
        return submissionInfo(submission);
    }

    public Eligibility suspend(UUID authId, Suspension decision) {
        requireAdmin();
        var profile = profiles.lockByAuthId(authId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        profile.setSuspended(decision.suspended());
        profiles.save(profile);
        var audit = new ProfessionalSubmission();
        audit.setId(UUID.randomUUID());
        audit.setOnboardingId(profile.getId());
        profile.setRevision(profile.getRevision() + 1);
        audit.setRevision(profile.getRevision());
        audit.setBaseApprovedVersion(profile.getApprovedVersion());
        audit.setSnapshotJson(profile.getApprovedJson() == null ? "{}" : profile.getApprovedJson());
        audit.setPolicyJson("{}");
        audit.setStatus(decision.suspended() ? "SUSPENDED" : "REINSTATED");
        audit.setDecidedAt(Instant.now());
        audit.setDecidedBy(current.getCurrentUser().getId());
        audit.setDecisionOrigin("ADMIN");
        audit.setDecisionReason(decision.reason());
        submissions.save(audit);
        return eligibility(authId);
    }

    @Transactional(readOnly = true)
    public Eligibility eligibility(UUID authId) {
        User user = users.findByAuthId(authId).orElse(null);
        var profile = profiles.findByAuthId(authId).orElse(null);
        boolean professional = profile != null && profile.getTechnicianId() != null;
        boolean complete = profile != null && profile.getApprovedJson() != null && profile.getApprovedVersion() > 0;
        boolean active = user != null && Boolean.TRUE.equals(user.getActive());
        boolean approved = professional && technicians.findById(profile.getTechnicianId())
                .map(t -> t.getStatus() == TechnicianStatus.APPROVED).orElse(false);
        boolean suspended = profile != null && profile.isSuspended();
        var reasons = new java.util.ArrayList<String>();
        if (!professional) reasons.add("NOT_PROFESSIONAL");
        if (!active) reasons.add("USER_INACTIVE");
        if (!complete) reasons.add("PROFILE_INCOMPLETE");
        if (!approved) reasons.add("APPROVAL_PENDING");
        if (suspended) reasons.add("PROFESSIONAL_SUSPENDED");
        return new Eligibility(authId, user == null ? null : user.getId(),
                profile == null ? null : profile.getTechnicianId(), professional, active, complete,
                approved ? "APPROVED" : "UNDER_ANALYSIS", reasons.isEmpty(), reasons,
                profile == null ? 0 : profile.getApprovedVersion(), Instant.now());
    }

    public void apply(ProfessionalOnboarding profile, ProfessionalSubmission submission) {
        var ids = readDraft(submission.getSnapshotJson()).documentIds();
        apply(profile, submission, ids == null ? Set.of() : Set.copyOf(ids));
    }

    public void apply(ProfessionalOnboarding profile, ProfessionalSubmission submission, Set<UUID> validatedDocuments) {
        if (profile.getApprovedVersion() != submission.getBaseApprovedVersion()) conflict();
        Draft draft = readDraft(submission.getSnapshotJson());
        validateDraft(draft, readPolicy(submission.getPolicyJson()), profile.getUserId());
        User user = users.findById(profile.getUserId()).orElseThrow();
        var person = persons.findByUserId(user.getId()).stream().findFirst().orElseGet(Person::new);
        if (person.getId() == null ? persons.existsByCpf(draft.cpf()) : persons.existsByCpfAndIdNot(draft.cpf(), person.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identity already registered");
        }
        for (UUID documentId : validatedDocuments) {
            if (!draft.documentIds().contains(documentId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            var document = documents.findById(documentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (!document.getUserId().equals(profile.getUserId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            storage.archive(document);
            documents.saveAndFlush(document);
        }
        var contact = person.getContact() == null ? new Contact() : person.getContact();
        contact.setEmail(draft.email());
        contact.setPhone(draft.phone());
        person.setContact(contacts.save(contact));
        person.setUser(user);
        person.setName(draft.name());
        person.setCpf(draft.cpf());
        person.setBirthDate(draft.birthDate());
        persons.save(person);
        var technician = technicians.findByPersonId(person.getId()).orElseGet(Technician::new);
        technician.setPerson(person);
        technician.setCrea(draft.registrationNumber());
        if (technician.getSlug() == null) technician.setSlug("professional-" + UUID.randomUUID());
        technician.setStatus(TechnicianStatus.APPROVED);
        technicians.save(technician);
        var registration = registrations.findByTechnicianId(technician.getId()).stream()
                .filter(r -> r.getProfession() != null && r.getProfession().getId().equals(draft.professionId()))
                .findFirst().orElseGet(ProfessionalRegistration::new);
        registration.setTechnician(technician);
        registration.setProfession(professions.findById(draft.professionId()).orElseThrow());
        registration.setCouncil(draft.council());
        registration.setNumber(draft.registrationNumber());
        registration.setExpirationDate(draft.registrationExpiration() == null ? null : draft.registrationExpiration().atStartOfDay());
        registrations.save(registration);
        profile.setTechnicianId(technician.getId());
        profile.setApprovedVersion(submission.getRevision());
        profile.setApprovedJson(submission.getSnapshotJson());
        profiles.save(profile);
    }

    public void validateDraft(Draft draft, Policy policy, UUID userId) {
        if (draft.name() == null || draft.name().isBlank() || draft.name().length() < 2 || draft.name().length() > 60
                || !validCpf(draft.cpf()) || draft.birthDate() == null || !draft.birthDate().isBefore(LocalDate.now())
                || draft.email() == null || draft.email().length() > 100 || !draft.email().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || (draft.council() != null && draft.council().length() > 60)
                || (draft.registrationNumber() != null && draft.registrationNumber().length() > 30)
                || draft.professionId() == null || draft.phone() == null || !draft.phone().matches("[0-9]{10,12}")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Incomplete personal information");
        }
        if (policy.requiresRegistration() && (draft.council() == null || draft.council().isBlank()
                || draft.registrationNumber() == null || draft.registrationNumber().isBlank()
                || (draft.registrationExpiration() != null && draft.registrationExpiration().isBefore(LocalDate.now())))) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Incomplete professional registration");
        }
        var kinds = new java.util.HashSet<String>();
        var ids = draft.documentIds() == null ? List.<UUID>of() : draft.documentIds();
        if (ids.size() > 10 || Set.copyOf(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid document list");
        }
        for (UUID id : ids) {
            var document = documents.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (!document.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            kinds.add(document.getKind());
        }
        if (!kinds.containsAll(policy.requiredDocuments())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Required documents are missing");
        }
    }

    public static boolean validCpf(String cpf) {
        if (cpf == null || !cpf.matches("[0-9]{11}") || cpf.chars().distinct().count() == 1) return false;
        for (int length = 9; length <= 10; length++) {
            int sum = 0;
            for (int i = 0; i < length; i++) sum += (cpf.charAt(i) - '0') * (length + 1 - i);
            int digit = 11 - sum % 11;
            if (digit >= 10) digit = 0;
            if (cpf.charAt(length) - '0' != digit) return false;
        }
        return true;
    }

    public Draft readDraft(String value) {
        if (json.readTree(value).isEmpty()) return new Draft(0, null, null, null, null, null, null, null, null, null, List.of());
        return json.readValue(value, Draft.class);
    }
    public Policy readPolicy(String value) {
        if (json.readTree(value).isEmpty()) return new Policy(0, false, false, null, List.of(), List.of());
        return json.readValue(value, Policy.class);
    }
    private Profile profileInfo(ProfessionalOnboarding profile) {
        return new Profile(profile.getRevision(), profile.getApprovedVersion(), profile.isSuspended(),
                readDraft(profile.getDraftJson()), profile.getApprovedJson() == null ? null : readDraft(profile.getApprovedJson()),
                submissions.findByOnboardingIdOrderByCreatedAtDesc(profile.getId()).stream().map(this::submissionInfo).toList());
    }
    @SuppressWarnings("unchecked")
    public SubmissionInfo submissionInfo(ProfessionalSubmission submission) {
        return new SubmissionInfo(submission.getId(), submission.getRevision(), submission.getBaseApprovedVersion(),
                submission.getStatus(), readDraft(submission.getSnapshotJson()),
                profiles.findById(submission.getOnboardingId()).map(ProfessionalOnboarding::getApprovedJson).map(this::readDraft).orElse(null),
                readPolicy(submission.getPolicyJson()), submission.getResultJson() == null ? Map.of()
                        : json.readValue(submission.getResultJson(), Map.class),
                submission.getCreatedAt(), submission.getDecidedAt(), submission.getDecidedBy(),
                submission.getDecisionOrigin(), submission.getDecisionReason());
    }
    private ProfessionalOnboarding findOrCreate() {
        User user = current.getCurrentUser();
        users.findByIdForUpdate(user.getId()).orElseThrow();
        return profiles.lockByAuthId(user.getAuth_id()).orElseGet(() -> {
            var profile = new ProfessionalOnboarding();
            profile.setId(UUID.randomUUID());
            profile.setAuthId(user.getAuth_id());
            profile.setUserId(user.getId());
            return profiles.save(profile);
        });
    }
    private void requireAdmin() {
        if (!admins.existsByUserId(current.getCurrentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solaria administrator required");
        }
    }
    private static void conflict() {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Submission version is no longer current");
    }
}
