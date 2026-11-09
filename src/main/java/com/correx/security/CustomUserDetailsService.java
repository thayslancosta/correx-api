package com.correx.security;

import com.correx.repository.ProfessorRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfessorRepository professorRepository;

    public CustomUserDetailsService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var professor = professorRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Professor não encontrado: " + email));

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + professor.getRole().name()));

        return User.builder()
            .username(professor.getEmail())
            .password(professor.getSenha())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!professor.getAtivo())
            .build();
    }
}
