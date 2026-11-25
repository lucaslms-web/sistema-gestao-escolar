package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Frequencia extends RegistroAcademico {
    private double percentual;
    private String observacao;
    private List<String> datasPresencas;

    public Frequencia(int id, LocalDate dataRegistro, double percentual, String observacao, List<String> datasPresencas) {
        super(id, dataRegistro, TipoRegistro.FREQUENCIA);

        if (percentual < 0.0 || percentual > 100.0) {
            throw new IllegalArgumentException("O percentual de frequência deve estar entre 0.0 e 100.0.");
        }

        this.percentual = percentual;
        this.observacao = (observacao != null) ? observacao : "";
        this.datasPresencas = (datasPresencas != null) ? new ArrayList<>(datasPresencas) : new ArrayList<>();
    }

    public double getPercentual() {
        return percentual;
    }

    public String getObservacao() {
        return observacao;
    }

    public List<String> getDatasPresencas() {
        return datasPresencas;
    }

    @Override
    public String gerarResumo() {
        return String.format("Frequência: %.1f%% | Presenças registradas: %d | Obs: %s",
                percentual, datasPresencas.size(), observacao.isEmpty() ? "Nenhuma" : observacao);
    }
}