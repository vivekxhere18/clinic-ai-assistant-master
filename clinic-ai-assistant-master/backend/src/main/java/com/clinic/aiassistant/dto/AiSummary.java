package com.clinic.aiassistant.dto;

import java.util.List;

/**
 * Structured, non-diagnostic AI summary. attentionLevel is a triage urgency hint only
 * (Routine / Soon / Urgent) — it never names or implies a medical condition.
 */
public class AiSummary {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_FALLBACK = "FALLBACK";
    public static final String STATUS_ERROR = "ERROR";

    public static final String LEVEL_ROUTINE = "Routine";
    public static final String LEVEL_SOON = "Soon";
    public static final String LEVEL_URGENT = "Urgent";

    private String patientSummary;
    private List<String> symptomsIdentified;
    private String duration;
    private List<String> missingInformation;
    private List<String> suggestedQuestions;
    private String attentionLevel;
    private String disclaimer;
    private String status;

    public AiSummary() {
    }

    public AiSummary(String patientSummary, List<String> symptomsIdentified, String duration,
                      List<String> missingInformation, List<String> suggestedQuestions,
                      String attentionLevel, String disclaimer, String status) {
        this.patientSummary = patientSummary;
        this.symptomsIdentified = symptomsIdentified;
        this.duration = duration;
        this.missingInformation = missingInformation;
        this.suggestedQuestions = suggestedQuestions;
        this.attentionLevel = attentionLevel;
        this.disclaimer = disclaimer;
        this.status = status;
    }

    public String getPatientSummary() {
        return patientSummary;
    }

    public void setPatientSummary(String patientSummary) {
        this.patientSummary = patientSummary;
    }

    public List<String> getSymptomsIdentified() {
        return symptomsIdentified;
    }

    public void setSymptomsIdentified(List<String> symptomsIdentified) {
        this.symptomsIdentified = symptomsIdentified;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<String> getMissingInformation() {
        return missingInformation;
    }

    public void setMissingInformation(List<String> missingInformation) {
        this.missingInformation = missingInformation;
    }

    public List<String> getSuggestedQuestions() {
        return suggestedQuestions;
    }

    public void setSuggestedQuestions(List<String> suggestedQuestions) {
        this.suggestedQuestions = suggestedQuestions;
    }

    public String getAttentionLevel() {
        return attentionLevel;
    }

    public void setAttentionLevel(String attentionLevel) {
        this.attentionLevel = attentionLevel;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}