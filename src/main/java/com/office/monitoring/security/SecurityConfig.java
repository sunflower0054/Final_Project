package com.office.monitoring.security;

import com.office.monitoring.member.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(customUserDetailsService)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/auth/login"),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/auth/logout"),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/events/receive"),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/daily-activity")
                        )
                )
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 세션 설정 추가 — 로그인 성공 시 세션 생성 보장
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 + 공개 페이지
                        .requestMatchers(
                                "/",
                                "/index",
                                "/index/**",
                                "/member/login",
                                "/member/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 회원 공개 API
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/check-username").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()

                        // 내부 통신 공개 API
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/receive").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/daily-activity").permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 로그인 필수 회원 API
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/auth/withdraw").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/my-info").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/my-info").authenticated()

                        // 그 외 API는 로그인 필요
                        .requestMatchers("/api/v1/**").authenticated()

                        // 페이지 접근
                        .requestMatchers(
                                "/camera/**",
                                "/events/**",
                                "/report/**",
                                "/myinfo/**",
                                "/resident/detail",
                                "/resident/edit",
                                "/setting/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/api/v1/auth/login")
                        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        // 리다이렉트 방식으로 변경
                        // 브라우저가 세션 쿠키를 자동으로 완벽하게 처리
                        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        .successHandler((request, response, authentication) -> {
                            response.sendRedirect("/");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("/member/login?error");
                        })
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/member/login"))
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .addLogoutHandler((request, response, authentication) -> {
                            Authentication current = authentication != null
                                    ? authentication
                                    : SecurityContextHolder.getContext().getAuthentication();
                            new SecurityContextLogoutHandler().logout(request, response, current);
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("""
                                {"success":true,"message":"로그아웃되었습니다."}
                                """.trim());
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}