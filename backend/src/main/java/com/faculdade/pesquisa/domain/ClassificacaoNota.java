package com.faculdade.pesquisa.domain;

/**
 * Faixa de satisfacao derivada da nota (escala de 1 a 10, estilo NPS):
 * 1-6 ruim, 7-8 razoavel, 9-10 bom.
 */
public enum ClassificacaoNota {
    RUIM,
    RAZOAVEL,
    BOM;

    public static ClassificacaoNota deNota(int nota) {
        if (nota <= 6) {
            return RUIM;
        }
        if (nota <= 8) {
            return RAZOAVEL;
        }
        return BOM;
    }
}
