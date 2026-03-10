# User Service - MicroPC Shop

## Overview
User Service là microservice quản lý thông tin người dùng trong hệ thống Electronics Shop, tích hợp với Keycloak OAuth2 để xác thực và phân quyền.

## Tech Stack
- **Framework**: Spring Boot 4.0.3
- **Database**: MongoDB Atlas
- **Authentication**: Keycloak OAuth2
- **Security**: Spring Security + OAuth2 Resource Server
- **Java Version**: 21
- **Build Tool**: Maven

## Features
✅ Tự động tạo profile khi user đăng nhập lần đầu  
✅ Quản lý thông tin cá nhân  
✅ Quản lý địa chỉ giao hàng (tối đa 5 địa chỉ)  
✅ Theo dõi độ hoàn thiện profile  
✅ Hệ thống điểm thưởng (Loyalty Points)  
✅ Quản lý preferences người dùng  
✅ Soft delete users  
✅ Admin endpoints  

## Project Structure
```
src/main/java/com/mss/user_service/
├── config/                      # Security & CORS configuration
│   ├── SecurityConfig.java
│   ├── KeycloakJwtAuthenticationConverter.java
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── controller/                  # REST API Controllers
│   └── UserProfileController.java
├── dto/                        # Data Transfer Objects
│   ├── AddressDto.java
│   └── UserPreferencesDto.java
├── entity/                     # MongoDB Entities
│   ├── UserProfile.java
│   ├── Address.java
│   └── UserPreferences.java
├── enums/                      # Enumerations
│   ├── UserStatus.java
│   └── Gender.java
├── exceptions/                 # Custom Exceptions
│   ├── UserNotFoundException.java
│   ├── ProfileAlreadyCompletedException.java
│   ├── InvalidAddressIndexException.java
│   ├── MaxAddressLimitException.java
│   └── GlobalExceptionHandler.java
├── mapper/                     # Entity-DTO Mappers
│   └── UserProfileMapper.java
├── payloads/                   # Request/Response DTOs
│   ├── requests/
│   │   ├── CompleteProfileRequest.java
│   │   ├── UpdateProfileRequest.java
│   │   └── AddLoyaltyPointsRequest.java
│   └── response/
│       ├── BaseResponse.java
│       ├── UserProfileResponse.java
│       ├── ProfileCompletionResponse.java
│       └── LoyaltyPointsResponse.java
├── repository/                 # MongoDB Repositories
│   └── UserProfileRepository.java
├── service/                    # Business Logic
│   ├── UserProfileService.java
│   └── serviceimpl/
│       └── UserProfileServiceImpl.java
├── utils/                      # Utility Classes
│   └── JwtUtils.java
└── UserServiceApplication.java
```

## Prerequisites

### 1. Keycloak Configuration
Đảm bảo Keycloak đang chạy trên `http://localhost:8080`

#### Tạo Realm
1. Đăng nhập vào Keycloak Admin Console
2. Tạo realm mới: `electronics-shop`

#### Tạo Client cho User Service
1. Clients → Create client
2. Client ID: `user-service`
3. Client authentication: OFF
4. Valid redirect URIs: `http://localhost:8081/*`
5. Standard Flow Enabled: ON

#### Tạo Roles
1. Realm roles → Create role:
   - `USER` (default role)
   - `ADMIN`
   - `SERVICE`
2. Thiết lập `USER` làm default role

### 2. MongoDB
MongoDB Atlas đã được cấu hình trong `application.yaml`

## Installation & Running

### 1. Clone & Build
```bash
cd user-service
mvn clean install
```

### 2. Run Application
```bash
mvn spring-boot:run
```

Application sẽ chạy trên: `http://localhost:8081`

### 3. Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

## API Endpoints

### Base URL
```
http://localhost:8081/api/v1
```

### Authentication
Tất cả endpoints (trừ `/actuator`) yêu cầu JWT token:
```
Authorization: Bearer <JWT_TOKEN>
```

### User Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/users/me` | Lấy profile hiện tại (auto-create nếu chưa có) | USER |
| POST | `/users/me/complete` | Hoàn thiện profile lần đầu | USER |
| PUT | `/users/me` | Cập nhật profile | USER |
| GET | `/users/me/completion-status` | Kiểm tra độ hoàn thiện profile | USER |

### Address Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/users/me/addresses` | Thêm địa chỉ giao hàng | USER |
| PUT | `/users/me/addresses/{index}` | Cập nhật địa chỉ | USER |
| DELETE | `/users/me/addresses/{index}` | Xóa địa chỉ | USER |
| PATCH | `/users/me/addresses/{index}/default` | Đặt địa chỉ mặc định | USER |

### Preferences & Settings

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| PUT | `/users/me/preferences` | Cập nhật preferences | USER |
| GET | `/users/me/loyalty-points` | Xem điểm thưởng | USER |

### Admin Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/users/{keycloakUserId}` | Lấy thông tin user | ADMIN |
| GET | `/admin/users?page=0&size=20` | Danh sách users | ADMIN |
| DELETE | `/admin/users/{keycloakUserId}` | Soft delete user | ADMIN |

### Internal Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/internal/users/{keycloakUserId}/loyalty-points` | Thêm điểm thưởng | SERVICE |

## Business Rules

### Profile Completion
- **Required fields**: `phoneNumber`, `shippingAddress` (ít nhất 1)
- **Optional fields**: `dateOfBirth`, `gender`, `preferences`
- **Calculation**: `(completed_fields / total_fields) × 100`

### Shipping Addresses
- **Minimum**: 1 địa chỉ (không thể xóa địa chỉ cuối cùng)
- **Maximum**: 5 địa chỉ
- Luôn có 1 địa chỉ mặc định
- Khi xóa địa chỉ mặc định → tự động đặt địa chỉ đầu tiên làm mặc định

### Loyalty Points
- Chỉ cộng điểm, không trừ
- Mỗi lần thêm điểm phải có lý do (audit trail)
- Tỷ lệ: 1000 VND = 1 điểm

### Soft Delete
- Set `status = DELETED`
- Set `deletedAt = now()`
- Ẩn danh email: `email + ".deleted." + timestamp`
- Giữ lại dữ liệu cho mục đích audit

## Configuration

### application.yaml
```yaml
spring:
  application:
    name: user-service
  mongodb:
    uri: mongodb+srv://[username]:[password]@cluster0.xxx.mongodb.net/SkillBridgeDB
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/electronics-shop
          jwk-set-uri: http://localhost:8080/realms/electronics-shop/protocol/openid-connect/certs

server:
  port: 8081
```

## Testing

### Get Access Token from Keycloak
```bash
curl -X POST 'http://localhost:8080/realms/electronics-shop/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=user-service' \
  -d 'username=testuser' \
  -d 'password=password123'
```

### Test API with Token
```bash
curl -X GET 'http://localhost:8081/api/v1/users/me' \
  -H 'Authorization: Bearer <ACCESS_TOKEN>'
```

## Error Handling

Tất cả response đều theo format `BaseResponse`:
```json
{
  "message": "Success message",
  "statusCode": "200",
  "data": { ... }
}
```

Error response:
```json
{
  "message": "Error message",
  "statusCode": "400",
  "data": {
    "field": "error detail"
  }
}
```

## Logging

Logs được cấu hình ở các level:
- **INFO**: Application events
- **DEBUG**: Security & MongoDB operations
- **ERROR**: Exceptions

## Monitoring

### Actuator Endpoints
- Health: `http://localhost:8081/actuator/health`
- Info: `http://localhost:8081/actuator/info`
- Metrics: `http://localhost:8081/actuator/metrics`

## Common Issues & Solutions

### Issue: "Invalid token signature"
**Solution**: Kiểm tra `issuer-uri` và `jwk-set-uri` trong `application.yaml`

### Issue: "Access Denied"
**Solution**: Kiểm tra user có role phù hợp (USER, ADMIN, SERVICE)

### Issue: "Cannot delete last address"
**Solution**: Đây là by design. User phải có ít nhất 1 địa chỉ.

### Issue: "Profile already completed"
**Solution**: Endpoint `/complete` chỉ được gọi 1 lần. Dùng `/users/me` để update.

## Security Best Practices
✅ JWT token validation với Keycloak  
✅ Role-based access control (RBAC)  
✅ CORS configuration  
✅ Stateless session management  
✅ Input validation với Jakarta Validation  
✅ Global exception handling  

## Future Enhancements
- [ ] Redis cache cho user profiles
- [ ] Event streaming với Kafka
- [ ] Advanced analytics & reporting
- [ ] Multi-language support
- [ ] Profile picture upload
- [ ] Email notification service integration

## Contributors
- Backend Development Team
- Security Team
- DevOps Team

## License
Proprietary - Electronics Shop

---

**Last Updated**: March 10, 2026  
**Version**: 1.0.0  
**Status**: Production Ready

