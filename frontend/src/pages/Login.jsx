// Tela de login e cadastro.

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const USUARIOS_TESTE = [
  { email: 'admin@empresa.com', senha: 'admin123', perfil: 'ADMIN' },
  { email: 'atendente@empresa.com', senha: 'atendente123', perfil: 'ATENDENTE' },
  { email: 'cliente@empresa.com', senha: 'cliente123', perfil: 'CLIENTE' },
];

export default function Login() {
  const { usuario, login, cadastrar } = useAuth();
  const navigate = useNavigate();
  const [aba, setAba] = useState('entrar');

  // Se já estiver logado, pula direto para a lista de chamados.
  useEffect(() => {
    if (usuario) {
      navigate('/chamados', { replace: true });
    }
  }, [usuario, navigate]);

  const [loginEmail, setLoginEmail] = useState('');
  const [loginSenha, setLoginSenha] = useState('');
  const [erroLogin, setErroLogin] = useState('');
  const [carregandoLogin, setCarregandoLogin] = useState(false);

  const [cadNome, setCadNome] = useState('');
  const [cadEmail, setCadEmail] = useState('');
  const [cadSenha, setCadSenha] = useState('');
  const [erroCad, setErroCad] = useState('');
  const [carregandoCad, setCarregandoCad] = useState(false);

  async function aoEntrar(evento) {
    evento.preventDefault();
    setErroLogin('');
    setCarregandoLogin(true);
    try {
      await login(loginEmail.trim(), loginSenha);
      navigate('/chamados');
    } catch (erro) {
      setErroLogin(erro.message);
    } finally {
      setCarregandoLogin(false);
    }
  }

  async function aoCadastrar(evento) {
    evento.preventDefault();
    setErroCad('');
    setCarregandoCad(true);
    try {
      await cadastrar(cadNome.trim(), cadEmail.trim(), cadSenha);
      navigate('/chamados');
    } catch (erro) {
      setErroCad(erro.message);
    } finally {
      setCarregandoCad(false);
    }
  }

  return (
    <div className="container-estreito">
      <div className="logo-topo">
        <h1>Central de Atendimento</h1>
        <p>Sistema de chamados e pesquisa de satisfação</p>
      </div>

      <div className="cartao">
        <div className="abas">
          <button
            type="button"
            className={`aba ${aba === 'entrar' ? 'ativa' : ''}`}
            onClick={() => setAba('entrar')}
          >
            Entrar
          </button>
          <button
            type="button"
            className={`aba ${aba === 'criar' ? 'ativa' : ''}`}
            onClick={() => setAba('criar')}
          >
            Criar conta
          </button>
        </div>

        {aba === 'entrar' ? (
          <form onSubmit={aoEntrar}>
            <div className="campo">
              <label htmlFor="login-email">E-mail</label>
              <input
                id="login-email"
                type="email"
                required
                autoComplete="email"
                value={loginEmail}
                onChange={(evento) => setLoginEmail(evento.target.value)}
              />
            </div>
            <div className="campo">
              <label htmlFor="login-senha">Senha</label>
              <input
                id="login-senha"
                type="password"
                required
                autoComplete="current-password"
                value={loginSenha}
                onChange={(evento) => setLoginSenha(evento.target.value)}
              />
            </div>
            <button type="submit" className="botao botao-bloco" disabled={carregandoLogin}>
              {carregandoLogin ? 'Entrando...' : 'Entrar'}
            </button>
            {erroLogin && <div className="mensagem-erro">{erroLogin}</div>}
          </form>
        ) : (
          <form onSubmit={aoCadastrar}>
            <div className="campo">
              <label htmlFor="cadastro-nome">Nome</label>
              <input
                id="cadastro-nome"
                type="text"
                required
                maxLength={120}
                autoComplete="name"
                value={cadNome}
                onChange={(evento) => setCadNome(evento.target.value)}
              />
            </div>
            <div className="campo">
              <label htmlFor="cadastro-email">E-mail</label>
              <input
                id="cadastro-email"
                type="email"
                required
                maxLength={160}
                autoComplete="email"
                value={cadEmail}
                onChange={(evento) => setCadEmail(evento.target.value)}
              />
            </div>
            <div className="campo">
              <label htmlFor="cadastro-senha">Senha</label>
              <input
                id="cadastro-senha"
                type="password"
                required
                minLength={6}
                maxLength={72}
                autoComplete="new-password"
                value={cadSenha}
                onChange={(evento) => setCadSenha(evento.target.value)}
              />
            </div>
            <button type="submit" className="botao botao-bloco" disabled={carregandoCad}>
              {carregandoCad ? 'Criando conta...' : 'Criar conta'}
            </button>
            {erroCad && <div className="mensagem-erro">{erroCad}</div>}
          </form>
        )}
      </div>

      <div className="usuarios-teste">
        <strong>Usuários de teste (protótipo acadêmico)</strong>
        <table>
          <tbody>
            {USUARIOS_TESTE.map((u) => (
              <tr key={u.email}>
                <td>{u.email}</td>
                <td>{u.senha}</td>
                <td>{u.perfil}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
