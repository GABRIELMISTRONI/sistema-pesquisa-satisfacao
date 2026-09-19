package com.faculdade.pesquisa.config;

import com.faculdade.pesquisa.domain.Perfil;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria os usuarios de teste na primeira execucao (banco vazio), para sempre
 * existir um login de cada perfil. Nao cria chamados/pesquisas de exemplo -
 * isso ficava poluindo a base durante os testes.
 */
@Component
public class CargaInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CargaInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Base ja possui usuarios, carga inicial ignorada.");
            return;
        }

        Usuario admin = criarUsuario("Administrador", "admin@empresa.com", "admin123", Perfil.ADMIN);
        criarUsuario("Joao Atendente", "atendente@empresa.com", "atendente123", Perfil.ATENDENTE);
        criarUsuario("Maria Silva", "cliente@empresa.com", "cliente123", Perfil.CLIENTE);

        // Cliente de teste real (integrante do grupo) para validar o envio da
        // pesquisa via Teams de ponta a ponta. E-MAIL PROVISORIO — trocar pelo
        // e-mail/UPN real do Bruno assim que o webhook do Teams for configurado
        // para o chat dele (ver TEAMS_WEBHOOK_URL no application.yml).
        criarUsuario("Bruno Ascielli", "bruno.ascielli@empresa.com", "bruno123", Perfil.CLIENTE);

        log.info("Carga inicial concluida. Login admin: {} / admin123", admin.getEmail());
    }

    private Usuario criarUsuario(String nome, String email, String senha, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }
}
