package com.nemo.oceanAcademy.config;
import com.nemo.oceanAcademy.common.security.CustomAuthenticationEntryPoint;
import com.nemo.oceanAcademy.common.security.CustomAccessDeniedHandler;
import com.nemo.oceanAcademy.common.security.CustomAuthorizationManager;
import com.nemo.oceanAcademy.domain.auth.security.JwtAuthenticationFilter;
import com.nemo.oceanAcademy.domain.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ComponentScan(basePackages = {"com.nemo.oceanAcademy.common.security", "com.nemo.oceanAcademy.domain.auth.security"})
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthorizationManager customAuthorizationManager;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .requiresChannel(channel ->
//                        channel.anyRequest().requiresSecure()
//                )
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션을 사용하지 않음
                .authorizeHttpRequests(auth -> auth

                        // static resources
                        .requestMatchers("/static/**").permitAll()

                        // Users
                        .requestMatchers(HttpMethod.PATCH, "/api/users").authenticated()                // 사용자 정보 업데이트, 해당 Id 유저만 가능
                        .requestMatchers(HttpMethod.GET, "/api/users").authenticated()                  // 사용자 정보 조회, 해당 Id 유저만 가능
                        .requestMatchers(HttpMethod.GET, "/api/users/checkNickname").permitAll()        // 닉네임 중복 검사

                        // Kakao
                        .requestMatchers(HttpMethod.GET, "/api/auth/kakao/app-key").permitAll()         // 클라이언트에게 카카오 앱 키 발급
                        .requestMatchers(HttpMethod.GET, "/api/auth/kakao/callback/**").permitAll()     // 카카오 인증 코드 처리 후 JWT 발급

                        // Auth
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/signup").authenticated()          // 회원탈퇴 신청, 해당 Id 유저만 가능
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").authenticated()           // 회원가입 신청
                        .requestMatchers(HttpMethod.GET, "/api/auth/signup").authenticated()            // 회원가입 여부 확인, 해당 Id 유저에게만 응답

                        // Categories
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()                 // 카테고리 테이블 전체 리스트 불러오기

                        // Dashboards
                        .requestMatchers(HttpMethod.GET, "/api/classes/{classId}/dashboard").authenticated()            // 강의 대시보드 정보 불러오기
                        .requestMatchers(HttpMethod.GET, "/api/classes/{classId}/dashboard/students").authenticated()   // 강의를 듣는 수강생 리스트 정보 불러오기, 강사만 가능

                        // Classes
                        .requestMatchers(HttpMethod.PATCH, "/api/classes/{classId}/enroll").authenticated()     // 수강신청, 수강생이 아니고 강사가 아닐때만 가능
                        .requestMatchers(HttpMethod.PATCH, "/api/classes/{classId}").authenticated()            // 강의실 정보 업데이트, 강사만 가능
                        .requestMatchers(HttpMethod.PATCH, "/api/classes/{classId}/delete").authenticated()     // 강의실 삭제, 강사만 가능
                        .requestMatchers(HttpMethod.POST, "/api/classes").authenticated()                       // 새로운 강의실 생성
                        .requestMatchers(HttpMethod.GET, "/api/classes/{classId}/role").authenticated()         // 해당 강의실의 "강사/수강생/관계없음" 구분, 해당 Id 유저에게만 응답
                        .requestMatchers(HttpMethod.GET, "/api/classes/{classId}").permitAll()                  // 개별 강의실 조회

                        // 전체 강의실 조회에 대해 Custom Authorization Manager 사용
                        .requestMatchers(HttpMethod.GET, "/api/classes").access(customAuthorizationManager)

                        // Schedules
                        .requestMatchers(HttpMethod.DELETE, "/api/classes/{classId}/schedule").authenticated() // 강의 일정 삭제, 강사만 가능
                        .requestMatchers(HttpMethod.POST, "/api/classes/{classId}/schedule").authenticated()        // 강의 일정 생성하기, 강사만 가능
                        .requestMatchers(HttpMethod.GET, "/api/classes/{classId}/schedule").authenticated()         // 강의 일정 불러오기, 수강자와 강사만 가능
                        .anyRequest().permitAll()  // 나머지 요청은 인증 없이 허용

                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // 401, 403 에러에 대한 핸들러 설정
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        // JWT 필터 추가
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // AuthenticationManager 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
