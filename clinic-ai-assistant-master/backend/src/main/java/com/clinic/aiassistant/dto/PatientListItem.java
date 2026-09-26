package com.clinic.aiassistant.dto;

import java.time.LocalDateTime;

/** Lightweight row for the dashboard list view. */
public class PatientListItem {

    private Long id;
    private String name;
    private Integer age;
    private String mainSymptoms;
    private String duration;
    private String attentionLevel;
    private LocalDateTime createdAt;

    public PatientListItem() {
    }

    public PatientListItem(Long id, String name, Integer age, String mainSymptoms,
                            String duration, String attentionLevel, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.mainSymptoms = mainSymptoms;
        this.duration = duration;
        this.attentionLevel = attentionLevel;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMainSymptoms() {
        return mainSymptoms;
    }

    public void setMainSymptoms(String mainSymptoms) {
        this.mainSymptoms = mainSymptoms;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAttentionLevel() {
        return attentionLevel;
    }

    public void setAttentionLevel(String attentionLevel) {
        this.attentionLevel = attentionLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}