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

### 작업 1 당시 적용된 기본 보안 구조
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

## 작업 3: 개발용 기본 계정 시드 추가
로컬/개발 환경에서 로그인 테스트를 바로 할 수 있도록, 애플리케이션 시작 시 기본 계정을 자동 생성하는 시드를 추가했습니다.

### 시드 동작 조건
- `local` 또는 `dev` 프로파일에서만 동작합니다.
- 애플리케이션 시작 시 `members` 테이블이 비어 있을 때만 계정을 생성합니다.
- 이미 회원 데이터가 1건 이상 있으면 추가 생성하지 않습니다.

### 개발용 기본 계정
- `admin / admin1234!` → 권한: `ADMIN`
- `user / user1234!` → 권한: `USER`

### 비밀번호 저장 방식
- 위 기본 계정 비밀번호는 저장 전에 `BCryptPasswordEncoder`로 인코딩됩니다.
- 즉 DB에는 평문이 아닌 BCrypt 해시가 저장됩니다.

### 로그인 테스트 방법 (간단)
1. `local` 또는 `dev` 프로파일로 애플리케이션을 실행합니다.
2. `/member/login`에서 위 기본 계정으로 로그인 테스트를 진행합니다.

> ⚠️ 주의: 위 계정은 **개발/테스트 전용**입니다. 운영 환경에서 동일 계정/비밀번호를 그대로 사용하면 안 됩니다.

## 작업 4: URL별 권한 제어와 CSRF 예외 설정

### 보안 정책 (URL 권한 정책)

| 구분 | 경로 |
|---|---|
| 공개 접근 가능 (`permitAll`) | `/`, `/index`, `/index/**`, `/member/login`, `/member/register`, `/css/**`, `/js/**`, `/images/**`, `/favicon.ico`, `POST /api/v1/events/receive` |
| 로그인 필요 (`authenticated`) | `/camera/**`, `/events/**`, `/report/**`, `/myinfo/**`, `/resident/detail` |
| 관리자 전용 (`hasRole("ADMIN")`) | `/setting/**`, `/resident/edit`, `/resident/register`, `/api/v1/settings/**` |
| 기본 정책 | 위 매핑 외 모든 요청은 인증 필요 |

### CSRF 예외 정책
- `POST /api/v1/events/receive` 는 외부 이벤트 수신(브라우저 폼 기반 요청이 아닌 호출) 용도를 고려해 **공개 + CSRF 예외**로 최소 범위 적용했습니다.
- 그 외 브라우저 기반 요청은 기본적으로 CSRF 보호를 유지합니다.
- TODO: 해당 API는 추후 **API Key** 또는 **서버 간 인증** 도입을 검토해야 합니다.

### 인증/인가 실패 동작
- 인증되지 않은 사용자가 보호된 페이지에 접근하면 `/member/login` 으로 이동합니다.
- 인증은 되었지만 권한이 부족한 경우 HTTP `403 Forbidden` 이 반환됩니다.
