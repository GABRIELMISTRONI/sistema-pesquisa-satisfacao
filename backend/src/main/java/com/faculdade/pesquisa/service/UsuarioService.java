package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Perfil;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.CadastroRequest;
import com.faculdade.pesquisa.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroRequest request, Perfil perfil) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw RegraDeNegocioException.conflito("Ja existe um usuario com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario porEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Usuario nao encontrado"));
    }
}
