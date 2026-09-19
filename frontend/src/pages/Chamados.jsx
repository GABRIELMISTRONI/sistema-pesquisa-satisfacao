// Tela principal: abertura de chamados, lista, filtro e ações de atendimento.

import { useCallback, useEffect, useRef, useState } from 'react';
import { apiFetch } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';
import Cabecalho from '../components/Cabecalho.jsx';
import ChatChamado from '../components/ChatChamado.jsx';
import Anexos from '../components/Anexos.jsx';
import { ROTULOS_CLASSIFICACAO, classeClassificacao } from '../nota.js';

const ROTULOS_STATUS = { ABERTO: 'Aberto', EM_ANDAMENTO: 'Em andamento', ENCERRADO: 'Encerrado' };
const CLASSES_STATUS = { ABERTO: 'badge-aberto', EM_ANDAMENTO: 'badge-em-andamento', ENCERRADO: 'badge-encerrado' };
const ROTULOS_PRIORIDADE = { BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta' };
const CLASSES_PRIORIDADE = { BAIXA: 'badge-baixa', MEDIA: 'badge-media', ALTA: 'badge-alta' };

function formatarData(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('pt-BR');
}

export default function Chamados() {
  const { usuario } = useAuth();
  const ehAtendimento = usuario?.perfil === 'ADMIN' || usuario?.perfil === 'ATENDENTE';

  const [chamados, setChamados] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erroLista, setErroLista] = useState('');
  const [filtro, setFiltro] = useState('TODOS');

  const carregarChamados = useCallback(async () => {
    setCarregando(true);
    setErroLista('');
    try {
      const dados = await apiFetch('/api/chamados', { method: 'GET' });
      setChamados(dados);
    } catch (erro) {
      setErroLista(erro.message);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregarChamados();
  }, [carregarChamados]);

  const chamadosFiltrados = filtro === 'TODOS'
    ? chamados
    : chamados.filter((c) => c.status === filtro);

  return (
    <>
      <Cabecalho />
      <main className="container">
        <FormularioNovoChamado aoCriar={carregarChamados} />

        <div className="cartao">
          <h2 className="cartao-titulo">Chamados</h2>
          <div className="filtros">
            {['TODOS', 'ABERTO', 'EM_ANDAMENTO', 'ENCERRADO'].map((status) => (
              <button
                key={status}
                type="button"
                className={`filtro-botao ${filtro === status ? 'ativo' : ''}`}
                onClick={() => setFiltro(status)}
              >
                {status === 'TODOS' ? 'Todos' : ROTULOS_STATUS[status]}
              </button>
            ))}
          </div>

          {carregando && <div className="estado-carregando">Carregando chamados...</div>}
          {!carregando && erroLista && <div className="mensagem-erro">{erroLista}</div>}
          {!carregando && !erroLista && chamadosFiltrados.length === 0 && (
            <div className="estado-vazio">Nenhum chamado encontrado.</div>
          )}
          {!carregando && !erroLista && chamadosFiltrados.length > 0 && (
            <div className="lista-chamados">
              {chamadosFiltrados.map((chamado) => (
                <ChamadoCard
                  key={chamado.id}
                  chamado={chamado}
                  usuario={usuario}
                  ehAtendimento={ehAtendimento}
                  aoAtualizar={carregarChamados}
                />
              ))}
            </div>
          )}
        </div>
      </main>
    </>
  );
}

function FormularioNovoChamado({ aoCriar }) {
  const [aberto, setAberto] = useState(false);
  const [titulo, setTitulo] = useState('');
  const [descricao, setDescricao] = useState('');
  const [prioridade, setPrioridade] = useState('');
  const [arquivo, setArquivo] = useState(null);
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const inputArquivoRef = useRef(null);

  function limparCampos() {
    setTitulo('');
    setDescricao('');
    setPrioridade('');
    setArquivo(null);
    if (inputArquivoRef.current) inputArquivoRef.current.value = '';
  }

  async function aoSubmeter(evento) {
    evento.preventDefault();
    setErro('');
    setEnviando(true);
    try {
      // 1) Cria o chamado normalmente.
      const novoChamado = await apiFetch('/api/chamados', {
        method: 'POST',
        body: JSON.stringify({ titulo, descricao, prioridade: prioridade || null }),
      });

      // 2) Se um arquivo foi escolhido, anexa no chamado recém-criado.
      //    Se o chamado foi aberto mas o anexo falhar (ex: arquivo grande demais),
      //    avisamos o motivo mas o chamado continua criado — não desfazemos, e o
      //    formulário fica aberto só para mostrar o aviso (o chamado já existe).
      if (arquivo) {
        try {
          const formData = new FormData();
          formData.append('arquivo', arquivo);
          await apiFetch(`/api/chamados/${novoChamado.id}/anexos`, {
            method: 'POST',
            body: formData,
          });
        } catch (erroAnexo) {
          limparCampos();
          await aoCriar();
          setErro(`Chamado #${novoChamado.id} foi aberto, mas o anexo não pôde ser enviado: ${erroAnexo.message}`);
          return;
        }
      }

      limparCampos();
      setAberto(false);
      await aoCriar();
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
    } finally {
      setEnviando(false);
    }
  }

  if (!aberto) {
    return (
      <button type="button" className="botao botao-novo-chamado" onClick={() => setAberto(true)}>
        + Abrir novo chamado
      </button>
    );
  }

  return (
    <div className="cartao">
      <div className="cartao-titulo-linha">
        <h2 className="cartao-titulo">Abrir novo chamado</h2>
        <button type="button" className="botao-fechar" onClick={() => setAberto(false)} aria-label="Fechar">
          ✕
        </button>
      </div>
      <form onSubmit={aoSubmeter}>
        <div className="campo-linha">
          <div className="campo campo-flex">
            <label htmlFor="novo-titulo">Título</label>
            <input
              id="novo-titulo"
              type="text"
              required
              autoFocus
              maxLength={160}
              value={titulo}
              onChange={(evento) => setTitulo(evento.target.value)}
            />
          </div>
          <div className="campo campo-prioridade">
            <label htmlFor="novo-prioridade">Prioridade</label>
            <select
              id="novo-prioridade"
              value={prioridade}
              onChange={(evento) => setPrioridade(evento.target.value)}
            >
              <option value="">Média (padrão)</option>
              <option value="BAIXA">Baixa</option>
              <option value="MEDIA">Média</option>
              <option value="ALTA">Alta</option>
            </select>
          </div>
        </div>
        <div className="campo">
          <label htmlFor="novo-descricao">Descrição</label>
          <textarea
            id="novo-descricao"
            required
            maxLength={2000}
            className="textarea-compacta"
            value={descricao}
            onChange={(evento) => setDescricao(evento.target.value)}
          />
        </div>
        <div className="campo">
          <label htmlFor="novo-anexo">Anexo (opcional, até 5 MB)</label>
          <input
            id="novo-anexo"
            type="file"
            ref={inputArquivoRef}
            onChange={(evento) => setArquivo(evento.target.files?.[0] || null)}
          />
        </div>
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Abrindo...' : 'Abrir chamado'}
        </button>
        {erro && <div className="mensagem-erro">{erro}</div>}
      </form>
    </div>
  );
}

function ChamadoCard({ chamado, usuario, ehAtendimento, aoAtualizar }) {
  const [erro, setErro] = useState('');
  const [processando, setProcessando] = useState(false);
  const [copiado, setCopiado] = useState(false);
  const [expandido, setExpandido] = useState(false);

  const linkPesquisa = chamado.tokenAvaliacao
    ? `${window.location.origin}/pesquisa?token=${chamado.tokenAvaliacao}`
    : null;

  function alternarExpandido() {
    setExpandido((atual) => !atual);
  }

  function aoTeclarCabecalho(evento) {
    if (evento.key === 'Enter' || evento.key === ' ') {
      evento.preventDefault();
      alternarExpandido();
    }
  }

  async function atender(evento) {
    evento.stopPropagation();
    setProcessando(true);
    setErro('');
    try {
      await apiFetch(`/api/chamados/${chamado.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status: 'EM_ANDAMENTO' }),
      });
      await aoAtualizar();
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
      setProcessando(false);
    }
  }

  async function encerrar(evento) {
    evento.stopPropagation();
    setProcessando(true);
    setErro('');
    try {
      await apiFetch(`/api/chamados/${chamado.id}/encerrar`, { method: 'POST' });
      await aoAtualizar();
    } catch (erroRequisicao) {
      setErro(erroRequisicao.message);
      setProcessando(false);
    }
  }

  async function copiarLink(evento) {
    evento.stopPropagation();
    try {
      await navigator.clipboard.writeText(linkPesquisa);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2000);
    } catch {
      setErro('Não foi possível copiar o link.');
    }
  }

  return (
    <div className={`chamado-card ${expandido ? 'expandido' : ''}`}>
      <div
        className="chamado-cabecalho-clicavel"
        role="button"
        tabIndex={0}
        onClick={alternarExpandido}
        onKeyDown={aoTeclarCabecalho}
        aria-expanded={expandido}
      >
        <div className="chamado-topo">
          <div className="chamado-titulo">#{chamado.id} — {chamado.titulo}</div>
          <div className="chamado-badges">
            <span className={`badge ${CLASSES_STATUS[chamado.status]}`}>{ROTULOS_STATUS[chamado.status]}</span>
            <span className={`badge ${CLASSES_PRIORIDADE[chamado.prioridade]}`}>{ROTULOS_PRIORIDADE[chamado.prioridade]}</span>
          </div>
        </div>
        <div className="chamado-resumo-linha">
          <span>Solicitante: {chamado.solicitante}</span>
          <span>Aberto em: {formatarData(chamado.criadoEm)}</span>
          {chamado.status === 'ENCERRADO' && chamado.nota != null && (
            <span className={`nota-pill ${classeClassificacao(chamado.classificacao)}`}>
              {chamado.nota}/10 · {ROTULOS_CLASSIFICACAO[chamado.classificacao] || chamado.classificacao}
            </span>
          )}
          <span className="chamado-chevron">{expandido ? '▾' : '▸'}</span>
        </div>
      </div>

      {expandido && (
        <div className="chamado-corpo">
          <div className="chamado-descricao">{chamado.descricao}</div>
          <div className="chamado-meta">
            <span>Responsável: {chamado.responsavel || '—'}</span>
            {chamado.encerradoEm && <span>Encerrado em: {formatarData(chamado.encerradoEm)}</span>}
          </div>
          <div className="chamado-acoes">
            {ehAtendimento && chamado.status !== 'ENCERRADO' && (
              <>
                {chamado.status === 'ABERTO' && (
                  <button
                    type="button"
                    className="botao botao-secundario botao-pequeno"
                    disabled={processando}
                    onClick={atender}
                  >
                    Atender
                  </button>
                )}
                <button
                  type="button"
                  className="botao botao-perigo botao-pequeno"
                  disabled={processando}
                  onClick={encerrar}
                >
                  Encerrar
                </button>
              </>
            )}
            {/* Só o cliente pode acessar o link e responder — admin/atendente não podem
                avaliar o próprio atendimento. Eles só acompanham se já foi respondida. */}
            {chamado.status === 'ENCERRADO' && chamado.nota == null && linkPesquisa && usuario?.perfil === 'CLIENTE' && (
              <div className="link-pesquisa-caixa">
                <input type="text" readOnly value={linkPesquisa} onClick={(e) => e.stopPropagation()} />
                <button type="button" className="botao botao-secundario botao-pequeno" onClick={copiarLink}>
                  {copiado ? 'Copiado!' : 'Copiar link da pesquisa'}
                </button>
              </div>
            )}
            {chamado.status === 'ENCERRADO' && chamado.nota == null && usuario?.perfil !== 'CLIENTE' && (
              <span className="pesquisa-pendente-aviso">Aguardando resposta do cliente</span>
            )}
          </div>
          {erro && <div className="mensagem-erro">{erro}</div>}

          <Anexos chamado={chamado} />
          <ChatChamado chamado={chamado} usuario={usuario} />
        </div>
      )}
    </div>
  );
}
