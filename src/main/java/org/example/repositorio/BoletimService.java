package org.example.repositorio;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import java.util.List;

public class BoletimService {

    public static final double MEDIA_APROVACAO = 7.0;
    public static final double MEDIA_RECUPERACAO = 5.0;
    public static final int FREQUENCIA_MINIMA = 75;

    public String verificarSituacao(Aluno aluno) {
        if (aluno == null) {
            throw new IllegalArgumentException("Aluno não pode ser nulo.");
        }
        return verificarSituacao(aluno.getNota(), aluno.getFrequencia());
    }

    public String verificarSituacao(double nota, int frequencia) {
        if (frequencia < FREQUENCIA_MINIMA) {
            return "REPROVADO POR FALTA";
        }
        if (nota >= MEDIA_APROVACAO) {
            return "APROVADO";
        }
        if (nota >= MEDIA_RECUPERACAO) {
            return "RECUPERACAO";
        }
        return "REPROVADO POR NOTA";
    }

    public double calcularMediaNotas(List<Pessoa> registros) {
        double soma = 0.0;
        int totalAlunos = 0;

        for (Pessoa p : registros) {
            if (p instanceof Aluno aluno) {
                soma += aluno.getNota();
                totalAlunos++;
            }
        }
        return (totalAlunos == 0) ? 0.0 : soma / totalAlunos;
    }
}