import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { AttentionBadgeComponent } from '../../components/attention-badge/attention-badge.component';
import { DisclaimerComponent } from '../../components/disclaimer/disclaimer.component';
import { PatientResponse } from '../../models/patient.model';
import { PatientService } from '../../services/patient.service';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, AttentionBadgeComponent, DisclaimerComponent],
  templateUrl: './patient-detail.component.html',
  styleUrl: './patient-detail.component.css'
})
export class PatientDetailComponent implements OnInit {
  patient: PatientResponse | null = null;
  loading = true;
  errorMessage = '';

  constructor(private route: ActivatedRoute, private patientService: PatientService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.patientService.getPatient(id).subscribe({
      next: (patient) => {
        this.patient = patient;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load this patient record.';
        this.loading = false;
      }
    });
  }
}