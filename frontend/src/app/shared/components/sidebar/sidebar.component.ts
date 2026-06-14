import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule }    from '@angular/common';
import { RouterModule }    from '@angular/router';
import { MatIconModule }   from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService }     from '../../../core/services/auth.service';
import { UserRole }        from '../../../core/models/user.model';

interface NavItem {
  label:  string;
  icon:   string;
  route:  string;
  roles:  UserRole[];
}

const NAV_ITEMS: NavItem[] = [
  // ── Admin ──────────────────────────────────────────────────────────────────
  { label: 'Dashboard',     icon: 'dashboard',      route: '/admin/dashboard',          roles: ['ROLE_ADMIN'] },
  { label: 'Users',         icon: 'people',         route: '/admin/users',              roles: ['ROLE_ADMIN'] },
  { label: 'Agencies',      icon: 'store',          route: '/admin/agencies',           roles: ['ROLE_ADMIN'] },
  { label: 'Countries',     icon: 'flag',           route: '/admin/countries',          roles: ['ROLE_ADMIN'] },
  { label: 'Currencies',    icon: 'attach_money',   route: '/admin/currencies',         roles: ['ROLE_ADMIN'] },
  { label: 'Corridors',     icon: 'alt_route',      route: '/admin/corridors',          roles: ['ROLE_ADMIN'] },
  { label: 'Exchange Rates',icon: 'currency_exchange', route: '/admin/exchange-rates',  roles: ['ROLE_ADMIN'] },
  { label: 'Fee Grids',     icon: 'grid_on',        route: '/admin/fee-grids',          roles: ['ROLE_ADMIN'] },
  { label: 'Transfers',     icon: 'swap_horiz',     route: '/admin/transfers',          roles: ['ROLE_ADMIN'] },
  { label: 'KYC / AML',    icon: 'verified_user',  route: '/admin/kyc',                roles: ['ROLE_ADMIN'] },
  { label: 'Mobile Money',  icon: 'phone_android',  route: '/admin/mobile-money',       roles: ['ROLE_ADMIN'] },
  { label: 'Audit Logs',    icon: 'history',        route: '/admin/audit',              roles: ['ROLE_ADMIN'] },
  { label: 'Chatbot',       icon: 'smart_toy',      route: '/admin/chatbot-escalations',roles: ['ROLE_ADMIN'] },

  // ── Manager ────────────────────────────────────────────────────────────────
  { label: 'Dashboard',     icon: 'dashboard',      route: '/manager/dashboard',        roles: ['ROLE_MANAGER'] },
  { label: 'My Agents',     icon: 'badge',          route: '/manager/agents',           roles: ['ROLE_MANAGER'] },
  { label: 'Transfers',     icon: 'swap_horiz',     route: '/manager/transfers',        roles: ['ROLE_MANAGER'] },

  // ── Agent ──────────────────────────────────────────────────────────────────
  { label: 'Dashboard',     icon: 'dashboard',      route: '/agent/dashboard',          roles: ['ROLE_AGENT'] },
  { label: 'Send Transfer', icon: 'send',           route: '/agent/send',               roles: ['ROLE_AGENT'] },
  { label: 'Payout',        icon: 'payments',       route: '/agent/payout',             roles: ['ROLE_AGENT'] },
  { label: 'My Transfers',  icon: 'receipt_long',   route: '/agent/my-transfers',       roles: ['ROLE_AGENT'] },
  { label: 'Cash Register', icon: 'point_of_sale',  route: '/agent/cash',               roles: ['ROLE_AGENT'] },

  // ── Client ─────────────────────────────────────────────────────────────────
  { label: 'Dashboard',     icon: 'dashboard',      route: '/client/dashboard',         roles: ['ROLE_CLIENT'] },
  { label: 'My Transfers',  icon: 'receipt_long',   route: '/client/transfers',         roles: ['ROLE_CLIENT'] },
  { label: 'Track Transfer',icon: 'track_changes',  route: '/client/track',             roles: ['ROLE_CLIENT'] },
  { label: 'Notifications', icon: 'notifications',  route: '/client/notifications',     roles: ['ROLE_CLIENT'] },
  { label: 'Profile',       icon: 'manage_accounts',route: '/client/profile',           roles: ['ROLE_CLIENT'] },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatRippleModule,
    MatTooltipModule,
  ],
  templateUrl: './sidebar.component.html',
  styleUrls:   ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit {

  @Input()  collapsed = false;
  @Output() toggleCollapse = new EventEmitter<void>();

  visibleItems: NavItem[] = [];
  role: UserRole | null   = null;
  roleName                = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.role     = this.authService.getRole();
    this.roleName = this.getRoleName(this.role);
    this.visibleItems = NAV_ITEMS.filter(
      item => this.role && item.roles.includes(this.role)
    );
  }

  private getRoleName(role: UserRole | null): string {
    const map: Record<string, string> = {
      ROLE_ADMIN:   'Administrator',
      ROLE_MANAGER: 'Manager',
      ROLE_AGENT:   'Agent',
      ROLE_CLIENT:  'Client',
    };
    return role ? (map[role] ?? role) : '';
  }

  onToggle(): void {
    this.toggleCollapse.emit();
  }
}
