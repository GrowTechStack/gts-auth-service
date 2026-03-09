# gts-auth-service

GrowTechStack 플랫폼의 인증/인가 마이크로서비스.
이메일/비밀번호 기반 회원가입·로그인, JWT 발급·갱신·폐기를 담당한다.

---

## 기술 스택

- Java 17 / Spring Boot 3.5
- Spring Security 6 / JJWT 0.12.6
- Spring Data JPA / MySQL 8
- Spring Cloud Netflix Eureka Client
- Springdoc OpenAPI (Swagger UI)

---

## 포트

| 환경 | 포트 |
|------|------|
| 로컬 (dev) | 29997 |
| Docker (prod) | 29997 |

---

## API

Base path: `/api/v1/auth`

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/signup` | 불필요 | 이메일/비밀번호 회원가입 |
| POST | `/login` | 불필요 | 로그인 → Access·Refresh 토큰 발급 |
| POST | `/refresh` | 불필요 | Refresh 토큰으로 Access 토큰 재발급 |
| POST | `/logout` | Gateway X-User-Id | 리프레시 토큰 폐기 |
| GET | `/me` | Gateway X-User-Id | 현재 사용자 정보 조회 |

> `/logout`, `/me`는 공개 엔드포인트가 아니다.
> Gateway가 JWT를 검증하고 `X-User-Id` 헤더를 주입한 뒤 이 서비스로 요청을 전달한다.

### 요청/응답 예시

#### POST `/signup`
```json
// Request
{ "email": "user@example.com", "password": "password123", "nickname": "홍길동" }

// Response 201
{ "success": true, "data": null, "error": null }
```

#### POST `/login`
```json
// Request
{ "email": "user@example.com", "password": "password123" }

// Response 200
{
  "success": true,
  "data": {
    "accessToken": "<JWT>",
    "refreshToken": "<JWT>"
  },
  "error": null
}
```

#### POST `/refresh`
```json
// Request
{ "refreshToken": "<JWT>" }

// Response 200
{ "success": true, "data": { "accessToken": "...", "refreshToken": "..." }, "error": null }
```

#### GET `/me`
```json
// Response 200
{
  "success": true,
  "data": { "id": 1, "email": "user@example.com", "nickname": "홍길동", "role": "USER", "provider": "LOCAL" },
  "error": null
}
```

### 에러 코드

| HTTP | Code | 설명 |
|------|------|------|
| 400 | C001 | 잘못된 입력값 |
| 401 | A002 | 이메일 또는 비밀번호 불일치 |
| 401 | A003 | 유효하지 않은 토큰 |
| 401 | A004 | 만료된 토큰 |
| 401 | A005 | 리프레시 토큰 없음 |
| 404 | U001 | 사용자 없음 |
| 409 | A001 | 이메일 중복 |
| 500 | C002 | 서버 내부 오류 |

---

## JWT 구조

| 토큰 | 유효기간 | Claims |
|------|---------|--------|
| Access Token | 30분 | `sub`=userId, `role`=USER\|ADMIN |
| Refresh Token | 14일 | `sub`=userId |

- Refresh Token은 **SHA-256 해시**로 DB에 저장된다 (원문 저장 없음).
- 만료된 Refresh Token은 매일 새벽 3시에 자동 정리된다 (`@Scheduled`).

---

## 패키지 구조

```
com.gts.auth
├── domain
│   ├── auth
│   │   ├── controller     # AuthController
│   │   ├── dto            # SignupRequest, LoginRequest, RefreshRequest, TokenResponse
│   │   └── service
│   │       ├── AuthService.java          # Interface
│   │       └── impl/AuthServiceImpl.java # Implementation
│   ├── token
│   │   ├── entity         # RefreshToken
│   │   └── repository     # RefreshTokenRepository
│   └── user
│       ├── dto            # UserResponse
│       ├── entity         # User, Role, AuthProvider
│       └── repository     # UserRepository
└── global
    ├── common/response    # ApiResult
    ├── config             # SecurityConfig
    ├── error              # ErrorCode, BusinessException, GlobalExceptionHandler
    └── jwt                # JwtProvider
```

---

## 로컬 실행

```bash
# 1. MySQL 실행 (포트 3306, DB: growtechstack)
# 2. 빌드 및 실행
./gradlew bootRun
```

`application-dev.yml`이 기본으로 활성화된다. JWT secret은 기본값(`local-dev-secret-key-must-be-at-least-32-chars`)을 사용한다.

Swagger UI: http://localhost:29997/swagger-ui/index.html

---

## 환경변수 (Docker)

| 변수 | 필수 | 설명 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | ✅ | `docker` 고정 |
| `EUREKA_URL` | ✅ | Eureka 주소 |
| `DB_HOST` | ✅ | MySQL 호스트 |
| `DB_NAME` | ✅ | DB 이름 |
| `DB_USERNAME` | ✅ | DB 사용자 |
| `DB_PASSWORD` | ✅ | DB 비밀번호 |
| `JWT_SECRET` | ✅ | JWT 서명 키 (32자 이상, Gateway와 동일한 값) |

---

## 배포

main 브랜치 push 시 GitHub Actions가 자동으로 ECR에 빌드·푸시 후 EC2에 배포한다.

```bash
# 수동 배포
/app/deploy.sh auth
```

---

## 향후 계획

- Google / GitHub OAuth2 소셜 로그인
- 이메일 인증 기반 비밀번호 재설정
