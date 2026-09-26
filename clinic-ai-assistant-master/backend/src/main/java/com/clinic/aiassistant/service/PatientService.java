package com.clinic.aiassistant.service;

import com.clinic.aiassistant.dto.AiSummary;
import com.clinic.aiassistant.dto.PatientListItem;
import com.clinic.aiassistant.dto.PatientRequest;
import com.clinic.aiassistant.dto.PatientResponse;
import com.clinic.aiassistant.exception.ResourceNotFoundException;
import com.clinic.aiassistant.model.Patient;
import com.clinic.aiassistant.repository.PatientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AiSummaryService aiSummaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PatientService(PatientRepository patientRepository, AiSummaryService aiSummaryService) {
        this.patientRepository = patientRepository;
        this.aiSummaryService = aiSummaryService;
    }

    public PatientResponse createPatient(PatientRequest request) {
        AiSummary aiSummary = aiSummaryService.generateSummary(request);

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setCity(request.getCity());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setSymptoms(String.join(",", request.getSymptoms()));
        patient.setSymptomText(request.getSymptomText());
        patient.setDuration(request.getDuration());
        patient.setNotes(request.getNotes());
        patient.setCreatedAt(LocalDateTime.now());

        patient.setAiSummary(aiSummary.getPatientSummary());
        patient.setAiSymptoms(toJson(aiSummary.getSymptomsIdentified()));
        patient.setAiDuration(aiSummary.getDuration());
        patient.setAiMissingInfo(toJson(aiSummary.getMissingInformation()));
        patient.setAiQuestions(toJson(aiSummary.getSuggestedQuestions()));
        patient.setAttentionLevel(aiSummary.getAttentionLevel());
        patient.setAiDisclaimer(aiSummary.getDisclaimer());
        patient.setAiStatus(aiSummary.getStatus());

        Patient saved = patientRepository.save(patient);
        return toResponse(saved);
    }

    public List<PatientListItem> listPatients(String query) {
        List<Patient> patients = (query == null || query.isBlank())
                ? patientRepository.findAllByOrderByCreatedAtDesc()
                : patientRepository.search(query.trim());

        return patients.stream().map(p -> new PatientListItem(
                p.getId(),
                p.getName(),
                p.getAge(),
                mainSymptomsPreview(p),
                p.getDuration(),
                p.getAttentionLevel(),
                p.getCreatedAt()
        )).collect(Collectors.toList());
    }

    public PatientResponse getPatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return toResponse(patient);
    }

    private String mainSymptomsPreview(Patient p) {
        return p.getSymptoms() == null ? "" : p.getSymptoms();
    }

    private PatientResponse toResponse(Patient p) {
        PatientResponse response = new PatientResponse();
        response.setId(p.getId());
        response.setName(p.getName());
        response.setAge(p.getAge());
        response.setGender(p.getGender());
        response.setPhone(p.getPhone());
        response.setCity(p.getCity());
        response.setMedicalHistory(p.getMedicalHistory());
        response.setSymptoms(p.getSymptoms() == null || p.getSymptoms().isBlank()
                ? Collections.emptyList()
                : List.of(p.getSymptoms().split(",")));
        response.setSymptomText(p.getSymptomText());
        response.setDuration(p.getDuration());
        response.setNotes(p.getNotes());
        response.setCreatedAt(p.getCreatedAt());

        AiSummary aiSummary = new AiSummary();
        aiSummary.setPatientSummary(p.getAiSummary());
        aiSummary.setSymptomsIdentified(fromJson(p.getAiSymptoms()));
        aiSummary.setDuration(p.getAiDuration());
        aiSummary.setMissingInformation(fromJson(p.getAiMissingInfo()));
        aiSummary.setSuggestedQuestions(fromJson(p.getAiQuestions()));
        aiSummary.setAttentionLevel(p.getAttentionLevel());
        aiSummary.setDisclaimer(p.getAiDisclaimer());
        aiSummary.setStatus(p.getAiStatus());
        response.setAiSummary(aiSummary);

        return response;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}