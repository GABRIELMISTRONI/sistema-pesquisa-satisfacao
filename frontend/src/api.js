// Camada central de comunicação com a API.
// Toda chamada à API do sistema deve passar por aqui.

// Em producao (Vercel), configurar VITE_API_URL nas variaveis de ambiente do
// projeto apontando para a URL do backend (ex: Render). Sem essa variavel,
// cai no localhost de sempre para desenvolvimento.
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

function tratarSessaoExpirada(resposta, autenticado) {
  if (resposta.status === 401 && autenticado) {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    if (window.location.pathname !== '/') {
      window.location.href = '/';
    }
    throw new Error('Sessão expirada. Faça login novamente.');
  }
}

function montarHeaders(opcoes, autenticado) {
  const headers = { ...(opcoes.headers || {}) };

  // FormData (upload de arquivo) define o próprio Content-Type com o boundary;
  // se a gente forçar 'application/json' aqui, o multipart quebra.
  if (!(opcoes.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (autenticado) {
    const token = localStorage.getItem('token');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  return headers;
}

/**
 * Faz uma chamada à API que devolve JSON.
 * @param {string} caminho - ex: '/api/chamados'
 * @param {object} opcoes - opções extras do fetch (method, body, etc.)
 * @param {boolean} autenticado - se true, injeta o header Authorization
 */
export async function apiFetch(caminho, opcoes = {}, autenticado = true) {
  let resposta;
  try {
    resposta = await fetch(`${API_BASE_URL}${caminho}`, {
      ...opcoes,
      headers: montarHeaders(opcoes, autenticado),
    });
  } catch (erroRede) {
    throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está rodando em ' + API_BASE_URL + '.');
  }

  tratarSessaoExpirada(resposta, autenticado);

  const texto = await resposta.text();
  const dados = texto ? JSON.parse(texto) : null;

  if (!resposta.ok) {
    const mensagem = (dados && dados.mensagem) ? dados.mensagem : 'Erro inesperado ao comunicar com o servidor.';
    throw new Error(mensagem);
  }

  return dados;
}

/**
 * Baixa um arquivo binário (anexo) autenticado e devolve um Blob.
 * Usado no lugar de um <a href> comum porque o download exige o header
 * Authorization, que uma tag <a> não consegue enviar sozinha.
 */
export async function apiFetchBlob(caminho) {
  let resposta;
  try {
    resposta = await fetch(`${API_BASE_URL}${caminho}`, {
      headers: montarHeaders({}, true),
    });
  } catch (erroRede) {
    throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está rodando em ' + API_BASE_URL + '.');
  }

  tratarSessaoExpirada(resposta, true);

  if (!resposta.ok) {
    let mensagem = 'Não foi possível baixar o arquivo.';
    try {
      const dados = await resposta.json();
      if (dados?.mensagem) mensagem = dados.mensagem;
    } catch {
      // corpo nao era JSON, mantem a mensagem padrao
    }
    throw new Error(mensagem);
  }

  return resposta.blob();
}
