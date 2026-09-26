export type AttentionLevel = 'Routine' | 'Soon' | 'Urgent';
export type AiStatus = 'OK' | 'FALLBACK' | 'ERROR';

export interface AiSummary {
  patientSummary: string;
  symptomsIdentified: string[];
  duration: string;
  missingInformation: string[];
  suggestedQuestions: string[];
  attentionLevel: AttentionLevel;
  disclaimer: string;
  status: AiStatus;
}

export interface PatientRequest {
  name: string;
  age: number;
  gender: string;
  phone: string;
  city: string;
  medicalHistory: string;
  symptoms: string[];
  symptomText: string;
  duration: string;
  notes: string;
}

export interface PatientResponse {
  id: number;
  name: string;
  age: number;
  gender: string;
  phone: string;
  city: string;
  medicalHistory: string;
  symptoms: string[];
  symptomText: string;
  duration: string;
  notes: string;
  createdAt: string;
  aiSummary: AiSummary;
}

export interface PatientListItem {
  id: number;
  name: string;
  age: number;
  mainSymptoms: string;
  duration: string;
  attentionLevel: AttentionLevel;
  createdAt: string;
}