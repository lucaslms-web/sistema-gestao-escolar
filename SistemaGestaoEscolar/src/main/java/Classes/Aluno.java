package Classes;

class Aluno {
    private String matricula;
    private double nota;
    private int frequencia;
    private String turma;

    public Aluno(double nota, String matricula, int frequencia, String turma) {
        this.nota = nota;
        this.matricula = matricula;
        this.frequencia = frequencia;
        this.turma = turma;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public void setTurma(String turma) {
        this.turma = turma;
    }

    public double getNota() {
        return nota;
    }

    public String getMatricula() {
        return matricula;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public String getTurma() {
        return turma;
    }

}
