package com.clinic.aiassistant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    private String gender;

    private String phone;

    private String city;

    @Lob
    private String medicalHistory;

    /** Comma-separated selected symptoms (e.g. "Fever,Cough,Other: rash on arm"). */
    @Column(nullable = false, length = 1000)
    private String symptoms;

    @Lob
    private String symptomText;

    @Column(nullable = false)
    private String duration;

    @Lob
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ----- AI result columns -----

    @Lob
    private String aiSummary;

    /** JSON array as String, e.g. ["Fever","Cough"] */
    @Lob
    private String aiSymptoms;

    private String aiDuration;

    /** JSON array as String */
    @Lob
    private String aiMissingInfo;

    /** JSON array as String */
    @Lob
    private String aiQuestions;

    private String attentionLevel;

    @Lob
    private String aiDisclaimer;

    private String aiStatus;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getSymptomText() {
        return symptomText;
    }

    public void setSymptomText(String symptomText) {
        this.symptomText = symptomText;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getAiSymptoms() {
        return aiSymptoms;
    }

    public void setAiSymptoms(String aiSymptoms) {
        this.aiSymptoms = aiSymptoms;
    }

    public String getAiDuration() {
        return aiDuration;
    }

    public void setAiDuration(String aiDuration) {
        this.aiDuration = aiDuration;
    }

    public String getAiMissingInfo() {
        return aiMissingInfo;
    }

    public void setAiMissingInfo(String aiMissingInfo) {
        this.aiMissingInfo = aiMissingInfo;
    }

    public String getAiQuestions() {
        return aiQuestions;
    }

    public void setAiQuestions(String aiQuestions) {
        this.aiQuestions = aiQuestions;
    }

    public String getAttentionLevel() {
        return attentionLevel;
    }

    public void setAttentionLevel(String attentionLevel) {
        this.attentionLevel = attentionLevel;
    }

    public String getAiDisclaimer() {
        return aiDisclaimer;
    }

    public void setAiDisclaimer(String aiDisclaimer) {
        this.aiDisclaimer = aiDisclaimer;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }
}