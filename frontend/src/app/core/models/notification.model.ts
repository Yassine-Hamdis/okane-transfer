export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH';

export interface Notification {
  id: number;
  title: string;
  message: string;
  channel: NotificationChannel;
  recipientAddress: string | null;
  read: boolean;
  readAt: string | null;
  sentAt: string;
  transferId: number | null;
}

export interface UnreadCountResponse {
  unreadCount: number;
}
