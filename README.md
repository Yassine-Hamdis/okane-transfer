# OkaneTransfer

## 🌐 Redefining Money Transfer

OkaneTransfer is a **secured and innovative web monolithic platform** designed to revolutionize the money transfer industry. Built with modern enterprise technologies, it provides a comprehensive solution for managing all aspects of transfer agencies from end-to-end.

### Live Demo
**URL:** [https://okanetransfert.netlify.app](https://okanetransfert.netlify.app)

---

## 🔐 Admin Access

To access the administrative dashboard, use the following credentials:

```
Email: admin@okanetransfer.com
Password: Admin@1234
```

---

## 📋 Overview

OkaneTransfer addresses critical business needs and technological challenges in the transfer industry:

### Business Needs (Métier)
- Complete end-to-end management of a transfer agency
- Front-office counters and cashier operations
- Automated accounting reconciliation
- Real-time multi-currency calculations
- Regulatory compliance (KYC verification)

### Technological Excellence
- **Strict Architecture** without abstraction layers
- **Zero Spring Boot Constraint** - Complete manual configuration
- Full Spring MVC, Security, and JPA integration
- Robust layered architecture with strict separation of concerns
- External AI Integration (GPT API)
- Asynchronous Mobile Money Ecosystem

---

## 🏗️ System Architecture

### Technology Stack

**Backend:**
- ☕ Java Spring Framework (Spring MVC)
- 🛡️ Spring Security with JWT Stateless authentication
- 💾 JPA/Hibernate ORM
- ⚡ Asynchronous processing with Spring async

**Frontend:**
- 🅰️ Angular 17
- 📱 Responsive UI
- 💬 Interactive user interfaces

**Infrastructure:**
- AWS
- RDS
- Netlify

---

## 👥 User Roles & Permissions

The system implements role-based access control (RBAC) with four main profiles:

| Role | Spring Role | Application Space | Key Responsibilities |
|------|------------|-------------------|----------------------|
| **Administrator** | `ROLE_ADMIN` | Global Configuration | System configuration, tariff grids, network supervision, KYC arbitration |
| **Manager** | `ROLE_MANAGER` | Agency Supervision | Agency monitoring, operation validation, sensitive operations, team management |
| **Agent** | `ROLE_AGENT` | Front-Office | Fund transfer input, identity verification, cash desk management |
| **Client** | `ROLE_CLIENT` | Self-Service | Real-time tracking, transaction history, profile management |

### Security Features
- Strict authorization via `@PreAuthorize` annotation
- JWT Stateless authentication
- Each actor is restricted to their application space
- Comprehensive security validation on all operations

---

## ✨ Key Features

### 1. **Front-Office Operations**
   - Complete customer transaction handling
   - Identity verification and KYC compliance
   - Cash desk management
   - Real-time transaction processing

### 2. **Financial Management**
   - Multi-currency support with real-time conversion
   - Automated accounting reconciliation
   - Tariff configuration and management
   - Financial reporting and auditing

### 3. **Agency Supervision**
   - Real-time agency performance monitoring
   - Sensitive operation validation
   - Team and resource management
   - Compliance tracking

### 4. **Self-Service Portal**
   - Transaction history and tracking
   - Real-time transaction status
   - Profile management
   - Personal settings

### 5. **Administrative Controls**
   - System-wide configuration
   - Tariff grid management
   - Network supervision
   - KYC arbitration and compliance

### 6. **AI Integration**
   - External API integration (GPT)
   - Intelligent decision support
   - Enhanced user experience

