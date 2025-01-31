package bingol.campus.security.config;

import bingol.campus.security.entity.Role;
import bingol.campus.security.filter.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // Public erişim için yollar
        String[] publicPaths = {
                "/auth/**",
                "/v1/api/admin/register",
                "/v1/api/token/",
                "/v1/api/meal/**",
                "/v1/api/weather/**",
                "/v1/api/student/sign-up",
                "/v1/api/teacher/register",
                "/v1/api/news/**"
        };

        // Sadece admin için yollar
        String[] adminPaths = {
               "/v1/api/admin/**"
        };

        // öğrenci rolleri için yollar
        String[] studentPaths = {
                "/v1/api/student/**",

        };



        // Haber görüntüleme yolları (public)
        String[] newsPublicPaths = {
                "/v1/api/news/getAll",
                "/v1/api/news/getById/**",
                "/v1/api/news/getByUrl/**"
        };

        return httpSecurity.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // Public erişim
                        .requestMatchers(publicPaths).permitAll()
                        // Admin erişimi
                        .requestMatchers(adminPaths).hasAuthority(Role.ADMIN.getAuthority())
                        // Öğretmen  erişimi
                        // öğrenci erişimi
                        .requestMatchers(studentPaths).hasAuthority(Role.STUDENT.getAuthority())
                        // Haber görüntüleme
                        .requestMatchers(newsPublicPaths).permitAll()
                        // Diğer istekler
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
