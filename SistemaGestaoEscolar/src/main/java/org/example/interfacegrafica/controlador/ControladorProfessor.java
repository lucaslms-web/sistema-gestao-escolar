package org.example.interfacegrafica.controlador;

import org.example.dominio.Pessoa;
import org.example.dominio.Professor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ControladorProfessor {

    private final GerenciadorRepositorio gerenciador;

    public ControladorProfessor(GerenciadorRepositorio gerenciador) {
        this.gerenciador = gerenciador;
    }

    public List<Professor> listarProfessores(String filtro) {
        String termo = (filtro == null) ? "" : filtro.trim().toLowerCase(Locale.ROOT);
        return gerenciador.getRepositorio().listarTodos().stream()
                .filter(p -> p instanceof Professor)
                .map(p -> (Professor) p)
                .filter(prof -> termo.isEmpty()
                        || String.valueOf(prof.getId()).contains(termo)
                        || prof.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || String.join(", ", prof.getDisciplinas()).toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparingInt(Professor::getId))
                .collect(Collectors.toList());
    }

    public boolean salvarOuAtualizar(
            String idTexto,
            String nome,
            String dataNascimentoTexto,
            String salarioTexto,
            String disciplinasTexto,
            boolean modoEdicao) {

        int id = parseInteiro(idTexto, "ID");
        LocalDate nascimento = parseData(dataNascimentoTexto);
        double salario = parseDouble(salarioTexto, "Salário");

        List<String> disciplinas = (disciplinasTexto == null || disciplinasTexto.isBlank())
                ? List.of()
                : Arrays.stream(disciplinasTexto.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Professor professor = new Professor(id, nome, nascimento, salario, disciplinas);

        if (!modoEdicao && gerenciador.getRepositorio().buscarPorId(id) != null) {
            throw new IllegalArgumentException("Já existe um registro cadastrado com o ID " + id + ".");
        }

        return gerenciador.getRepositorio().upsertAtomico(professor);
    }

    public boolean excluir(int id) {
        Pessoa existente = gerenciador.getRepositorio().buscarPorId(id);
        if (!(existente instanceof Professor)) {
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
            throw new IllegalArgumentException("Data de nascimento inválida. Use o formato AAAA-MM-DD (ex.: 1982-09-20).");
        }
    }
}