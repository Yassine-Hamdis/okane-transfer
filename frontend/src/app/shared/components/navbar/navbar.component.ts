import {
  Component, OnInit, OnDestroy,
  Input, Output, EventEmitter,
} from '@angular/core';
import { CommonModule }       from '@angular/common';
import { RouterModule }       from '@angular/router';
import { MatToolbarModule }   from '@angular/material/toolbar';
import { MatIconModule }      from '@angular/material/icon';
import { MatButtonModule }    from '@angular/material/button';
import { MatMenuModule }      from '@angular/material/menu';
import { MatBadgeModule }     from '@angular/material/badge';
import { MatDividerModule }   from '@angular/material/divider';
import { Subscription }       from 'rxjs';

import { AuthService }           from '../../../core/services/auth.service';
import { NotificationService }   from '../../../core/services/notification.service';
import { Notification }          from '../../../core/models/notification.model';
import { UserRole }              from '../../../core/models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule,
  ],
  templateUrl: './navbar.component.html',
  styleUrls:   ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit, OnDestroy {

  @Input()  sidebarCollapsed = false;
  @Output() toggleSidebar    = new EventEmitter<void>();

  fullName     = '';
  role: UserRole | null = null;
  roleLabel    = '';
  isClient     = false;

  unreadCount  = 0;
  recentNotifs: Notification[] = [];

  private sub = new Subscription();

  constructor(
    private authService:         AuthService,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.fullName  = this.authService.getFullName() ?? 'User';
    this.role      = this.authService.getRole();
    this.roleLabel = this.getRoleLabel(this.role);
    this.isClient  = this.role === 'ROLE_CLIENT';

    if (this.isClient) {
      // Subscribe to live unread count
      this.sub.add(
        this.notificationService.unreadCount$.subscribe(
          count => this.unreadCount = count
        )
      );
      this.notificationService.startPolling();
    }
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  openNotifications(): void {
    if (!this.isClient) return;
    this.notificationService.getUnread().subscribe(
      notifs => {
        this.recentNotifs = notifs.slice(0, 5); // show last 5
      }
    );
  }

  markRead(id: number): void {
    this.notificationService.markAsRead(id).subscribe(() => {
      this.recentNotifs = this.recentNotifs.map(n =>
        n.id === id ? { ...n, read: true } : n
      );
      this.notificationService.refreshCount();
    });
  }

  markAllRead(): void {
    this.notificationService.markAllRead().subscribe(() => {
      this.recentNotifs = [];
      this.notificationService.refreshCount();
    });
  }

  logout(): void {
    this.authService.logout();
  }

  getInitials(): string {
    return this.fullName
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  private getRoleLabel(role: UserRole | null): string {
    const map: Record<string, string> = {
      ROLE_ADMIN:   'Admin',
      ROLE_MANAGER: 'Manager',
      ROLE_AGENT:   'Agent',
      ROLE_CLIENT:  'Client',
    };
    return role ? (map[role] ?? '') : '';
  }
}
