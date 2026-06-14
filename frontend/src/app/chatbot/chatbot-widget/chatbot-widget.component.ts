import {
  Component, OnInit, OnDestroy,
  ViewChild, ElementRef, AfterViewChecked,
} from '@angular/core';
import { CommonModule }        from '@angular/common';
import {
  ReactiveFormsModule, FormBuilder, FormGroup, Validators,
} from '@angular/forms';
import { MatIconModule }       from '@angular/material/icon';
import { MatButtonModule }     from '@angular/material/button';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatSelectModule }     from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule }    from '@angular/material/tooltip';

import { ChatbotService }      from '../../core/services/chatbot.service';
import {
  ChatMessage,
  ChatbotLanguage,
  ChatbotStartResponse,
} from '../../core/models/chatbot.model';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './chatbot-widget.component.html',
  styleUrls:   ['./chatbot-widget.component.scss'],
})
export class ChatbotWidgetComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesEnd') messagesEnd!: ElementRef;

  // ── State ─────────────────────────────────────────────────────────────────────
  isOpen       = false;
  isStarted    = false;
  isTyping     = false;
  sessionId    = '';
  messages:    ChatMessage[] = [];
  messageForm: FormGroup;

  // ── Language ──────────────────────────────────────────────────────────────────
  selectedLang: ChatbotLanguage = 'EN';
  readonly languages: { value: ChatbotLanguage; label: string; flag: string }[] = [
    { value: 'EN', label: 'English', flag: '🇬🇧' },
    { value: 'FR', label: 'Français', flag: '🇫🇷' },
    { value: 'AR', label: 'العربية', flag: '🇲🇦' },
  ];

  private shouldScroll = false;

  constructor(
    private chatbotService: ChatbotService,
    private fb:             FormBuilder,
  ) {
    this.messageForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(1)]],
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {
    if (this.sessionId) {
      this.chatbotService.closeConversation(this.sessionId).subscribe();
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  // ── Open / Close ──────────────────────────────────────────────────────────────

  toggleOpen(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen && !this.isStarted) {
      this.startConversation();
    }
    if (this.isOpen) {
      this.shouldScroll = true;
    }
  }

  close(): void {
    this.isOpen = false;
  }

  // ── Start conversation ────────────────────────────────────────────────────────

  startConversation(): void {
    this.isTyping = true;
    this.chatbotService.startConversation({ language: this.selectedLang })
      .subscribe({
        next: (res: ChatbotStartResponse) => {
          this.sessionId = res.sessionId;
          this.isStarted = true;
          this.isTyping  = false;
          this.addBotMessage(res.welcomeMessage);
        },
        error: () => {
          this.isTyping = false;
          this.addBotMessage('Sorry, I could not start. Please try again.');
        },
      });
  }

  changeLanguage(lang: ChatbotLanguage): void {
    this.selectedLang = lang;
    if (this.sessionId) {
      // Close old session and start new one with new language
      this.chatbotService.closeConversation(this.sessionId).subscribe();
    }
    this.isStarted  = false;
    this.sessionId  = '';
    this.messages   = [];
    this.startConversation();
  }

  // ── Send message ──────────────────────────────────────────────────────────────

  onSend(): void {
    if (this.messageForm.invalid || !this.sessionId) return;

    const content = this.messageForm.value.content.trim();
    if (!content) return;

    // Add user message immediately
    this.messages.push({
      content,
      sender:    'USER',
      timestamp: new Date(),
    });
    this.messageForm.reset();
    this.shouldScroll = true;

    // Show typing indicator
    this.isTyping = true;

    this.chatbotService.sendMessage({ sessionId: this.sessionId, content })
      .subscribe({
        next: response => {
          this.isTyping = false;
          this.addBotMessage(
            response.content,
            response.intent,
            response.escalated,
          );

          if (response.escalated) {
            this.addBotMessage(
              '🔔 Connecting you to a human agent... Please wait.',
            );
          }
        },
        error: () => {
          this.isTyping = false;
          this.addBotMessage('Sorry, I encountered an error. Please try again.');
        },
      });
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSend();
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private addBotMessage(
    content:  string,
    intent?:  any,
    escalated = false,
  ): void {
    this.messages.push({
      content,
      sender:    'BOT',
      intent,
      escalated,
      timestamp: new Date(),
    });
    this.shouldScroll = true;
  }

  private scrollToBottom(): void {
    try {
      this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
    } catch (_) {}
  }

  getLangFlag(): string {
    return this.languages.find(l => l.value === this.selectedLang)?.flag ?? '🌐';
  }

  isRtl(): boolean {
    return this.selectedLang === 'AR';
  }
}
