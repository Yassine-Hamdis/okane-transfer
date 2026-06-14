import { Component, OnInit } from '@angular/core';
import { CommonModule }      from '@angular/common';
import { RouterModule }      from '@angular/router';
import { NavbarComponent }   from '../navbar/navbar.component';
import { SidebarComponent }  from '../sidebar/sidebar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NavbarComponent,
    SidebarComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrls:   ['./shell.component.scss'],
})
export class ShellComponent implements OnInit {

  sidebarCollapsed = false;

  ngOnInit(): void {
    // Restore collapsed state from localStorage
    const saved = localStorage.getItem('okane_sidebar_collapsed');
    this.sidebarCollapsed = saved === 'true';
  }

  onToggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    localStorage.setItem(
      'okane_sidebar_collapsed',
      String(this.sidebarCollapsed)
    );
  }
}
