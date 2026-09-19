package com.faculdade.pesquisa.controller;

import com.faculdade.pesquisa.domain.Perfil;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.CadastroRequest;
import com.faculdade.pesquisa.dto.LoginRequest;
import com.faculdade.pesquisa.dto.LoginResponse;
import com.faculdade.pesquisa.security.JwtService;
import com.faculdade.pesquisa.security.UsuarioAutenticado;
import com.faculdade.pesquisa.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
        return montarResposta(usuario);
    }

    /** Cadastro publico: todo mundo que se registra entra como CLIENTE. */
    @PostMapping("/cadastrar")
    public ResponseEntity<LoginResponse> cadastrar(@RequestBody @Valid CadastroRequest request) {
        Usuario usuario = usuarioService.cadastrar(request, Perfil.CLIENTE);
        return ResponseEntity.status(HttpStatus.CREATED).body(montarResposta(usuario));
    }

    /** Devolve os dados do usuario do token; o front usa para validar a sessao. */
    @GetMapping("/eu")
    public LoginResponse eu(Authentication autenticacao) {
        Usuario usuario = ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
        return new LoginResponse(
                null,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name());
    }

    private LoginResponse montarResposta(Usuario usuario) {
        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getPerfil().name(), usuario.getId());
        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name());
    }
}
