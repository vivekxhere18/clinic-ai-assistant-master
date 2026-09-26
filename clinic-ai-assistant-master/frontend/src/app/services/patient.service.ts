import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PatientListItem, PatientRequest, PatientResponse } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly baseUrl = '/api/patients';

  constructor(private http: HttpClient) {}

  createPatient(request: PatientRequest): Observable<PatientResponse> {
    return this.http.post<PatientResponse>(this.baseUrl, request);
  }

  getPatients(query?: string): Observable<PatientListItem[]> {
    let params = new HttpParams();
    if (query) {
      params = params.set('q', query);
    }
    return this.http.get<PatientListItem[]>(this.baseUrl, { params });
  }

  getPatient(id: number): Observable<PatientResponse> {
    return this.http.get<PatientResponse>(`${this.baseUrl}/${id}`);
  }
}