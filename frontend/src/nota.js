// Helpers para exibir a nota de satisfação (escala 1-10, classificação vem do backend).

export const ROTULOS_CLASSIFICACAO = {
  RUIM: 'Ruim',
  RAZOAVEL: 'Razoável',
  BOM: 'Bom',
};

// classe CSS em minúsculo: nota-ruim, nota-razoavel, nota-bom
export function classeClassificacao(classificacao) {
  return classificacao ? `nota-${classificacao.toLowerCase()}` : '';
}

// Classificação calculada no front só para o preview do formulário (antes de enviar).
// Depois de enviada, sempre usar o campo "classificacao" que a API devolve.
export function classificarNota(nota) {
  if (nota <= 6) return 'RUIM';
  if (nota <= 8) return 'RAZOAVEL';
  return 'BOM';
}
