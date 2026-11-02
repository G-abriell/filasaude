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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final AtendimentoRepository atendimentos = mock(AtendimentoRepository.class);
    private final PerfilService perfis = mock(PerfilService.class);
    private final UsuarioService service = new UsuarioService(usuarios, atendimentos, perfis, mock(PasswordEncoder.class));

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    @Test
    void impedeAutoDesativacao() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        logadoComo("admin@exemplo.com");

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L));
        assertTrue(admin.isAtivo());
    }

    @Test
    void impedeDesativarUltimoAdministradorAtivo() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(List.of(admin));

        assertThrows(RegraNegocioException.class, () -> service.alternarAtivo(1L));
        assertTrue(admin.isAtivo());
    }

    @Test
    void impedeExcluirAPropriaConta() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        logadoComo("ADMIN@exemplo.com");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
        assertEquals("Não é possível excluir a própria conta.", erro.getMessage());
        verify(usuarios, never()).delete(any());
    }

    @Test
    void impedeExcluirUltimoAdministradorAtivo() {
        Usuario admin = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(List.of(admin));
        logadoComo("outro@exemplo.com");

        assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
        verify(usuarios, never()).delete(any());
    }

    @Test
    void permiteExcluirAdministradorQuandoHaOutroAtivo() {
        Usuario admin1 = usuario(1L, "admin1@exemplo.com", "ADMIN");
        Usuario admin2 = usuario(2L, "admin2@exemplo.com", "ADMIN");
        when(usuarios.findById(2L)).thenReturn(Optional.of(admin2));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(List.of(admin1, admin2));
        logadoComo("admin1@exemplo.com");

        service.excluir(2L);

        verify(usuarios).delete(admin2);
    }

    @Test
    void naoExigeOutroAdministradorParaExcluirUsuarioComum() {
        Usuario recepcao = usuario(3L, "recepcao@exemplo.com", "RECEPCAO");
        when(usuarios.findById(3L)).thenReturn(Optional.of(recepcao));

        service.excluir(3L);

        verify(usuarios).delete(recepcao);
        verify(usuarios, never()).findAdministradoresAtivosComLock();
    }

    @Test
    void impedeRebaixarOUltimoAdministradorAtivoPelaEdicao() {
        Usuario atual = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(atual));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(List.of(atual));
        Perfil recepcao = new Perfil(2L, "RECEPCAO", "");
        when(perfis.buscarPorId(2L)).thenReturn(recepcao);

        Usuario formulario = usuario(1L, "admin@exemplo.com", "RECEPCAO");
        formulario.getPerfil().setId(2L);

        assertThrows(RegraNegocioException.class, () -> service.salvar(formulario));
        verify(usuarios, never()).save(any());
    }

    @Test
    void impedeDesativarOUltimoAdministradorPelaEdicao() {
        Usuario atual = usuario(1L, "admin@exemplo.com", "ADMIN");
        when(usuarios.findById(1L)).thenReturn(Optional.of(atual));
        when(usuarios.findAdministradoresAtivosComLock()).thenReturn(List.of(atual));
        when(perfis.buscarPorId(1L)).thenReturn(new Perfil(1L, "ADMIN", ""));

        Usuario formulario = usuario(1L, "admin@exemplo.com", "ADMIN");
        formulario.setAtivo(false);

        assertThrows(RegraNegocioException.class, () -> service.salvar(formulario));
        verify(usuarios, never()).save(any());
    }

    private void logadoComo(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, ""));
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
