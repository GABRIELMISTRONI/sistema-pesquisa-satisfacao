// Pesquisa pública de satisfação. Não usa autenticação nem localStorage.

import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { apiFetch } from '../api.js';
import { classeClassificacao, classificarNota } from '../nota.js';

export default function Pesquisa() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  // carregando | sem-token | nao-encontrada | ja-respondida | formulario | agradecimento
  const [estado, setEstado] = useState('carregando');
  const [avaliacao, setAvaliacao] = useState(null);
  const [mensagemErroCarregamento, setMensagemErroCarregamento] = useState('');

  const [nota, setNota] = useState(0);
  const [notaEmFoco, setNotaEmFoco] = useState(0);
  const [comentario, setComentario] = useState('');
  const [erroEnvio, setErroEnvio] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState(null);

  useEffect(() => {
    if (!token) {
      setEstado('sem-token');
      return;
    }

    (async () => {
      try {
        const dados = await apiFetch(`/api/pesquisa/${token}`, { method: 'GET' }, false);
        setAvaliacao(dados);
        setEstado(dados.respondida ? 'ja-respondida' : 'formulario');
      } catch (erro) {
        setMensagemErroCarregamento(erro.message);
        setEstado('nao-encontrada');
      }
    })();
  }, [token]);

  async function aoEnviar(evento) {
    evento.preventDefault();
    setErroEnvio('');

    if (nota < 1 || nota > 10) {
      setErroEnvio('Selecione uma nota de 1 a 10.');
      return;
    }

    setEnviando(true);
    try {
      const dados = await apiFetch(`/api/pesquisa/${token}`, {
        method: 'POST',
        body: JSON.stringify({ nota, comentario: comentario.trim() || null }),
      }, false);
      setResultado(dados);
      setEstado('agradecimento');
    } catch (erro) {
      setErroEnvio(erro.message);
    } finally {
      setEnviando(false);
    }
  }

  const notaDestacada = notaEmFoco || nota;

  return (
    <div className="container-estreito">
      <div className="logo-topo">
        <div className="logo-icone">★</div>
        <h1>Pesquisa de satisfação</h1>
        <p>Conte para nós como foi o seu atendimento</p>
      </div>

      <div className="cartao cartao-pesquisa">
        {estado === 'carregando' && (
          <div className="estado-carregando">Carregando pesquisa...</div>
        )}

        {estado === 'sem-token' && (
          <div className="estado-vazio">Link inválido: nenhum código de pesquisa foi informado.</div>
        )}

        {estado === 'nao-encontrada' && (
          <div className="mensagem-erro">{mensagemErroCarregamento}</div>
        )}

        {estado === 'ja-respondida' && avaliacao && (
          <div className="pesquisa-resultado">
            <div className="pesquisa-resultado-icone">✓</div>
            <p className="pesquisa-resultado-titulo">Esta pesquisa já foi respondida</p>
            <p className="pesquisa-resultado-chamado">{avaliacao.tituloChamado}</p>
            <div className={`nota-pill nota-pill-grande ${classeClassificacao(avaliacao.classificacao)}`}>
              {avaliacao.nota}/10
            </div>
            {avaliacao.comentario && (
              <p className="pesquisa-resultado-comentario">“{avaliacao.comentario}”</p>
            )}
          </div>
        )}

        {estado === 'formulario' && avaliacao && (
          <div>
            <div className="pesquisa-info-chamado">
              <span className="pesquisa-info-rotulo">Chamado</span>
              <span className="pesquisa-info-valor">{avaliacao.tituloChamado}</span>
            </div>

            <form onSubmit={aoEnviar}>
              <div className="campo">
                <label>De 1 a 10, como foi o atendimento?</label>
                <div className="selecao-nota">
                  {Array.from({ length: 10 }, (_, i) => i + 1).map((valor) => {
                    const classe = classeClassificacao(classificarNota(valor));
                    return (
                      <button
                        key={valor}
                        type="button"
                        className={`${classe} ${valor === nota ? 'selecionada' : ''}`}
                        onClick={() => setNota(valor)}
                        onMouseEnter={() => setNotaEmFoco(valor)}
                        onMouseLeave={() => setNotaEmFoco(0)}
                      >
                        {valor}
                      </button>
                    );
                  })}
                </div>
                <div className="legenda-nota">
                  <span className={`legenda-item ${notaDestacada > 0 && notaDestacada <= 6 ? 'legenda-ativa' : ''}`}>
                    <i className="legenda-ponto nota-ruim" /> 1–6 ruim
                  </span>
                  <span className={`legenda-item ${notaDestacada >= 7 && notaDestacada <= 8 ? 'legenda-ativa' : ''}`}>
                    <i className="legenda-ponto nota-razoavel" /> 7–8 razoável
                  </span>
                  <span className={`legenda-item ${notaDestacada >= 9 ? 'legenda-ativa' : ''}`}>
                    <i className="legenda-ponto nota-bom" /> 9–10 bom
                  </span>
                </div>
              </div>
              <div className="campo">
                <label htmlFor="comentario-pesquisa">Quer contar mais alguma coisa? (opcional)</label>
                <textarea
                  id="comentario-pesquisa"
                  placeholder="Conte um pouco sobre a sua experiência..."
                  maxLength={1000}
                  value={comentario}
                  onChange={(evento) => setComentario(evento.target.value)}
                />
              </div>
              <button type="submit" className="botao botao-bloco" disabled={enviando || nota === 0}>
                {enviando ? 'Enviando...' : 'Enviar minha avaliação'}
              </button>
              {erroEnvio && <div className="mensagem-erro">{erroEnvio}</div>}
            </form>
          </div>
        )}

        {estado === 'agradecimento' && resultado && (
          <div className="pesquisa-resultado">
            <div className="pesquisa-resultado-icone">✓</div>
            <p className="pesquisa-resultado-titulo">Obrigado por avaliar nosso atendimento!</p>
            <div className={`nota-pill nota-pill-grande ${classeClassificacao(resultado.classificacao)}`}>
              {resultado.nota}/10
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
