import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Chamados from './pages/Chamados.jsx';
import Painel from './pages/Painel.jsx';
import Pesquisa from './pages/Pesquisa.jsx';
import RotaProtegida from './components/RotaProtegida.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/pesquisa" element={<Pesquisa />} />
      <Route
        path="/chamados"
        element={
          <RotaProtegida>
            <Chamados />
          </RotaProtegida>
        }
      />
      <Route
        path="/painel"
        element={
          <RotaProtegida perfisPermitidos={['ADMIN']}>
            <Painel />
          </RotaProtegida>
        }
      />
    </Routes>
  );
}
