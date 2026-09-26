import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AttentionLevel } from '../../models/patient.model';

@Component({
  selector: 'app-attention-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attention-badge.component.html',
  styleUrl: './attention-badge.component.css'
})
export class AttentionBadgeComponent {
  @Input() level: AttentionLevel = 'Routine';

  get cssClass(): string {
    return 'badge-' + this.level.toLowerCase();
  }
}