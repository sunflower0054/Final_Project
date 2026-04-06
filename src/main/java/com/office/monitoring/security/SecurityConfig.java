package com.office.monitoring.security;

import com.office.monitoring.member.CustomUserDetailsService;
import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
/** SecurityConfig의 역할을 담당한다. */
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final MemberRepository memberRepository;

    /** securityFilterChain 동작을 수행한다. */
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
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            String username = authentication.getName();
                            Member member = memberRepository.findByUsername(username).orElse(null);
                            String name = member != null ? member.getName() : username;
                            String role = member != null ? member.getRole().name() : "";

                            if (request.getSession(false) != null) {
                                boolean jsessionAlreadySet = response.getHeaders(HttpHeaders.SET_COOKIE).stream()
                                        .anyMatch(header -> header.startsWith("JSESSIONID="));

                                if (!jsessionAlreadySet) {
                                    Cookie sessionCookie = new Cookie("JSESSIONID", request.getSession(false).getId());
                                    sessionCookie.setHttpOnly(true);
                                    sessionCookie.setPath("/");
                                    sessionCookie.setSecure(request.isSecure());
                                    response.addCookie(sessionCookie);
                                }
                            }

                            response.getWriter().write("""
                                {"success":true,"username":"%s","name":"%s","role":"%s","token":null,"message":"로그인 성공"}
                                """.formatted(
                                    escapeJson(username),
                                    escapeJson(name),
                                    escapeJson(role)
                            ).trim());
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

    /** escapeJson 동작을 수행한다. */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    /** passwordEncoder 동작을 수행한다. */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
