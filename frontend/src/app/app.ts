import { Component }          from '@angular/core';
import { CommonModule }       from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter }             from 'rxjs';

import { ChatbotWidgetComponent } from './chatbot/chatbot-widget/chatbot-widget.component';
import { AuthService }            from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, ChatbotWidgetComponent],
  template: `
    <router-outlet />
    <app-chatbot-widget *ngIf="showChatbot" />
  `,
})
export class AppComponent {

  showChatbot = false;

  constructor(
    private authService: AuthService,
    private router:      Router,
  ) {
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        const url: string = e.urlAfterRedirects;
        // Show chatbot if logged in OR if on public track page
        this.showChatbot =
          this.authService.isLoggedIn() ||
          url.startsWith('/track');
      });
  }
}
