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

5. **CI/CD 자동화**:
    - Docker와 GitHub Actions를 사용하여 CI/CD 파이프라인을 구축.
    - main branch 코드 푸시 후 자동 빌드, 테스트, Docker 이미지 빌드 및 AWS 배포.

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
- **CI/CD**: GitHub Actions, Docker

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
        - **만료된 리프레시 토큰**: 만료된 리프레시 토큰을 이용한 재발급 요청 시 EXPIRED_REFRESH_TOKEN 에러가 발생하는지 검증합니다.
        - **잘못된 비밀번호**: 비밀번호가 일치하지 않을 경우 INVALID_CREDENTIALS 에러가 발생하는지 검증합니다.
        - **로그인 성공**: 정상적인 로그인 요청에 대해 액세스 토큰과 리프레시 토큰이 반환되는지 검증합니다.
        - **사용자 없음**: 존재하지 않는 사용자로 로그인 시도 시, NOT_FOUND_USER 에러가 발생하는지 검증합니다.
- **SignupServiceTest**:
    - 비밀번호 규칙 및 닉네임 중복 확인 로직 검증.
    - 주요 테스트 시나리오:
        - **사용자 회원가입 성공**: 일반 사용자가 유효한 비밀번호와 닉네임으로 회원가입을 시도할 때 성공하는지 검증합니다.
        - **관리자 회원가입 성공**: 관리자가 유효한 관리자 토큰을 제공하고 가입하는지 검증합니다.
        - **비밀번호 규칙 위반**: 잘못된 비밀번호 규칙을 입력할 경우 INVALID_REQUEST 에러가 발생하는지 검증합니다.
        - **잘못된 관리자 토큰**: 유효하지 않은 관리자 토큰을 입력할 경우 FORBIDDEN_TOKEN 에러가 발생하는지 검증합니다.
        - **닉네임 중복**: 이미 존재하는 닉네임으로 회원가입 시도 시 DUPLICATE_NICKNAME 에러가 발생하는지 검증합니다.
---
### GitHub Actions를 통한 AWS 배포
이 프로젝트에서는 GitHub Actions를 이용하여 AWS에 자동 배포하는 작업을 설정하였습니다. 아래는 해당 설정을 위한 CI/CD 파이프라인의 YAML 파일입니다:
```plaintext
name: Deploy to AWS

on:
  push:
    branches:
      - main

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      # 1. Checkout repository
      - name: Checkout repository
        uses: actions/checkout@v3

      # 2. Set up JDK 17
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      # 3. Build with Gradle
      - name: Grant execute permissions for Gradlew
        run: chmod +x ./gradlew

      - name: Build with Gradle
        run: ./gradlew build

      # 4. Configure AWS credentials
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v1
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ secrets.AWS_REGION }}

      # 5. Login to Amazon ECR
      - name: Login to Amazon ECR
        uses: aws-actions/amazon-ecr-login@v1

      # 6. Delete existing image from ECR
      - name: Delete existing image from ECR
        env:
          AWS_REGION: ${{ secrets.AWS_REGION }}
          ECR_URI: ${{ secrets.ECR_URI }}
        run: |
          IMAGE_TAG=latest
          aws ecr batch-delete-image \
            --repository-name demo \
            --image-ids imageTag=$IMAGE_TAG || echo "No existing image found."

      # 7. Build Docker image
      - name: Build Docker image
        run: docker build -t demo .

      # 8. Tag and Push Docker image
      - name: Tag and Push Docker image
        env:
          AWS_REGION: ${{ secrets.AWS_REGION }}
          ECR_URI: ${{ secrets.ECR_URI }}
        run: |
          docker tag demo:latest $ECR_URI/demo:latest
          docker push $ECR_URI/demo:latest

      # 9. SSH into EC2 & Deploy
      - name: Deploy to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_KEY }}
          port: 22
          debug: true # 디버그 활성화
          script: |
            echo "SSH connection established. Starting deployment."
      
            # Stop and remove existing Spring Boot container
            docker stop demo-app || true
            docker rm demo-app || true
      
            # Remove old image
            docker rmi ${{ secrets.ECR_URI }}/demo:latest || true
      
            # Pull and run new image
            aws ecr get-login-password --region ${{ secrets.AWS_REGION }} | docker login --username AWS --password-stdin ${{ secrets.ECR_URI }}
            docker pull ${{ secrets.ECR_URI }}/demo:latest
            
            # Create .env file dynamically
            echo "${{ secrets.APP_ENV }}" > .env
            
            # Run Docker container with .env file
            docker run -d --name demo-app --link redis -p 8080:8080 --env-file /home/ec2-user/.env ${{ secrets.ECR_URI }}/demo:latest            
            
            # Ensure Redis container is running
            docker ps | grep redis || docker run -d --name redis -p 6379:6379 redis:6.2
            
            # Check application status
            echo "Checking application logs..."
            docker logs demo-app || echo "Application logs not found."
      
            echo "Deployment finished successfully."

```
## 설명
### 1. 프로젝트 빌드:

 - GitHub Actions는 gradlew를 사용하여 Spring Boot 프로젝트를 빌드합니다. Gradle을 통해 의존성을 다운로드하고 프로젝트를 컴파일합니다.

### 2. AWS 자격 증명 설정:

 - AWS에 접근하기 위해 GitHub Secrets에 저장된 자격 증명을 사용하여 aws-actions/configure-aws-credentials 액션을 통해 AWS 자격 증명을 설정합니다.

### 3. ECR 로그인 및 이미지 삭제:
 - ECR(Amazon Elastic Container Registry)에 로그인한 후, 기존의 Docker 이미지를 삭제하여 새로운 이미지를 푸시할 준비를 합니다.

### 4. Docker 이미지 빌드 및 푸시:
 - 프로젝트를 빌드하고 Docker 이미지를 생성한 뒤, ECR에 푸시합니다.

### 5. EC2 인스턴스에서 애플리케이션 배포:
- appleboy/ssh-action을 사용하여 EC2 인스턴스에 SSH 연결 후, Spring Boot 애플리케이션의 Docker 컨테이너를 관리합니다. 이를 통해 새로운 Docker 이미지를 실행하고 Redis와 같은 의존 서비스를 실행합니다.

### 6. 디버그 로그 활성화:
 - 배포 중 발생할 수 있는 오류를 쉽게 추적할 수 있도록 debug: true를 설정하여 디버깅 로그를 활성화합니다.

### 7. CI/CD 파이프라인 특징:
 - 자동화된 배포: main 브랜치에 푸시될 때마다 자동으로 빌드, 테스트, Docker 이미지 빌드, ECR 푸시, EC2 배포가 이루어집니다.
 - 환경 변수 관리: .env 파일을 동적으로 생성하여 환경 변수들을 안전하게 관리합니다.
 - Redis 관리: Redis 컨테이너가 실행 중이지 않으면 자동으로 실행되도록 구성되어 있어, 애플리케이션에 필수적인 Redis가 항상 가동됩니다.
이 설정을 통해 GitHub에서 자동으로 애플리케이션을 빌드하고 AWS에 배포할 수 있습니다.