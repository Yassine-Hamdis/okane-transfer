import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule }                 from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator }   from '@angular/material/paginator';
import { MatCardModule }    from '@angular/material/card';
import { MatIconModule }    from '@angular/material/icon';
import { MatInputModule }   from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule }   from '@angular/material/chips';

import { ChatbotService }          from '../../../core/services/chatbot.service';
import { EscalatedConversation }   from '../../../core/models/chatbot.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-chatbot-escalations',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatCardModule, MatIconModule,
    MatInputModule, MatFormFieldModule, MatChipsModule,
    LoadingSpinnerComponent,
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Chatbot Escalations</h1>
      </div>

      <app-loading-spinner *ngIf="loading" />

      <mat-card *ngIf="!loading">
        <div style="padding:16px 16px 0">
          <mat-form-field appearance="outline" style="width:320px">
            <mat-label>Search sessions</mat-label>
            <input matInput (input)="applyFilter($any($event.target).value)" />
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>
        </div>

        <div style="overflow-x:auto">
          <table mat-table [dataSource]="dataSource">

            <ng-container matColumnDef="sessionId">
              <th mat-header-cell *matHeaderCellDef>Session ID</th>
              <td mat-cell *matCellDef="let e">
                <code style="font-size:0.8rem;background:#e8eaf6;padding:2px 8px;border-radius:4px;color:#3949ab">
                  {{ e.sessionId | slice:0:16 }}...
                </code>
              </td>
            </ng-container>

            <ng-container matColumnDef="language">
              <th mat-header-cell *matHeaderCellDef>Language</th>
              <td mat-cell *matCellDef="let e">
                <span style="font-weight:700;font-size:0.85rem">{{ e.language }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="messages">
              <th mat-header-cell *matHeaderCellDef>Messages</th>
              <td mat-cell *matCellDef="let e">{{ e.messageCount }}</td>
            </ng-container>

            <ng-container matColumnDef="startedAt">
              <th mat-header-cell *matHeaderCellDef>Started</th>
              <td mat-cell *matCellDef="let e">{{ e.startedAt | date:'medium' }}</td>
            </ng-container>

            <ng-container matColumnDef="lastMessage">
              <th mat-header-cell *matHeaderCellDef>Last Message</th>
              <td mat-cell *matCellDef="let e">{{ e.lastMessageAt | date:'medium' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell"
                  [attr.colspan]="displayedColumns.length"
                  style="text-align:center;padding:48px;color:var(--okane-text-light)">
                <mat-icon style="font-size:48px;width:48px;height:48px;opacity:0.2;display:block;margin:0 auto 8px">
                  smart_toy
                </mat-icon>
                No escalated conversations
              </td>
            </tr>
          </table>
        </div>

        <mat-paginator [pageSizeOptions]="[10, 25]" showFirstLastButtons />
      </mat-card>
    </div>
  `,
})
export class ChatbotEscalationsComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['sessionId', 'language', 'messages', 'startedAt', 'lastMessage'];
  dataSource       = new MatTableDataSource<EscalatedConversation>();
  loading          = true;

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit(): void {
    this.chatbotService.getEscalated().subscribe({
      next: list => {
        this.dataSource.data      = list;
        this.dataSource.paginator = this.paginator;
        this.loading              = false;
      },
      error: () => { this.loading = false; },
    });
  }

  applyFilter(v: string): void {
    this.dataSource.filter = v.trim().toLowerCase();
  }
}
