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
- 🗄️ MongoDB for data persistence
- ⚡ Asynchronous processing with Spring async

**Frontend:**
- 🅰️ Angular 17
- 📱 Responsive UI
- 💬 Interactive user interfaces

**Infrastructure:**
- 🐳 Docker containerization
- ☸️ Docker Compose orchestration
- 📦 Modern DevOps practices

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

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 11+ (if running locally without Docker)
- Node.js 16+ (for Angular development)
- MongoDB

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd okanetransfer
   ```

2. **Using Docker (Recommended)**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Open your browser and navigate to: `https://okanetransfert.netlify.app`
   - Login with admin credentials provided above

4. **Explore Different Roles**
   - Switch between different user roles to explore various features
   - Each role provides a tailored experience based on their responsibilities

---

## 📊 Use Case Diagram

The system supports comprehensive workflows across all operational levels, from customer transactions to system administration, with clear separation of concerns and role-based access control.

---

## 🔒 Security Highlights

- **Authentication:** JWT-based stateless authentication
- **Authorization:** Role-based access control (RBAC) with Spring Security
- **Data Protection:** Encrypted sensitive data in transit and at rest
- **Compliance:** Full KYC compliance framework integrated
- **API Security:** GPT API integration with secure endpoints

---

## 📱 Platform Compatibility

- ✅ Desktop Web Application
- ✅ Mobile-responsive design
- ✅ Modern browsers (Chrome, Firefox, Safari, Edge)
- ✅ Integration with Mobile Money ecosystems

---

## 🛠️ Technical Details

### Backend Architecture
- **Spring MVC** for request handling
- **JPA/Hibernate** for object-relational mapping
- **MongoDB** for flexible document storage
- **Spring Security** for comprehensive security management
- **Async Processing** for background tasks

### Frontend Architecture
- **Angular 17** single-page application
- **Responsive Design** for all device sizes
- **Component-based** architecture
- **TypeScript** for type safety
- **RxJS** for reactive programming

### Infrastructure
- **Docker** for containerization
- **Docker Compose** for orchestration
- **Microservices-ready** architecture
- **Stateless** design for horizontal scalability

---

## 📞 Support & Team

**Authors:**
- Y. Hamdis
- M. Driouche
- O. Mghira
- I. Toumi

**Supervisor:** A. Atlas

---

## 📝 License

This project is a academic enterprise solution for money transfer management.

---

## 🎯 Project Goals

OkaneTransfer was developed to demonstrate:
- Modern Java enterprise application development with Spring Framework
- Full-stack web application architecture
- Role-based access control implementation
- Asynchronous processing in distributed systems
- Integration with external APIs and services
- Docker containerization and DevOps practices

---

## 📧 Contact

For more information, questions, or support regarding OkaneTransfer, please refer to the official project documentation or contact the development team.

---

**Last Updated:** June 2026  
**Version:** 1.0.0  
**Status:** Production Ready ✅
