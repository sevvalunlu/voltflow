package com.voltflow.config;

import com.voltflow.entity.User;
import com.voltflow.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**").permitAll()
                .requestMatchers("/scooters/delete/**", "/scooters/new").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      @Value("${voltflow.seed.admin-password:}") String adminPassword,
                                      @Value("${voltflow.seed.employee-password:}") String employeePassword) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "admin", adminPassword, "ADMIN");
            createUserIfMissing(userRepository, passwordEncoder, "personel", employeePassword, "EMPLOYEE");
        };
    }

    private void createUserIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            log.warn("'{}' kullanicisi olusturulmadi: .env dosyasinda sifre tanimli degil.", username);
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build());
    }
}
