package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Nota extends RegistroAcademico {
    private double valor;
    private String disciplina;
    private List<Double> historicoAvaliacoes;

    public Nota(int id, LocalDate dataRegistro, double valor, String disciplina, List<Double> historicoAvaliacoes) {
        // 1. Chamada obrigatória ao construtor da superclasse
        super(id, dataRegistro, TipoRegistro.NOTA);

        if (valor < 0.0 || valor > 10.0) {
            throw new IllegalArgumentException("A nota deve estar entre 0.0 e 10.0.");
        }
        if (disciplina == null || disciplina.isBlank()) {
            throw new IllegalArgumentException("A disciplina não pode ser vazia.");
        }

        this.valor = valor;
        this.disciplina = disciplina;
        this.historicoAvaliacoes = (historicoAvaliacoes != null) ? new ArrayList<>(historicoAvaliacoes) : new ArrayList<>();
    }

    public double getValor() {
        return valor;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public List<Double> getHistoricoAvaliacoes() {
        return historicoAvaliacoes;
    }

    // 2. Implementação obrigatória do método abstrato herdado de RegistroAcademico
    @Override
    public String gerarResumo() {
        return String.format("Nota: %.2f na disciplina %s | Avaliações realizadas: %d",
                valor, disciplina, historicoAvaliacoes.size());
    }
}