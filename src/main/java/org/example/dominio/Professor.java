package org.example.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Professor extends Pessoa {
    private double salario;
    private List<String> disciplinas;

    public Professor(int id, String nome, LocalDate dataNascimento, double salario, List<String> disciplinas) {
        super(id, nome, dataNascimento);

        if (salario < 0) {
            throw new IllegalArgumentException("O salário não pode ser negativo.");
        }

        this.salario = salario;
        this.disciplinas = higienizarDisciplinas(disciplinas);
    }

    private static List<String> higienizarDisciplinas(List<String> origem) {
        if (origem == null || origem.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> limpas = new ArrayList<>(origem.size());
        for (String d : origem) {
            if (d != null && !d.isBlank()) {
                limpas.add(d.trim());
            }
        }
        return limpas;
    }

    public double getSalario() {
        return salario;
    }

    public List<String> getDisciplinas() {
        return Collections.unmodifiableList(disciplinas);
    }

    public void setSalario(double salario) {
        if (salario < 0) {
            throw new IllegalArgumentException("Salário inválido.");
        }
        this.salario = salario;
    }

    public void setDisciplinas(List<String> disciplinas) {
        this.disciplinas = higienizarDisciplinas(disciplinas);
    }

    public void adicionarDisciplina(String disciplina) {
        if (disciplina != null && !disciplina.isBlank()) {
            this.disciplinas.add(disciplina.trim());
        }
    }

    @Override
    public String exibirDetalhes() {
        return String.format(Locale.US, "PROFESSOR [%s | Salário=R$ %.2f | Disciplinas=%s]",
                formatarIdentificacaoBase(), salario, disciplinas);
    }

    @Override
    public String toCsvLinha() {
        String materias = disciplinas.isEmpty() ? "Geral" : String.join(",", disciplinas);
        return String.format(Locale.US, "%d;PROFESSOR;%s;%.2f;%s;%s",
                getId(), getNome(), salario, materias, getDataNascimento());
    }
}