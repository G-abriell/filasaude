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
                        // Administração (perfis, usuários, filas cadastrais)
                        .requestMatchers("/perfis/**", "/usuarios/**", "/filas/**", "/erros/**").hasRole("ADMIN")
                        // Configurações da unidade e da fila: somente administração
                        .requestMatchers("/configuracoes/**").hasRole("ADMIN")
                        // Painel e atualização em tempo real: disponível a qualquer usuário autenticado
                        .requestMatchers("/painel/**").authenticated()
                        // O formulário de cadastro é da recepção (precisa vir antes de "/pacientes/*", que também o casaria)
                        .requestMatchers("/pacientes/novo").hasAnyRole("RECEPCAO", "ADMIN")
                        // Histórico de pacientes e resumo clínico/receita: acessível também pelo profissional de saúde
                        .requestMatchers(HttpMethod.GET, "/pacientes/*", "/atendimentos/*/resumo")
                        .hasAnyRole("RECEPCAO", "PROFISSIONAL_DE_SAUDE", "ADMIN")
                        // Recepção: gestão de pacientes e emissão/cancelamento de senhas
                        .requestMatchers("/pacientes/**", "/atendimentos/novo").hasAnyRole("RECEPCAO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/atendimentos", "/atendimentos/*/cancelar")
                        .hasAnyRole("RECEPCAO", "ADMIN")
                        // Contingência: a recepção chama o próximo em favor de um profissional escolhido
                        .requestMatchers(HttpMethod.POST, "/atendimentos/chamar-em-nome-de")
                        .hasAnyRole("RECEPCAO", "ADMIN")
                        // Não comparecimento: também da recepção (unidades pequenas); não altera a ordem da fila
                        .requestMatchers(HttpMethod.POST, "/atendimentos/*/nao-compareceu")
                        .hasAnyRole("RECEPCAO", "PROFISSIONAL_DE_SAUDE", "ADMIN")
                        // Atendimento (profissional de saúde): chamar, rechamar, iniciar e finalizar
                        .requestMatchers("/atendimentos/guiche", "/atendimentos/chamar",
                                "/atendimentos/*/chamar", "/atendimentos/*/rechamar",
                                "/atendimentos/*/iniciar", "/atendimentos/*/finalizar")
                        .hasAnyRole("PROFISSIONAL_DE_SAUDE", "ADMIN")
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
