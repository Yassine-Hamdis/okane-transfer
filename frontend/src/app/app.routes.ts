import { Routes }        from '@angular/router';
import { authGuard }     from './core/guards/auth.guard';
import { adminGuard }    from './core/guards/admin.guard';
import { managerGuard }  from './core/guards/manager.guard';
import { agentGuard }    from './core/guards/agent.guard';

export const routes: Routes = [

  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // ── Public ──────────────────────────────────────────────────────────────────
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'verify-2fa',
    loadComponent: () =>
      import('./features/auth/verify-2fa/verify-two-factor.component').then(m => m.VerifyTwoFactorComponent),
  },
  {
    path: 'change-password',
    loadComponent: () =>
      import('./features/auth/change-password/change-password.component').then(m => m.ChangePasswordComponent),
  },
  {
    path: 'track/:code',
    loadComponent: () =>
      import('./features/client/track-transfer/track-transfer.component').then(m => m.TrackTransferComponent),
  },
  {
    path: 'track/code',
    loadComponent: () =>
        import('./features/client/track-transfer/track-transfer.component')
        .then(m => m.TrackTransferComponent),
  },
  {
  path: 'track',
  loadComponent: () =>
    import('./features/client/track-transfer/track-transfer.component')
      .then(m => m.TrackTransferComponent),
  },


  // ── Admin (Shell wraps all children) ────────────────────────────────────────
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',      loadComponent: () => import('./features/admin/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'users',          loadComponent: () => import('./features/admin/users/user-list/user-list.component').then(m => m.UserListComponent) },
      { path: 'users/new',      loadComponent: () => import('./features/admin/users/user-form/user-form.component').then(m => m.UserFormComponent) },
      { path: 'users/:id',      loadComponent: () => import('./features/admin/users/user-detail/user-detail.component').then(m => m.UserDetailComponent) },
      { path: 'agencies',       loadComponent: () => import('./features/admin/agencies/agency-list/agency-list.component').then(m => m.AgencyListComponent) },
      { path: 'agencies/new',   loadComponent: () => import('./features/admin/agencies/agency-form/agency-form.component').then(m => m.AgencyFormComponent) },
      { path: 'agencies/:id',   loadComponent: () => import('./features/admin/agencies/agency-detail/agency-detail.component').then(m => m.AgencyDetailComponent) },
      { path: 'countries',      loadComponent: () => import('./features/admin/countries/country-list.component').then(m => m.CountryListComponent) },
      { path: 'currencies',     loadComponent: () => import('./features/admin/currencies/currency-list.component').then(m => m.CurrencyListComponent) },
      { path: 'corridors',      loadComponent: () => import('./features/admin/corridors/corridor-list.component').then(m => m.CorridorListComponent) },
      { path: 'exchange-rates', loadComponent: () => import('./features/admin/exchange-rates/exchange-rate-list.component').then(m => m.ExchangeRateListComponent) },
      { path: 'fee-grids',      loadComponent: () => import('./features/admin/fee-grids/fee-grid-list.component').then(m => m.FeeGridListComponent) },
      { path: 'transfers',      loadComponent: () => import('./features/admin/transfers/admin-transfer-list.component').then(m => m.AdminTransferListComponent) },
      { path: 'kyc',            loadComponent: () => import('./features/admin/kyc/kyc-list/kyc-list.component').then(m => m.KycListComponent) },
      { path: 'kyc/:id',        loadComponent: () => import('./features/admin/kyc/kyc-review/kyc-review.component').then(m => m.KycReviewComponent) },
      { path: 'mobile-money',   loadComponent: () => import('./features/admin/mobile-money/mobile-money-list.component').then(m => m.MobileMoneyListComponent) },
      { path: 'audit',          loadComponent: () => import('./features/admin/audit-logs/audit-log-list.component').then(m => m.AuditLogListComponent) },
      { path: 'chatbot-escalations', loadComponent: () => import('./features/admin/chatbot-escalations/chatbot-escalations.component').then(m => m.ChatbotEscalationsComponent) },
    ],
  },

  // ── Manager ─────────────────────────────────────────────────────────────────
  {
    path: 'manager',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/manager/dashboard/manager-dashboard.component').then(m => m.ManagerDashboardComponent) },
      { path: 'agents',    loadComponent: () => import('./features/manager/agents/agent-list.component').then(m => m.AgentListComponent) },
      { path: 'transfers', loadComponent: () => import('./features/manager/transfers/manager-transfer-list.component').then(m => m.ManagerTransferListComponent) },
    ],
  },

  // ── Agent ────────────────────────────────────────────────────────────────────
  {
    path: 'agent',
    canActivate: [agentGuard],
    loadComponent: () =>
      import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',    loadComponent: () => import('./features/agent/dashboard/agent-dashboard.component').then(m => m.AgentDashboardComponent) },
      { path: 'send',         loadComponent: () => import('./features/agent/send-transfer/send-transfer.component').then(m => m.SendTransferComponent) },
      { path: 'payout',       loadComponent: () => import('./features/agent/payout/payout.component').then(m => m.PayoutComponent) },
      { path: 'my-transfers', loadComponent: () => import('./features/agent/my-transfers/agent-transfer-list.component').then(m => m.AgentTransferListComponent) },
      { path: 'cash',         loadComponent: () => import('./features/agent/cash-register/cash-overview/cash-overview.component').then(m => m.CashOverviewComponent) },
      { path: 'cash/today',   loadComponent: () => import('./features/agent/cash-register/operations-today/operations-today.component').then(m => m.OperationsTodayComponent) },
      { path: 'cash/close',   loadComponent: () => import('./features/agent/cash-register/close-register/close-register.component').then(m => m.CloseRegisterComponent) },
    ],
  },

  // ── Client ───────────────────────────────────────────────────────────────────
  {
    path: 'client',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',       loadComponent: () => import('./features/client/dashboard/client-dashboard.component').then(m => m.ClientDashboardComponent) },
      { path: 'transfers',       loadComponent: () => import('./features/client/my-transfers/client-transfer-list.component').then(m => m.ClientTransferListComponent) },
      { path: 'track',           loadComponent: () => import('./features/client/track-transfer/track-transfer.component').then(m => m.TrackTransferComponent) },
      { path: 'notifications',   loadComponent: () => import('./features/client/notifications/notifications.component').then(m => m.NotificationsComponent) },
      { path: 'profile',         loadComponent: () => import('./features/client/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'change-password', loadComponent: () => import('./features/auth/change-password/change-password.component').then(m => m.ChangePasswordComponent) },
    ],
  },

  { path: '**', redirectTo: 'login' },
];
