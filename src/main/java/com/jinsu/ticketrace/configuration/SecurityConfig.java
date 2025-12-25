package com.jinsu.ticketrace.configuration;


import com.jinsu.ticketrace.auth.jwt.JwtAuthenticationFilter;
import com.jinsu.ticketrace.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()
//                );
//
//        return http.build();
//    }


    //패스워드 인코딩 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder(14)); // 10 -> 14로 상향

        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret-key}") String secretBase64,
            @Value("${jwt.access-minutes}") long accessMinutes,
            @Value("${jwt.refresh-days}") long refreshDays
    ) {
        return new JwtTokenProvider(
                issuer,
                Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64)),
                Duration.ofMinutes(accessMinutes),
                Duration.ofDays(refreshDays)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenProvider tokenProvider) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/members/signup","/swagger-ui/**", "/swagger-resources/**").permitAll()
                .anyRequest().authenticated()
        );

        http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
