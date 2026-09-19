// Contexto de autenticação: guarda o usuário logado e expõe login/cadastro/logout.

import { createContext, useCallback, useContext, useState } from 'react';
import { apiFetch } from '../api.js';

const AuthContext = createContext(null);

function lerUsuarioSalvo() {
  const bruto = localStorage.getItem('usuario');
  return bruto ? JSON.parse(bruto) : null;
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(lerUsuarioSalvo);

  const salvarSessao = useCallback((loginResponse) => {
    localStorage.setItem('token', loginResponse.token);
    const dadosUsuario = {
      usuarioId: loginResponse.usuarioId,
      nome: loginResponse.nome,
      email: loginResponse.email,
      perfil: loginResponse.perfil,
    };
    localStorage.setItem('usuario', JSON.stringify(dadosUsuario));
    setUsuario(dadosUsuario);
  }, []);

  const login = useCallback(async (email, senha) => {
    const dados = await apiFetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    }, false);
    salvarSessao(dados);
    return dados;
  }, [salvarSessao]);

  const cadastrar = useCallback(async (nome, email, senha) => {
    const dados = await apiFetch('/api/auth/cadastrar', {
      method: 'POST',
      body: JSON.stringify({ nome, email, senha }),
    }, false);
    salvarSessao(dados);
    return dados;
  }, [salvarSessao]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    setUsuario(null);
  }, []);

  return (
    <AuthContext.Provider value={{ usuario, login, cadastrar, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
