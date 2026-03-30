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

## 다음 단계 예정
- `Member` 엔티티 연동
- `UserDetailsService` 구현
- 권한(Role) 세분화 및 인가 정책 고도화
