// Lista de anexos do chamado + upload. Arquivo fica salvo no próprio banco
// (sem bucket externo), limite de 5 MB validado pelo backend.

import { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch, apiFetchBlob } from '../api.js';

function formatarTamanho(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function Anexos({ chamado }) {
  const [anexos, setAnexos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [baixandoId, setBaixandoId] = useState(null);
  const inputRef = useRef(null);

  const encerrado = chamado.status === 'ENCERRADO';

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro('');
    try {
      const dados = await apiFetch(`/api/chamados/${chamado.id}/anexos`, { method: 'GET' });
      setAnexos(dados);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setCarregando(false);
    }
  }, [chamado.id]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function aoEscolherArquivo(evento) {
    const arquivo = evento.target.files?.[0];
    evento.target.value = ''; // permite escolher o mesmo arquivo de novo depois
    if (!arquivo) return;

    setEnviando(true);
    setErro('');
    try {
      const formData = new FormData();
      formData.append('arquivo', arquivo);
      const novo = await apiFetch(`/api/chamados/${chamado.id}/anexos`, {
        method: 'POST',
        body: formData,
      });
      setAnexos((atual) => [...atual, novo]);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setEnviando(false);
    }
  }

  async function baixar(anexo) {
    setBaixandoId(anexo.id);
    setErro('');
    try {
      const blob = await apiFetchBlob(`/api/chamados/${chamado.id}/anexos/${anexo.id}`);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = anexo.nomeArquivo;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setBaixandoId(null);
    }
  }

  return (
    <div className="anexos">
      <div className="anexos-cabecalho">
        <h3>Anexos</h3>
        {!encerrado && (
          <>
            <input
              ref={inputRef}
              type="file"
              hidden
              onChange={aoEscolherArquivo}
            />
            <button
              type="button"
              className="botao botao-secundario botao-pequeno"
              disabled={enviando}
              onClick={() => inputRef.current?.click()}
            >
              {enviando ? 'Enviando...' : '+ Anexar arquivo'}
            </button>
          </>
        )}
      </div>

      {carregando && <div className="estado-carregando">Carregando anexos...</div>}
      {!carregando && erro && <div className="mensagem-erro">{erro}</div>}
      {!carregando && anexos.length === 0 && (
        <div className="estado-vazio estado-vazio-compacto">Nenhum anexo enviado.</div>
      )}

      {!carregando && anexos.length > 0 && (
        <ul className="anexos-lista">
          {anexos.map((anexo) => (
            <li key={anexo.id}>
              <button
                type="button"
                className="anexo-item"
                disabled={baixandoId === anexo.id}
                onClick={() => baixar(anexo)}
              >
                <span className="anexo-nome">{anexo.nomeArquivo}</span>
                <span className="anexo-meta">
                  {formatarTamanho(anexo.tamanhoBytes)} · {anexo.autorNome}
                  {baixandoId === anexo.id ? ' · baixando...' : ''}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
