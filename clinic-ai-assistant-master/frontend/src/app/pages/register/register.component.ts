import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PatientService } from '../../services/patient.service';

const SYMPTOM_OPTIONS = ['Fever', 'Cough', 'Headache', 'Cold', 'Body Pain', 'Stomach Pain'];

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  symptomOptions = SYMPTOM_OPTIONS;
  form: FormGroup;
  submitting = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      age: [null, [Validators.required, Validators.min(0), Validators.max(130)]],
      gender: ['Male'],
      phone: [''],
      city: [''],
      medicalHistory: [''],
      symptoms: this.fb.array(SYMPTOM_OPTIONS.map(() => false)),
      otherSymptom: [false],
      symptomText: [''],
      duration: ['', [Validators.required]],
      notes: ['']
    });
  }

  get symptomsArray(): FormArray {
    return this.form.get('symptoms') as FormArray;
  }

  private atLeastOneSymptomSelected(): boolean {
    const anyChecked = this.symptomsArray.value.some((v: boolean) => v);
    const otherChecked = this.form.get('otherSymptom')?.value;
    const otherHasText = !!this.form.get('symptomText')?.value?.trim();
    return anyChecked || (otherChecked && otherHasText);
  }

  submit(): void {
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Please fix the highlighted fields.';
      return;
    }

    if (!this.atLeastOneSymptomSelected()) {
      this.errorMessage = 'Please select at least one symptom, or describe it under "Other".';
      return;
    }

    const selectedSymptoms = this.symptomOptions.filter((_, i) => this.symptomsArray.value[i]);
    if (this.form.get('otherSymptom')?.value) {
      selectedSymptoms.push('Other');
    }

    this.submitting = true;
    this.patientService
      .createPatient({
        name: this.form.value.name,
        age: this.form.value.age,
        gender: this.form.value.gender,
        phone: this.form.value.phone,
        city: this.form.value.city,
        medicalHistory: this.form.value.medicalHistory,
        symptoms: selectedSymptoms,
        symptomText: this.form.value.symptomText,
        duration: this.form.value.duration,
        notes: this.form.value.notes
      })
      .subscribe({
        next: (response) => {
          this.submitting = false;
          this.router.navigate(['/patients', response.id]);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage =
            err?.error?.fieldErrors
              ? Object.values(err.error.fieldErrors).join(' ')
              : 'Something went wrong while registering the patient. Please try again.';
        }
      });
  }
}