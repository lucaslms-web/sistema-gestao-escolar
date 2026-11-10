package Classes;

import java.time.LocalDate;

class Aluno extends Pessoa{
    private double nota;
    private int frequencia;
    private String turma;

    public Aluno(int id, String nome, LocalDate dataNascimento,  double nota, int frequencia, String turma) {
        super(id, nome, dataNascimento);

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
        this.nota = nota;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public void setTurma(String turma) {
        this.turma = turma;
    }
}
