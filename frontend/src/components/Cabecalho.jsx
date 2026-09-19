// Cabeçalho comum das páginas internas: nome, perfil, links e botão sair.

import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const ROTULOS_PERFIL = { ADMIN: 'Administrador', ATENDENTE: 'Atendente', CLIENTE: 'Cliente' };

export default function Cabecalho() {
  const { usuario, logout } = useAuth();
  const location = useLocation();
  const ehAdmin = usuario?.perfil === 'ADMIN';

  return (
    <header className="cabecalho">
      <Link to="/chamados" className="cabecalho-marca">Central de Atendimento</Link>
      <div className="cabecalho-usuario">
        <div className="cabecalho-links">
          {location.pathname !== '/chamados' && (
            <Link to="/chamados" className="cabecalho-link">Chamados</Link>
          )}
          {ehAdmin && location.pathname !== '/painel' && (
            <Link to="/painel" className="cabecalho-link">Painel de satisfação</Link>
          )}
        </div>
        <span>{usuario?.nome}</span>
        <span className="perfil-badge">{ROTULOS_PERFIL[usuario?.perfil] || usuario?.perfil}</span>
        <button type="button" className="botao botao-secundario botao-pequeno" onClick={logout}>
          Sair
        </button>
      </div>
    </header>
  );
}
