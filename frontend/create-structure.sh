#!/bin/bash

# ── CORE ──────────────────────────────────────────────────────────────────────
mkdir -p src/app/core/interceptors
mkdir -p src/app/core/guards
mkdir -p src/app/core/services
mkdir -p src/app/core/models

touch src/app/core/interceptors/auth.interceptor.ts

touch src/app/core/guards/auth.guard.ts
touch src/app/core/guards/admin.guard.ts
touch src/app/core/guards/manager.guard.ts
touch src/app/core/guards/agent.guard.ts

touch src/app/core/services/auth.service.ts
touch src/app/core/services/user.service.ts
touch src/app/core/services/agency.service.ts
touch src/app/core/services/country.service.ts
touch src/app/core/services/currency.service.ts
touch src/app/core/services/corridor.service.ts
touch src/app/core/services/exchange-rate.service.ts
touch src/app/core/services/fee-grid.service.ts
touch src/app/core/services/transfer.service.ts
touch src/app/core/services/cash.service.ts
touch src/app/core/services/kyc.service.ts
touch src/app/core/services/mobile-money.service.ts
touch src/app/core/services/notification.service.ts
touch src/app/core/services/client.service.ts
touch src/app/core/services/audit.service.ts
touch src/app/core/services/chatbot.service.ts

touch src/app/core/models/api-response.model.ts
touch src/app/core/models/user.model.ts
touch src/app/core/models/agency.model.ts
touch src/app/core/models/country.model.ts
touch src/app/core/models/currency.model.ts
touch src/app/core/models/corridor.model.ts
touch src/app/core/models/exchange-rate.model.ts
touch src/app/core/models/fee-grid.model.ts
touch src/app/core/models/transfer.model.ts
touch src/app/core/models/cash.model.ts
touch src/app/core/models/kyc.model.ts
touch src/app/core/models/mobile-money.model.ts
touch src/app/core/models/notification.model.ts
touch src/app/core/models/chatbot.model.ts

# ── SHARED ────────────────────────────────────────────────────────────────────
mkdir -p src/app/shared/components/navbar
mkdir -p src/app/shared/components/sidebar
mkdir -p src/app/shared/components/status-badge
mkdir -p src/app/shared/components/confirm-dialog
mkdir -p src/app/shared/components/loading-spinner
mkdir -p src/app/shared/pipes

touch src/app/shared/components/navbar/navbar.component.ts
touch src/app/shared/components/navbar/navbar.component.html
touch src/app/shared/components/navbar/navbar.component.scss

touch src/app/shared/components/sidebar/sidebar.component.ts
touch src/app/shared/components/sidebar/sidebar.component.html
touch src/app/shared/components/sidebar/sidebar.component.scss

touch src/app/shared/components/status-badge/status-badge.component.ts
touch src/app/shared/components/status-badge/status-badge.component.html
touch src/app/shared/components/status-badge/status-badge.component.scss

touch src/app/shared/components/confirm-dialog/confirm-dialog.component.ts
touch src/app/shared/components/confirm-dialog/confirm-dialog.component.html

touch src/app/shared/components/loading-spinner/loading-spinner.component.ts
touch src/app/shared/components/loading-spinner/loading-spinner.component.html
touch src/app/shared/components/loading-spinner/loading-spinner.component.scss

touch src/app/shared/pipes/transfer-status.pipe.ts
touch src/app/shared/pipes/currency-format.pipe.ts

# ── AUTH FEATURE ──────────────────────────────────────────────────────────────
mkdir -p src/app/features/auth/login
mkdir -p src/app/features/auth/register
mkdir -p src/app/features/auth/verify-2fa
mkdir -p src/app/features/auth/change-password

touch src/app/features/auth/login/login.component.ts
touch src/app/features/auth/login/login.component.html
touch src/app/features/auth/login/login.component.scss

touch src/app/features/auth/register/register.component.ts
touch src/app/features/auth/register/register.component.html
touch src/app/features/auth/register/register.component.scss

touch src/app/features/auth/verify-2fa/verify-two-factor.component.ts
touch src/app/features/auth/verify-2fa/verify-two-factor.component.html
touch src/app/features/auth/verify-2fa/verify-two-factor.component.scss

touch src/app/features/auth/change-password/change-password.component.ts
touch src/app/features/auth/change-password/change-password.component.html
touch src/app/features/auth/change-password/change-password.component.scss

# ── ADMIN FEATURE ─────────────────────────────────────────────────────────────
mkdir -p src/app/features/admin/dashboard
mkdir -p src/app/features/admin/users/user-list
mkdir -p src/app/features/admin/users/user-form
mkdir -p src/app/features/admin/users/user-detail
mkdir -p src/app/features/admin/agencies/agency-list
mkdir -p src/app/features/admin/agencies/agency-form
mkdir -p src/app/features/admin/agencies/agency-detail
mkdir -p src/app/features/admin/countries
mkdir -p src/app/features/admin/currencies
mkdir -p src/app/features/admin/corridors
mkdir -p src/app/features/admin/exchange-rates
mkdir -p src/app/features/admin/fee-grids
mkdir -p src/app/features/admin/transfers
mkdir -p src/app/features/admin/kyc/kyc-list
mkdir -p src/app/features/admin/kyc/kyc-review
mkdir -p src/app/features/admin/mobile-money
mkdir -p src/app/features/admin/chatbot-escalations
mkdir -p src/app/features/admin/audit-logs

touch src/app/features/admin/dashboard/admin-dashboard.component.ts
touch src/app/features/admin/dashboard/admin-dashboard.component.html
touch src/app/features/admin/dashboard/admin-dashboard.component.scss

touch src/app/features/admin/users/user-list/user-list.component.ts
touch src/app/features/admin/users/user-list/user-list.component.html
touch src/app/features/admin/users/user-list/user-list.component.scss
touch src/app/features/admin/users/user-form/user-form.component.ts
touch src/app/features/admin/users/user-form/user-form.component.html
touch src/app/features/admin/users/user-form/user-form.component.scss
touch src/app/features/admin/users/user-detail/user-detail.component.ts
touch src/app/features/admin/users/user-detail/user-detail.component.html
touch src/app/features/admin/users/user-detail/user-detail.component.scss

touch src/app/features/admin/agencies/agency-list/agency-list.component.ts
touch src/app/features/admin/agencies/agency-list/agency-list.component.html
touch src/app/features/admin/agencies/agency-list/agency-list.component.scss
touch src/app/features/admin/agencies/agency-form/agency-form.component.ts
touch src/app/features/admin/agencies/agency-form/agency-form.component.html
touch src/app/features/admin/agencies/agency-form/agency-form.component.scss
touch src/app/features/admin/agencies/agency-detail/agency-detail.component.ts
touch src/app/features/admin/agencies/agency-detail/agency-detail.component.html
touch src/app/features/admin/agencies/agency-detail/agency-detail.component.scss

touch src/app/features/admin/countries/country-list.component.ts
touch src/app/features/admin/countries/country-list.component.html
touch src/app/features/admin/countries/country-list.component.scss

touch src/app/features/admin/currencies/currency-list.component.ts
touch src/app/features/admin/currencies/currency-list.component.html
touch src/app/features/admin/currencies/currency-list.component.scss

touch src/app/features/admin/corridors/corridor-list.component.ts
touch src/app/features/admin/corridors/corridor-list.component.html
touch src/app/features/admin/corridors/corridor-list.component.scss

touch src/app/features/admin/exchange-rates/exchange-rate-list.component.ts
touch src/app/features/admin/exchange-rates/exchange-rate-list.component.html
touch src/app/features/admin/exchange-rates/exchange-rate-list.component.scss

touch src/app/features/admin/fee-grids/fee-grid-list.component.ts
touch src/app/features/admin/fee-grids/fee-grid-list.component.html
touch src/app/features/admin/fee-grids/fee-grid-list.component.scss

touch src/app/features/admin/transfers/admin-transfer-list.component.ts
touch src/app/features/admin/transfers/admin-transfer-list.component.html
touch src/app/features/admin/transfers/admin-transfer-list.component.scss

touch src/app/features/admin/kyc/kyc-list/kyc-list.component.ts
touch src/app/features/admin/kyc/kyc-list/kyc-list.component.html
touch src/app/features/admin/kyc/kyc-list/kyc-list.component.scss
touch src/app/features/admin/kyc/kyc-review/kyc-review.component.ts
touch src/app/features/admin/kyc/kyc-review/kyc-review.component.html
touch src/app/features/admin/kyc/kyc-review/kyc-review.component.scss

touch src/app/features/admin/mobile-money/mobile-money-list.component.ts
touch src/app/features/admin/mobile-money/mobile-money-list.component.html
touch src/app/features/admin/mobile-money/mobile-money-list.component.scss

touch src/app/features/admin/chatbot-escalations/chatbot-escalations.component.ts
touch src/app/features/admin/chatbot-escalations/chatbot-escalations.component.html
touch src/app/features/admin/chatbot-escalations/chatbot-escalations.component.scss

touch src/app/features/admin/audit-logs/audit-log-list.component.ts
touch src/app/features/admin/audit-logs/audit-log-list.component.html
touch src/app/features/admin/audit-logs/audit-log-list.component.scss

# ── MANAGER FEATURE ───────────────────────────────────────────────────────────
mkdir -p src/app/features/manager/dashboard
mkdir -p src/app/features/manager/agents
mkdir -p src/app/features/manager/transfers

touch src/app/features/manager/dashboard/manager-dashboard.component.ts
touch src/app/features/manager/dashboard/manager-dashboard.component.html
touch src/app/features/manager/dashboard/manager-dashboard.component.scss

touch src/app/features/manager/agents/agent-list.component.ts
touch src/app/features/manager/agents/agent-list.component.html
touch src/app/features/manager/agents/agent-list.component.scss

touch src/app/features/manager/transfers/manager-transfer-list.component.ts
touch src/app/features/manager/transfers/manager-transfer-list.component.html
touch src/app/features/manager/transfers/manager-transfer-list.component.scss

# ── AGENT FEATURE ─────────────────────────────────────────────────────────────
mkdir -p src/app/features/agent/dashboard
mkdir -p src/app/features/agent/send-transfer
mkdir -p src/app/features/agent/payout
mkdir -p src/app/features/agent/my-transfers
mkdir -p src/app/features/agent/cash-register/cash-overview
mkdir -p src/app/features/agent/cash-register/operations-today
mkdir -p src/app/features/agent/cash-register/close-register

touch src/app/features/agent/dashboard/agent-dashboard.component.ts
touch src/app/features/agent/dashboard/agent-dashboard.component.html
touch src/app/features/agent/dashboard/agent-dashboard.component.scss

touch src/app/features/agent/send-transfer/send-transfer.component.ts
touch src/app/features/agent/send-transfer/send-transfer.component.html
touch src/app/features/agent/send-transfer/send-transfer.component.scss

touch src/app/features/agent/payout/payout.component.ts
touch src/app/features/agent/payout/payout.component.html
touch src/app/features/agent/payout/payout.component.scss

touch src/app/features/agent/my-transfers/agent-transfer-list.component.ts
touch src/app/features/agent/my-transfers/agent-transfer-list.component.html
touch src/app/features/agent/my-transfers/agent-transfer-list.component.scss

touch src/app/features/agent/cash-register/cash-overview/cash-overview.component.ts
touch src/app/features/agent/cash-register/cash-overview/cash-overview.component.html
touch src/app/features/agent/cash-register/cash-overview/cash-overview.component.scss

touch src/app/features/agent/cash-register/operations-today/operations-today.component.ts
touch src/app/features/agent/cash-register/operations-today/operations-today.component.html
touch src/app/features/agent/cash-register/operations-today/operations-today.component.scss

touch src/app/features/agent/cash-register/close-register/close-register.component.ts
touch src/app/features/agent/cash-register/close-register/close-register.component.html
touch src/app/features/agent/cash-register/close-register/close-register.component.scss

# ── CLIENT FEATURE ────────────────────────────────────────────────────────────
mkdir -p src/app/features/client/dashboard
mkdir -p src/app/features/client/my-transfers
mkdir -p src/app/features/client/track-transfer
mkdir -p src/app/features/client/notifications
mkdir -p src/app/features/client/profile

touch src/app/features/client/dashboard/client-dashboard.component.ts
touch src/app/features/client/dashboard/client-dashboard.component.html
touch src/app/features/client/dashboard/client-dashboard.component.scss

touch src/app/features/client/my-transfers/client-transfer-list.component.ts
touch src/app/features/client/my-transfers/client-transfer-list.component.html
touch src/app/features/client/my-transfers/client-transfer-list.component.scss

touch src/app/features/client/track-transfer/track-transfer.component.ts
touch src/app/features/client/track-transfer/track-transfer.component.html
touch src/app/features/client/track-transfer/track-transfer.component.scss

touch src/app/features/client/notifications/notifications.component.ts
touch src/app/features/client/notifications/notifications.component.html
touch src/app/features/client/notifications/notifications.component.scss

touch src/app/features/client/profile/profile.component.ts
touch src/app/features/client/profile/profile.component.html
touch src/app/features/client/profile/profile.component.scss

# ── CHATBOT WIDGET ────────────────────────────────────────────────────────────
mkdir -p src/app/chatbot/chatbot-widget

touch src/app/chatbot/chatbot-widget/chatbot-widget.component.ts
touch src/app/chatbot/chatbot-widget/chatbot-widget.component.html
touch src/app/chatbot/chatbot-widget/chatbot-widget.component.scss

# ── ENVIRONMENTS ──────────────────────────────────────────────────────────────
mkdir -p src/environments
touch src/environments/environment.ts
touch src/environments/environment.prod.ts

echo "✅ Project structure created successfully!"
