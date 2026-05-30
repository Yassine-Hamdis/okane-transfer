package com.okanetransfer.security;

import com.okanetransfer.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation that wraps the User entity
 * and exposes id and agencyId for use in controllers via @AuthenticationPrincipal.
 *
 * Drop-in replacement for the plain Spring User object returned by
 * CustomUserDetailsService — update that service to return this instead.
 */
public class CustomUserDetails implements UserDetails {

    private final Long   id;
    private final Long   agencyId;   // null for ROLE_ADMIN and ROLE_CLIENT
    private final String email;
    private final String password;
    private final String role;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.id       = user.getId();
        this.agencyId = (user.getAgency() != null) ? user.getAgency().getId() : null;
        this.email    = user.getEmail();
        this.password = user.getPassword();
        this.role     = user.getRole().name();
        this.active   = user.isActive();
    }

    // ── Extra fields used by your controllers ──────────────────────────────────

    public Long getId()       { return id; }
    public Long getAgencyId() { return agencyId; }

    // ── UserDetails contract ───────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String  getPassword()           { return password; }
    @Override public String  getUsername()            { return email; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return active; }
}
