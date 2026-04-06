package com.office.monitoring.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
/** CustomUserDetailsService의 역할을 담당한다. */
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /** loadUserByUsername 동작을 수행한다. */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다. username=" + username));

        return User.builder()
            .username(member.getUsername())
            .password(member.getPassword())
            .authorities(toAuthorities(member.getRole()))
            .build();
    }

    /** toAuthorities 동작을 수행한다. */
    private Collection<? extends GrantedAuthority> toAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
