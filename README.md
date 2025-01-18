# 인증 및 회원가입 서비스

Spring Boot를 기반으로 개발된 **인증 및 회원가입 서비스**는 JWT(Json Web Token)를 이용한 인증 및 Redis를 활용한 토큰 관리 기능을 제공합니다. 이 서비스는 사용자 로그인, 토큰 재발급, 회원가입 등 핵심적인 인증 기능을 지원하며, 관리자와 일반 사용자 구분 기능도 포함되어 있습니다.

---

## 프로젝트 특징

1. **JWT 기반 인증**:
    - 액세스 토큰과 리프레시 토큰을 발급하여 사용자의 인증과 세션 관리를 처리.
    - 토큰의 만료 시간 및 역할 정보를 포함하여 보안 강화.

2. **Redis를 활용한 리프레시 토큰 관리**:
    - 사용자별 리프레시 토큰을 Redis에 저장하여 빠르고 안정적인 토큰 관리 구현.
    - 토큰 만료 시 Redis에서 자동 삭제 처리.

3. **비밀번호 암호화**:
    - Spring Security의 `PasswordEncoder`를 사용하여 사용자 비밀번호 암호화 및 검증.

4. **관리자 회원가입 지원**:
    - 관리자로 가입하려면 별도의 관리자 토큰이 필요하며, 이를 통해 권한 구분 처리.

---

## 주요 기능

### 1. 사용자 로그인
- **API 경로**: `POST /api/auth/login`
- **설명**:  
  사용자의 아이디와 비밀번호를 검증한 후, 성공적으로 인증된 사용자에게 액세스 토큰과 리프레시 토큰을 반환.
- **주요 로직**:
    - 비밀번호 검증 후 JWT 생성.
    - Redis에 리프레시 토큰 저장.
- **관련 클래스**:
    - `LoginController`, `LoginService`
    - `LoginRequest`, `LoginResponse`

### 2. 토큰 재발급
- **API 경로**: `POST /api/auth/reissue`
- **설명**:  
  리프레시 토큰을 검증한 뒤, 새로운 액세스 토큰과 리프레시 토큰을 발급.
- **주요 로직**:
    - 리프레시 토큰의 유효성 확인.
    - Redis에서 저장된 토큰과 비교.
    - 새로운 토큰 생성 및 Redis 업데이트.
- **관련 클래스**:
    - `LoginController`, `LoginService`

### 3. 사용자 회원가입
- **API 경로**: `POST /api/auth/signup`
- **설명**:  
  신규 사용자 또는 관리자를 등록하며, 비밀번호 규칙 및 닉네임 중복 검사를 수행.
- **주요 로직**:
    - 비밀번호 규칙 검증(대문자, 소문자, 숫자, 특수문자 포함).
    - 닉네임 중복 확인.
    - 관리자 가입 시 관리자 토큰 검증.
- **관련 클래스**:
    - `SignupController`, `SignupService`
    - `SignupRequest`, `SignupResponse`

---

## 기술 스택

- **프레임워크**: Spring Boot
- **보안**: Spring Security, JWT
- **데이터베이스**: MySQL, H2(테스트용)
- **토큰 관리**: Redis
- **빌드 도구**: Gradle
- **테스트 도구**: JUnit5, Mockito

---

## 파일 구조

# 파일 구조

이 프로젝트의 주요 파일 구조는 다음과 같습니다:

```plaintext
src/main/java
└── com.example
    ├── common                  # 공통 모듈
    │   ├── ApiResponse.java    # API 응답 처리 클래스
    │   ├── ErrorStatus.java    # 에러 상태 정의
    │   └── exception
    │       └── ApiException.java  # 사용자 정의 예외 처리
    ├── domain                  # 도메인 관련 모듈
    │   ├── entity              # 엔티티 클래스
    │   │   └── User.java       # 사용자 엔티티
    │   ├── enums               # 열거형 정의
    │   │   ├── TokenType.java  # 토큰 유형
    │   │   └── UserRole.java   # 사용자 역할
    │   ├── login               # 로그인 관련 모듈
    │   │   ├── controller
    │   │   │   └── LoginController.java  # 로그인 API 컨트롤러
    │   │   ├── dto
    │   │   │   ├── request
    │   │   │   │   └── LoginRequest.java  # 로그인 요청 DTO
    │   │   │   └── response
    │   │   │       └── LoginResponse.java # 로그인 응답 DTO
    │   │   └── service
    │   │       └── LoginService.java      # 로그인 서비스
    │   ├── signup              # 회원가입 관련 모듈
    │   │   ├── dto
    │   │   │   ├── request
    │   │   │   │   └── SignupRequest.java  # 회원가입 요청 DTO
    │   │   │   └── response
    │   │   │       └── SignupResponse.java # 회원가입 응답 DTO
    │   │   └── service
    │   │       └── SignupService.java      # 회원가입 서비스
    │   └── repository          # 데이터베이스 레포지토리
    │       └── UserRepository.java # 사용자 레포지토리 인터페이스
    └── security                # 보안 관련 모듈
        ├── JwtUtil.java        # JWT 유틸리티 클래스
        └── SecurityConfig.java # Spring Security 설정
```

---

## 테스트

### 1. 서비스 유닛 테스트
- **LoginServiceTest**:
    - 성공 및 실패 케이스를 포함하여 로그인 로직을 검증.
    - 주요 테스트 시나리오:
        - 정상적인 로그인 요청 처리.
        - 잘못된 비밀번호 또는 사용자 정보로 로그인 실패.
        - 리프레시 토큰 재발급 성공 및 실패.
- **SignupServiceTest**:
    - 비밀번호 규칙 및 닉네임 중복 확인 로직 검증.
    - 주요 테스트 시나리오:
        - 일반 사용자 및 관리자 가입 성공.
        - 비밀번호 규칙 위반 및 닉네임 중복 시 예외 발생.

---

