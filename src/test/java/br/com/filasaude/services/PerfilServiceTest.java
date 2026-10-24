package br.com.filasaude.services;

import br.com.filasaude.entities.Perfil;
import br.com.filasaude.exceptions.RegraNegocioException;
import br.com.filasaude.repositories.PerfilRepository;
import br.com.filasaude.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PerfilServiceTest {
    private final PerfilRepository perfis = mock(PerfilRepository.class);
    private final PerfilService service = new PerfilService(perfis, mock(UsuarioRepository.class));

    @Test
    void impedeExcluirPerfilEssencialMesmoSemUsuariosVinculados() {
        when(perfis.findById(1L)).thenReturn(Optional.of(new Perfil(1L, "ADMIN", "")));
        assertThrows(RegraNegocioException.class, () -> service.excluir(1L));
    }

    @Test
    void impedeRenomearPerfilEssencial() {
        when(perfis.findById(1L)).thenReturn(Optional.of(new Perfil(1L, "ADMIN", "")));
        Perfil alterado = new Perfil(1L, "GESTOR", "");
        assertThrows(RegraNegocioException.class, () -> service.salvar(alterado));
    }
}
