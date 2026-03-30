# Office Monitoring

Spring Boot 4.x / Java 17 / Thymeleaf / JPA 기반의 오피스 모니터링 프로젝트입니다.  
패키지 루트는 `com.office.monitoring` 입니다.

## 프로젝트 개요
- 모니터링 관련 화면(Thymeleaf)과 API를 제공하는 서버 애플리케이션
- JPA 기반 데이터 접근
- 정적 리소스(css/js/images) 및 웹 페이지 렌더링 지원

## 작업 1: Spring Security 기본 뼈대 추가
이번 단계에서는 **세션 기반 인증을 붙이기 위한 최소 보안 구조**를 프로젝트에 반영했습니다.

### Spring Security 도입 목적
- 인증이 필요한 요청과 공개 요청을 명확히 분리하기 위한 기반 마련
- 이후 `Member` 기반 로그인/인증 로직을 안전하게 연결하기 위한 구조 선반영

### 현재 적용된 기본 보안 구조
- `SecurityFilterChain` 등록 (`com.office.monitoring.security.SecurityConfig`)
- 공개 경로(`permitAll`):
  - `/`
  - `/index`
  - `/index/**`
  - `/member/**`
  - `/css/**`
  - `/js/**`
  - `/images/**`
  - `/favicon.ico`
- 인증 필요 경로: 위 공개 경로를 제외한 모든 요청(`authenticated`)

### 로그인/로그아웃 경로
- 로그인 페이지: `/member/login`
- 로그인 처리: `POST /member/login`
- 로그아웃: `/member/logout`
- 로그인 성공 시 기본 이동 경로: `/`

### 이번 단계 변경 사항
- `build.gradle`에 Spring Security 의존성 추가
- `security` 패키지에 `SecurityConfig` 추가
- `BCryptPasswordEncoder` 빈 추가

## 작업 2: 인증용 Member 도메인 최소 구현
이번 단계에서는 **세션 기반 로그인에 필요한 최소 회원 인증 도메인**만 반영했습니다.

### 인증/권한 모델
- `Member` 엔티티 추가
- `Role` enum 도입: `USER`, `ADMIN`
- `MemberRepository` 추가 (`findByLoginId`)
- `CustomUserDetailsService` 추가

### 핵심 인증 필드
`Member` 엔티티는 인증에 필요한 최소 필드를 포함합니다.
- `loginId` (로그인 식별자)
- `password` (암호화 저장)
- `role` (권한: `USER` 또는 `ADMIN`)
- `enabled` (계정 활성화 여부)

### Spring Security 회원 조회 방식 (요약)
- 사용자가 `/member/login`으로 로그인하면, Spring Security가 `UserDetailsService`를 통해 회원을 조회합니다.
- 이 프로젝트에서는 `CustomUserDetailsService`가 `MemberRepository.findByLoginId(...)`로 사용자를 조회합니다.
- 조회된 `Member` 정보를 `UserDetails`로 변환하여 인증에 사용합니다.

### 비밀번호 저장 방식
- 비밀번호는 `BCryptPasswordEncoder` 기반으로 저장/검증하는 것을 전제로 합니다.
- 따라서 DB에는 **BCrypt 해시 값**이 저장되어야 합니다.

### 현재 범위 아님
- 회원가입(Registration) 전체 기능
- 이메일 인증
- OAuth/Social Login

## 다음 단계 예정
- 권한(Role) 세분화 및 인가 정책 고도화
