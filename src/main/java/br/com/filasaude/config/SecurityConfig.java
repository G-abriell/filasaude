package br.com.filasaude.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/favicon.ico", "/favicon.svg", "/css/**", "/js/**").permitAll()
                        // Administração
                        .requestMatchers("/perfis/**", "/usuarios/**", "/filas/**", "/configuracoes/**").hasRole("ADMIN")
                        // Recepção
                        .requestMatchers("/pacientes/**", "/atendimentos/novo").hasAnyRole("RECEPCAO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/atendimentos", "/atendimentos/*/cancelar")
                        .hasAnyRole("RECEPCAO", "ADMIN")
                        // Atendimento (profissional de saúde): chamar, iniciar e finalizar
                        .requestMatchers("/atendimentos/guiche", "/atendimentos/chamar",
                                "/atendimentos/*/iniciar", "/atendimentos/*/finalizar",
                                "/atendimentos/*/nao-compareceu").hasAnyRole("PROFISSIONAL_DE_SAUDE", "ADMIN")
                        // Início, atendimentos do dia e acesso negado: qualquer usuário logado
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .defaultSuccessUrl("/inicio", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/acesso-negado"));
        return http.build();
    }
}
