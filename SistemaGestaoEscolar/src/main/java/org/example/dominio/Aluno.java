package org.example.dominio;

import java.time.LocalDate;
import java.util.Locale;

public class Aluno extends Pessoa {
    private double nota;
    private int frequencia;
    private String turma;

    public Aluno(int id, String nome, LocalDate dataNascimento, double nota, int frequencia, String turma) {
        super(id, nome, dataNascimento);

        if (nota < 0.0 || nota > 10.0) {
            throw new IllegalArgumentException("A nota deve estar entre 0.0 e 10.0.");
        }
        if (frequencia < 0 || frequencia > 100) {
            throw new IllegalArgumentException("A frequência deve estar entre 0 e 100.");
        }
        if (turma == null || turma.isBlank()) {
            throw new IllegalArgumentException("A turma não pode ser vazia.");
        }

        this.nota = nota;
        this.frequencia = frequencia;
        this.turma = turma;
    }

    public double getNota() {
        return nota;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public String getTurma() {
        return turma;
    }

    public void setNota(double nota) {
        if (nota < 0.0 || nota > 10.0) {
            throw new IllegalArgumentException("A nota deve estar entre 0.0 e 10.0.");
        }
        this.nota = nota;
    }

    public void setFrequencia(int frequencia) {
        if (frequencia < 0 || frequencia > 100) {
            throw new IllegalArgumentException("A frequência deve estar entre 0 e 100.");
        }
        this.frequencia = frequencia;
    }

    public void setTurma(String turma) {
        if (turma == null || turma.isBlank()) {
            throw new IllegalArgumentException("A turma não pode ser vazia.");
        }
        this.turma = turma;
    }

    @Override
    public String exibirDetalhes() {
        return String.format(Locale.US, "ALUNO [%s | Nota=%.2f | Freq=%d%% | Turma=%s]",
                formatarIdentificacaoBase(), nota, frequencia, turma);
    }

    @Override
    public String toCsvLinha() {
        String turmaNumerica = turma.replaceAll("\\D+", "");
        if (turmaNumerica.isEmpty()) {
            turmaNumerica = "1";
        }
        String listaExtra = frequencia + "," + turmaNumerica;
        return String.format(Locale.US, "%d;ALUNO;%s;%.2f;%s;%s",
                getId(), getNome(), nota, listaExtra, getDataNascimento());
    }
}