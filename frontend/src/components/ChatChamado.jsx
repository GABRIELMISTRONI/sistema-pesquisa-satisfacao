// Chat simples dentro do chamado: REST + polling (sem WebSocket).
// Enquanto o painel está aberto, busca mensagens novas a cada 4s; pausa
// quando a aba perde foco e retoma (com busca imediata) ao voltar.

import { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../api.js';

const INTERVALO_POLLING_MS = 4000;

function formatarHora(iso) {
  return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

export default function ChatChamado({ chamado, usuario }) {
  const [mensagens, setMensagens] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [texto, setTexto] = useState('');
  const [enviando, setEnviando] = useState(false);

  const ultimoIdRef = useRef(null);
  const encerrado = chamado.status === 'ENCERRADO';

  const buscarNovas = useCallback(async () => {
    try {
      const query = ultimoIdRef.current ? `?apos=${ultimoIdRef.current}` : '';
      const novas = await apiFetch(`/api/chamados/${chamado.id}/mensagens${query}`, { method: 'GET' });
      if (novas.length > 0) {
        ultimoIdRef.current = novas[novas.length - 1].id;
        setMensagens((atual) => [...atual, ...novas]);
      }
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    }
  }, [chamado.id]);

  // Carga inicial.
  useEffect(() => {
    let cancelado = false;
    (async () => {
      setCarregando(true);
      setErro('');
      try {
        const dados = await apiFetch(`/api/chamados/${chamado.id}/mensagens`, { method: 'GET' });
        if (cancelado) return;
        setMensagens(dados);
        ultimoIdRef.current = dados.length > 0 ? dados[dados.length - 1].id : null;
      } catch (erroRequisicao) {
        if (!cancelado) setErro(erroRequisicao.message);
      } finally {
        if (!cancelado) setCarregando(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [chamado.id]);

  // Polling: para enquanto a aba está em segundo plano, retoma (com busca
  // imediata) quando volta ao primeiro plano.
  useEffect(() => {
    const intervalo = setInterval(() => {
      if (!document.hidden) {
        buscarNovas();
      }
    }, INTERVALO_POLLING_MS);

    function aoVoltarFoco() {
      if (!document.hidden) {
        buscarNovas();
      }
    }
    document.addEventListener('visibilitychange', aoVoltarFoco);

    return () => {
      clearInterval(intervalo);
      document.removeEventListener('visibilitychange', aoVoltarFoco);
    };
  }, [buscarNovas]);

  async function enviar(evento) {
    evento.preventDefault();
    const texy = texto.trim();
    if (!texy) return;

    setEnviando(true);
    setErro('');
    try {
      const nova = await apiFetch(`/api/chamados/${chamado.id}/mensagens`, {
        method: 'POST',
        body: JSON.stringify({ texto: texy }),
      });
      setMensagens((atual) => [...atual, nova]);
      ultimoIdRef.current = nova.id;
      setTexto('');
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="chat-chamado">
      <div className="chat-mensagens">
        {carregando && <div className="estado-carregando">Carregando conversa...</div>}
        {!carregando && mensagens.length === 0 && (
          <div className="estado-vazio">Nenhuma mensagem ainda. Comece a conversa.</div>
        )}
        {mensagens.map((mensagem) => {
          const propria = mensagem.autorId === usuario?.usuarioId;
          return (
            <div key={mensagem.id} className={`chat-balao ${propria ? 'chat-balao-proprio' : 'chat-balao-outro'}`}>
              {!propria && (
                <div className="chat-balao-autor">{mensagem.autorNome} · {mensagem.autorPerfil}</div>
              )}
              <div className="chat-balao-texto">{mensagem.texto}</div>
              <div className="chat-balao-hora">{formatarHora(mensagem.criadaEm)}</div>
            </div>
          );
        })}
      </div>

      {erro && <div className="mensagem-erro">{erro}</div>}

      {encerrado ? (
        <div className="chat-encerrado-aviso">Chamado encerrado — conversa finalizada.</div>
      ) : (
        <form className="chat-form" onSubmit={enviar}>
          <input
            type="text"
            placeholder="Escreva uma mensagem..."
            maxLength={2000}
            value={texto}
            onChange={(evento) => setTexto(evento.target.value)}
            disabled={enviando}
          />
          <button type="submit" className="botao botao-pequeno" disabled={enviando || !texto.trim()}>
            Enviar
          </button>
        </form>
      )}
    </div>
  );
}
