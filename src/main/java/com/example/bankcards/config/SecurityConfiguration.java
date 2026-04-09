package com.example.bankcards.config;

import com.example.bankcards.exception.ErrorCode;
import com.example.bankcards.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

import static com.example.bankcards.exception.ProblemDetailProvider.getProblemDetail;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtFilter jwtFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JsonMapper jsonMapper;

    public SecurityConfiguration(
            JwtFilter jwtFilter,
            AuthenticationEntryPoint authenticationEntryPoint,
            JsonMapper jsonMapper
    ) {
        this.jwtFilter = jwtFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jsonMapper = jsonMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .exceptionHandling(
                        exception -> exception
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler((request, response, ex) -> {
                                    ProblemDetail pd = getProblemDetail(
                                            HttpStatus.FORBIDDEN,
                                            "Access denied",
                                            ErrorCode.FORBIDDEN,
                                            new ServletWebRequest(request)
                                    );

                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                                    response.setCharacterEncoding("UTF-8");
                                    response.getWriter().write(jsonMapper.writeValueAsString(pd));
                                })
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                                authorizeRequests -> {
                                    authorizeRequests.requestMatchers("/api/auth/**").permitAll();
                                    authorizeRequests.anyRequest().authenticated();
                                }
                ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
