import { Injectable }      from '@angular/core';
import { HttpClient }      from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment }     from '../../../environments/environment';
import { ApiResponse }     from '../models/api-response.model';
import {
  ChatbotStartRequest,
  ChatbotStartResponse,
  ChatbotMessageRequest,
  ChatbotMessageResponse,
  EscalatedConversation,
} from '../models/chatbot.model';

@Injectable({ providedIn: 'root' })
export class ChatbotService {

  private readonly base      = `${environment.apiUrl}/chatbot`;
  private readonly adminBase = `${environment.apiUrl}/admin/chatbot`;

  constructor(private http: HttpClient) {}

  // ── Public / any user ───────────────────────────────────────────────────────

  startConversation(request: ChatbotStartRequest): Observable<ChatbotStartResponse> {
    return this.http
      .post<ApiResponse<ChatbotStartResponse>>(`${this.base}/start`, request)
      .pipe(map(res => res.data!));
  }

  sendMessage(request: ChatbotMessageRequest): Observable<ChatbotMessageResponse> {
    return this.http
      .post<ApiResponse<ChatbotMessageResponse>>(`${this.base}/message`, request)
      .pipe(map(res => res.data!));
  }

  getMessages(sessionId: string): Observable<ChatbotMessageResponse[]> {
    return this.http
      .get<ApiResponse<ChatbotMessageResponse[]>>(
        `${this.base}/${sessionId}/messages`
      )
      .pipe(map(res => res.data!));
  }

  closeConversation(sessionId: string): Observable<string> {
    return this.http
      .patch<ApiResponse<void>>(`${this.base}/${sessionId}/close`, {})
      .pipe(map(res => res.message));
  }

  // ── Admin only ──────────────────────────────────────────────────────────────

  getEscalated(): Observable<EscalatedConversation[]> {
    return this.http
      .get<ApiResponse<EscalatedConversation[]>>(`${this.adminBase}/escalated`)
      .pipe(map(res => res.data!));
  }
}
