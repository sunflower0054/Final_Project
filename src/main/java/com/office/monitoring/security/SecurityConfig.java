package com.office.monitoring.security;

import com.office.monitoring.member.CustomUserDetailsService;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final MemberRepository memberRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(customUserDetailsService)
                .csrf(csrf -> csrf
                        // TODO: POST /api/v1/events/receive 엔드포인트는 추후 API Key 또는 서버 간 인증 도입 검토 필요
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/auth/login"),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/auth/logout"),
                                PathPatternRequestMatcher.withDefaults()
                                        .matcher(HttpMethod.POST, "/api/v1/events/receive")
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/index",
                                "/index/**",
                                "/member/login",
                                "/member/register",
                                "/api/v1/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/receive").permitAll()
                        .requestMatchers(
                                "/setting/**",
                                "/resident/edit",
                                "/resident/register",
                                "/api/v1/settings/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/camera/**",
                                "/events/**",
                                "/report/**",
                                "/myinfo/**",
                                "/resident/detail"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/api/v1/auth/login")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            String username = authentication.getName();
                            Member member = memberRepository.findByLoginId(username).orElse(null);
                            String name = member != null ? member.getName() : username;
                            String role = member != null ? member.getRole().name() : "";

                            // NOTE: Authentication is handled by the HTTP session (JSESSIONID).
                            // `token` is returned as `null` only for response-contract compatibility.
                            response.getWriter().write("""
                                {"success":true,"username":"%s","name":"%s","role":"%s","token":null,"message":"로그인 성공"}
                                """.formatted(escapeJson(username), escapeJson(name), escapeJson(role)).trim());
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.getWriter().write("""
                                {"success":false,"message":"아이디 또는 비밀번호가 올바르지 않습니다."}
                                """.trim());
                        })
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/member/login"))
                        .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            Authentication current = authentication != null ? authentication : SecurityContextHolder.getContext().getAuthentication();
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

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
