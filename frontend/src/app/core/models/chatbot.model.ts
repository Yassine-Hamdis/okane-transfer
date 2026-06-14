export type ChatbotLanguage = 'FR' | 'EN' | 'AR';

export type ChatbotIntent =
  | 'FAQ'
  | 'TRACKING'
  | 'ESCALATION'
  | 'UNKNOWN';

export type ChatbotSender = 'BOT' | 'USER';

export interface ChatbotStartRequest {
  language: ChatbotLanguage;
}

export interface ChatbotStartResponse {
  conversationId: number;
  sessionId: string;
  welcomeMessage: string;
}

export interface ChatbotMessageRequest {
  sessionId: string;
  content: string;
}

export interface ChatbotMessageResponse {
  content: string;
  sender: ChatbotSender;
  intent: ChatbotIntent;
  escalated: boolean;
}

// Local UI message — adds user messages too (sender can be USER or BOT)
export interface ChatMessage {
  content: string;
  sender: ChatbotSender;
  intent?: ChatbotIntent;
  escalated?: boolean;
  timestamp: Date;
}

export interface EscalatedConversation {
  sessionId: string;
  conversationId: number;
  language: ChatbotLanguage;
  startedAt: string;
  lastMessageAt: string;
  messageCount: number;
}
