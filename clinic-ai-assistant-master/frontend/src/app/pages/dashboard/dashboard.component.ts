import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AttentionBadgeComponent } from '../../components/attention-badge/attention-badge.component';
import { PatientListItem } from '../../models/patient.model';
import { PatientService } from '../../services/patient.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AttentionBadgeComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  patients: PatientListItem[] = [];
  loading = false;
  errorMessage = '';
  searchControl = new FormControl('');

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.load();

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((value) => this.load(value ?? ''));
  }

  load(query = ''): void {
    this.loading = true;
    this.errorMessage = '';
    this.patientService.getPatients(query).subscribe({
      next: (patients) => {
        this.patients = patients;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load patients right now. Please try again.';
        this.loading = false;
      }
    });
  }
}