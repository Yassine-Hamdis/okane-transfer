import { Component, OnInit }       from '@angular/core';
import { CommonModule }            from '@angular/common';
import { MatCardModule }           from '@angular/material/card';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatTabsModule }           from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule }        from '@angular/material/divider';
import { MatTooltipModule }        from '@angular/material/tooltip';

import { NotificationService }     from '../../../core/services/notification.service';
import { Notification }            from '../../../core/models/notification.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatSnackBarModule,
    MatDividerModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './notifications.component.html',
  styleUrls:   ['./notifications.component.scss'],
})
export class NotificationsComponent implements OnInit {

  loading          = true;
  all:    Notification[] = [];
  unread: Notification[] = [];

  constructor(
    private notificationService: NotificationService,
    private snackBar:            MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.notificationService.getAll().subscribe({
      next: notifs => {
        this.all    = notifs;
        this.unread = notifs.filter(n => !n.read);
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  markRead(notif: Notification): void {
    if (notif.read) return;
    this.notificationService.markAsRead(notif.id).subscribe({
      next: () => {
        notif.read = true;
        this.unread = this.all.filter(n => !n.read);
        this.notificationService.refreshCount();
      },
    });
  }

  markAllRead(): void {
    this.notificationService.markAllRead().subscribe({
      next: msg => {
        this.all    = this.all.map(n => ({ ...n, read: true }));
        this.unread = [];
        this.notificationService.refreshCount();
        this.snackBar.open(msg || 'All marked as read', 'OK', { duration: 3000 });
      },
      error: err =>
        this.snackBar.open(err.error?.message ?? 'Failed', 'OK', { duration: 3000 }),
    });
  }

  getChannelIcon(channel: string): string {
    const map: Record<string, string> = {
      EMAIL: 'email',
      SMS:   'sms',
      PUSH:  'notifications',
    };
    return map[channel] ?? 'notifications';
  }
}
