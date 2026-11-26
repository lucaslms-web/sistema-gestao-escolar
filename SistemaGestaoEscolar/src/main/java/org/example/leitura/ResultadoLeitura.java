package org.example.leitura;

import org.example.dominio.Pessoa;
import java.util.List;

public class ResultadoLeitura {
    private final List<Pessoa> registros;
    private final int linhasValidas;
    private final int linhasInvalidas;
    private final double tempoMs;

    public ResultadoLeitura(List<Pessoa> registros, int linhasValidas, int linhasInvalidas, double tempoMs) {
        this.registros = registros;
        this.linhasValidas = linhasValidas;
        this.linhasInvalidas = linhasInvalidas;
        this.tempoMs = tempoMs;
    }

    public List<Pessoa> getRegistros() {
        return registros;
    }

    public int getLinhasValidas() {
        return linhasValidas;
    }

    public int getLinhasInvalidas() {
        return linhasInvalidas;
    }

    public double getTempoMs() {
        return tempoMs;
    }
}