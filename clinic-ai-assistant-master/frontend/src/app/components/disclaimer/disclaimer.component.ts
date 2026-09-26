import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-disclaimer',
  standalone: true,
  templateUrl: './disclaimer.component.html',
  styleUrl: './disclaimer.component.css'
})
export class DisclaimerComponent {
  @Input() message = 'This summary is generated for informational purposes only and is not a medical diagnosis or treatment recommendation. Please consult a qualified doctor for diagnosis, prescriptions, or treatment.';
}