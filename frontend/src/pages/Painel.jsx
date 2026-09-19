// Painel de relatório de satisfação (só ADMIN).

import { useEffect, useState } from 'react';
import { apiFetch } from '../api.js';
import Cabecalho from '../components/Cabecalho.jsx';
import { ROTULOS_CLASSIFICACAO, classeClassificacao } from '../nota.js';

function formatarData(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('pt-BR');
}

const ICONES = {
  abertos: '◎',
  andamento: '◐',
  encerrados: '●',
  enviadas: '↗',
  respondidas: '✓',
  taxa: '%',
  media: '★',
};

export default function Painel() {
  const [resumo, setResumo] = useState(null);
  const [erroResumo, setErroResumo] = useState('');
  const [carregandoResumo, setCarregandoResumo] = useState(true);

  const [avaliacoes, setAvaliacoes] = useState([]);
  const [erroAvaliacoes, setErroAvaliacoes] = useState('');
  const [carregandoAvaliacoes, setCarregandoAvaliacoes] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setResumo(await apiFetch('/api/relatorios/resumo', { method: 'GET' }));
      } catch (erro) {
        setErroResumo(erro.message);
      } finally {
        setCarregandoResumo(false);
      }
    })();

    (async () => {
      try {
        setAvaliacoes(await apiFetch('/api/relatorios/avaliacoes', { method: 'GET' }));
      } catch (erro) {
        setErroAvaliacoes(erro.message);
      } finally {
        setCarregandoAvaliacoes(false);
      }
    })();
  }, []);

  return (
    <>
      <Cabecalho />
      <main className="container">
        <h1 className="pagina-titulo">Painel de satisfação</h1>

        {carregandoResumo && <div className="estado-carregando">Carregando resumo...</div>}
        {!carregandoResumo && erroResumo && <div className="mensagem-erro">{erroResumo}</div>}
        {!carregandoResumo && !erroResumo && resumo && <Resumo resumo={resumo} />}

        <div className="cartao">
          <h2 className="cartao-titulo">Avaliações respondidas</h2>
          {carregandoAvaliacoes && <div className="estado-carregando">Carregando avaliações...</div>}
          {!carregandoAvaliacoes && erroAvaliacoes && <div className="mensagem-erro">{erroAvaliacoes}</div>}
          {!carregandoAvaliacoes && !erroAvaliacoes && avaliacoes.length === 0 && (
            <div className="estado-vazio">Nenhuma avaliação respondida ainda.</div>
          )}
          {!carregandoAvaliacoes && !erroAvaliacoes && avaliacoes.length > 0 && (
            <div className="tabela-rolagem">
              <table className="tabela-avaliacoes">
                <thead>
                  <tr>
                    <th>Chamado</th>
                    <th>Cliente</th>
                    <th>Nota</th>
                    <th>Comentário</th>
                    <th>Respondida em</th>
                  </tr>
                </thead>
                <tbody>
                  {avaliacoes.map((avaliacao) => (
                    <tr key={avaliacao.token}>
                      <td>{avaliacao.tituloChamado}</td>
                      <td>{avaliacao.cliente}</td>
                      <td>
                        <span className={`nota-pill ${classeClassificacao(avaliacao.classificacao)}`}>
                          {avaliacao.nota}/10
                        </span>
                      </td>
                      <td>{avaliacao.comentario || '—'}</td>
                      <td>{formatarData(avaliacao.respondidaEm)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </>
  );
}

function Resumo({ resumo }) {
  const notaMediaTexto = resumo.notaMedia != null ? `${resumo.notaMedia.toFixed(2)}/10` : '—';

  const classificacoes = ['BOM', 'RAZOAVEL', 'RUIM'];
  const contagensClassificacao = classificacoes.map((c) => resumo.distribuicaoClassificacao?.[c] || 0);
  const totalRespondidas = contagensClassificacao.reduce((soma, n) => soma + n, 0);

  const contagensNota = Array.from({ length: 10 }, (_, i) => resumo.distribuicaoNotas[String(i + 1)] || 0);
  const maiorContagemNota = Math.max(...contagensNota, 1);

  return (
    <>
      <div className="cartoes-resumo">
        <CartaoNumero icone={ICONES.abertos} valor={resumo.chamadosAbertos} rotulo="Abertos" tom="info" />
        <CartaoNumero icone={ICONES.andamento} valor={resumo.chamadosEmAndamento} rotulo="Em andamento" tom="razoavel" />
        <CartaoNumero icone={ICONES.encerrados} valor={resumo.chamadosEncerrados} rotulo="Encerrados" tom="bom" />
        <CartaoNumero icone={ICONES.enviadas} valor={resumo.pesquisasEnviadas} rotulo="Pesquisas enviadas" tom="neutro" />
        <CartaoNumero icone={ICONES.respondidas} valor={resumo.pesquisasRespondidas} rotulo="Pesquisas respondidas" tom="neutro" />
        <CartaoNumero icone={ICONES.taxa} valor={`${resumo.taxaResposta.toFixed(1)}%`} rotulo="Taxa de resposta" tom="primaria" destaque />
        <CartaoNumero icone={ICONES.media} valor={notaMediaTexto} rotulo="Nota média" tom="primaria" destaque />
      </div>

      <div className="cartao">
        <h2 className="cartao-titulo">Satisfação geral</h2>

        {totalRespondidas === 0 ? (
          <div className="estado-vazio estado-vazio-compacto">Nenhuma pesquisa respondida ainda.</div>
        ) : (
          <>
            <div className="barra-empilhada">
              {classificacoes.map((classificacao, i) => {
                const contagem = contagensClassificacao[i];
                if (contagem === 0) return null;
                const percentual = (contagem / totalRespondidas) * 100;
                return (
                  <div
                    key={classificacao}
                    className={`barra-empilhada-segmento barra-${classificacao.toLowerCase()}`}
                    style={{ width: `${percentual}%` }}
                    title={`${ROTULOS_CLASSIFICACAO[classificacao]}: ${contagem} (${percentual.toFixed(0)}%)`}
                  >
                    {percentual >= 12 && `${percentual.toFixed(0)}%`}
                  </div>
                );
              })}
            </div>
            <div className="legenda-satisfacao">
              {classificacoes.map((classificacao, i) => {
                const contagem = contagensClassificacao[i];
                const percentual = totalRespondidas ? (contagem / totalRespondidas) * 100 : 0;
                return (
                  <span className="legenda-item legenda-item-fixa" key={classificacao}>
                    <i className={`legenda-ponto nota-${classificacao.toLowerCase()}`} />
                    {ROTULOS_CLASSIFICACAO[classificacao]}
                    <strong>{contagem}</strong>
                    <span className="legenda-percentual">({percentual.toFixed(0)}%)</span>
                  </span>
                );
              })}
            </div>
          </>
        )}
      </div>

      <div className="cartao">
        <h2 className="cartao-titulo">Distribuição das notas (1 a 10)</h2>
        {totalRespondidas === 0 ? (
          <div className="estado-vazio estado-vazio-compacto">Nenhuma pesquisa respondida ainda.</div>
        ) : (
          <div className="grafico-colunas">
            {contagensNota.map((contagem, i) => {
              const nota = i + 1;
              const alturaPercentual = Math.max((contagem / maiorContagemNota) * 100, contagem > 0 ? 6 : 0);
              return (
                <div className="grafico-coluna" key={nota}>
                  <div className="grafico-coluna-trilho">
                    <div
                      className="grafico-coluna-barra"
                      style={{ height: `${alturaPercentual}%` }}
                      title={`Nota ${nota}: ${contagem} resposta(s)`}
                    />
                  </div>
                  <span className="grafico-coluna-rotulo">{nota}</span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </>
  );
}

function CartaoNumero({ icone, valor, rotulo, tom = 'neutro', destaque = false }) {
  return (
    <div className={`cartao-numero tom-${tom} ${destaque ? 'cartao-numero-destaque' : ''}`}>
      <div className="cartao-numero-icone">{icone}</div>
      <div className="cartao-numero-texto">
        <div className="valor">{valor}</div>
        <div className="rotulo">{rotulo}</div>
      </div>
    </div>
  );
}
