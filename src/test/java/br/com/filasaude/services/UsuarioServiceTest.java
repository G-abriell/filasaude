package br.com.filasaude.services;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.entities.Usuario;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.AtendimentoRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final UsuarioService service = new UsuarioService(usuarios, mock(AtendimentoRepository.class),
            mock(PerfilService.class), mock(PasswordEncoder.class));

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    @Test
    void impedeAutoDesativacao() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(java.util.Optional.of(admin));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@exemplo.com", ""));

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L));
        assertTrue(admin.isAtivo());
    }

    @Test
    void impedeDesativarUltimoAdministradorAtivo() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(java.util.Optional.of(admin));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(java.util.List.of(admin));

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L));
        assertTrue(admin.isAtivo());
    }

    private Usuario usuario(Long id, String email, String perfilNome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setAtivo(true);
        usuario.setPerfil(new Perfil(1L, perfilNome, ""));
        return usuario;
    }
}
