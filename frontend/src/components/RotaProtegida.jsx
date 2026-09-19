// Guarda de rota: exige login e, opcionalmente, um perfil específico.

import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function RotaProtegida({ children, perfisPermitidos }) {
  const { usuario } = useAuth();
  const token = localStorage.getItem('token');

  if (!token || !usuario) {
    return <Navigate to="/" replace />;
  }

  if (perfisPermitidos && !perfisPermitidos.includes(usuario.perfil)) {
    return <Navigate to="/chamados" replace />;
  }

  return children;
}
