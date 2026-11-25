package Classes;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays; // Se for converter a String do CSV em Lista

import java.awt.*;

class Professor extends Pessoa{
    private double salario;
    private List<String> disciplinas;

    public Professor(int id, String nome, LocalDate dataNascimento) {
        super(id, nome, dataNascimento);

        if (salario < 0) {
            throw new IllegalArgumentException("O salário não pode ser negativo.");
        }

        this.salario = salario;
        this.disciplinas = (disciplinas != null) ? new ArrayList<>(disciplinas) : new ArrayList<>();
    }

    public double getSalario() {
        return salario;
    }

    public List<String> getDisciplinas() {
        return disciplinas;
    }

    public void setSalario(double salario) {
        if (salario < 0) throw new IllegalArgumentException("Salário inválido");
        this.salario = salario;
    }

    public void setDisciplinas(List<String> disciplinas) {
        this.disciplinas = disciplinas;
    }
    public void adicionarDisciplina(String disciplina) {
        if (disciplina != null && !disciplina.isBlank()) {
            this.disciplinas.add(disciplina);
        }
    }
}
