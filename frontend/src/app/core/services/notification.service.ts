import { Injectable, OnDestroy }         from '@angular/core';
import { HttpClient }                    from '@angular/common/http';
import {
  Observable, map, BehaviorSubject,
  interval, switchMap, Subscription,
} from 'rxjs';

import { environment }                   from '../../../environments/environment';
import { ApiResponse }                   from '../models/api-response.model';
import {
  Notification,
  UnreadCountResponse,
} from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {

  private readonly base = `${environment.apiUrl}/client/notifications`;

  readonly unreadCount$ = new BehaviorSubject<number>(0);

  private pollingSub: Subscription | null = null;

  constructor(private http: HttpClient) {}

  ngOnDestroy(): void {
    this.stopPolling();
  }

  // ── GET ─────────────────────────────────────────────────────────────────────

  getAll(): Observable<Notification[]> {
    return this.http
      .get<ApiResponse<Notification[]>>(this.base)
      .pipe(map(res => res.data!));
  }

  getUnread(): Observable<Notification[]> {
    return this.http
      .get<ApiResponse<Notification[]>>(`${this.base}/unread`)
      .pipe(map(res => res.data!));
  }

  getUnreadCount(): Observable<number> {
    return this.http
      .get<ApiResponse<UnreadCountResponse>>(`${this.base}/unread/count`)
      .pipe(map(res => res.data!.unreadCount));
  }

  // ── PATCH ───────────────────────────────────────────────────────────────────

  markAsRead(id: number): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${id}/read`, {})
      .pipe(map(res => res.message));
  }

  markAllRead(): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/read-all`, {})
      .pipe(map(res => res.message));
  }

  // ── Polling ──────────────────────────────────────────────────────────────────

  startPolling(): void {
    // Prevent duplicate subscriptions
    if (this.pollingSub) return;

    this.refreshCount();

    this.pollingSub = interval(30_000)
      .pipe(switchMap(() => this.getUnreadCount()))
      .subscribe(count => this.unreadCount$.next(count));
  }

  stopPolling(): void {
    this.pollingSub?.unsubscribe();
    this.pollingSub = null;
  }

  refreshCount(): void {
    this.getUnreadCount()
      .subscribe(count => this.unreadCount$.next(count));
  }
}
