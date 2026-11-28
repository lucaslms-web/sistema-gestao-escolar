package org.example.interfacegrafica.controlador;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ControladorAluno {

    private final GerenciadorRepositorio gerenciador;

    public ControladorAluno(GerenciadorRepositorio gerenciador) {
        this.gerenciador = gerenciador;
    }

    public List<Aluno> listarAlunos(String filtro) {
        String termo = (filtro == null) ? "" : filtro.trim().toLowerCase(Locale.ROOT);
        return gerenciador.getRepositorio().listarTodos().stream()
                .filter(p -> p instanceof Aluno)
                .map(p -> (Aluno) p)
                .filter(a -> termo.isEmpty()
                        || String.valueOf(a.getId()).contains(termo)
                        || a.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || a.getTurma().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparingInt(Aluno::getId))
                .collect(Collectors.toList());
    }

    public boolean salvarOuAtualizar(
            String idTexto,
            String nome,
            String dataNascimentoTexto,
            String notaTexto,
            String frequenciaTexto,
            String turma,
            boolean modoEdicao) {

        int id = parseInteiro(idTexto, "ID");
        LocalDate nascimento = parseData(dataNascimentoTexto);
        double nota = parseDouble(notaTexto, "Nota");
        int frequencia = parseInteiro(frequenciaTexto, "Frequência");

        Aluno aluno = new Aluno(id, nome, nascimento, nota, frequencia, turma);

        if (!modoEdicao && gerenciador.getRepositorio().buscarPorId(id) != null) {
            throw new IllegalArgumentException("Já existe um registro cadastrado com o ID " + id + ".");
        }

        return gerenciador.getRepositorio().upsertAtomico(aluno);
    }

    public boolean excluir(int id) {
        Pessoa existente = gerenciador.getRepositorio().buscarPorId(id);
        if (!(existente instanceof Aluno)) {
            return false;
        }
        return gerenciador.getRepositorio().remover(id);
    }

    private int parseInteiro(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo '" + campo + "' é obrigatório.");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser um número inteiro válido.");
        }
    }

    private double parseDouble(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo '" + campo + "' é obrigatório.");
        }
        try {
            return Double.parseDouble(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + campo + "' deve ser um número decimal válido.");
        }
    }

    private LocalDate parseData(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("A data de nascimento é obrigatória (formato AAAA-MM-DD).");
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de nascimento inválida. Use o formato AAAA-MM-DD (ex.: 2003-05-15).");
        }
    }
}