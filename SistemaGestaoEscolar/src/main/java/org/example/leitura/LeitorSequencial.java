package org.example.leitura;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.dominio.TipoRegistro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class LeitorSequencial {

    private static final int TOTAL_COLUNAS_ESPERADO = 6;
    static final int BUFFER_SIZE = 64 * 1024;

    public ResultadoLeitura lerArquivo(String caminhoArquivo) {
        return lerArquivo(caminhoArquivo, true);
    }

    public ResultadoLeitura lerArquivo(String caminhoArquivo, boolean logConsole) {
        Path path = Path.of(caminhoArquivo);
        int capacidadeInicial = estimarCapacidade(path);
        List<Pessoa> registrosValidos = new ArrayList<>(capacidadeInicial);
        int linhasValidas = 0;
        int linhasInvalidas = 0;

        long inicioTempo = System.nanoTime();

        try (BufferedReader br = new BufferedReader(
                Files.newBufferedReader(path, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) {
                    linhasInvalidas++;
                    continue;
                }

                try {
                    registrosValidos.add(parsearLinha(linha));
                    linhasValidas++;
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    linhasInvalidas++;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao abrir ou ler o arquivo: " + caminhoArquivo, e);
        }

        double tempoMs = (System.nanoTime() - inicioTempo) / 1_000_000.0;

        if (logConsole) {
            System.out.printf("Leitura: %-22s | Válidas: %-8d | Inválidas: %-6d | Tempo: %8.2f ms%n",
                    caminhoArquivo, linhasValidas, linhasInvalidas, tempoMs);
        }
        return new ResultadoLeitura(registrosValidos, linhasValidas, linhasInvalidas, tempoMs);
    }

    static int estimarCapacidade(Path path) {
        try {
            long bytes = Files.size(path);
            return (int) Math.max(16, Math.min(Integer.MAX_VALUE, bytes / 42L));
        } catch (IOException e) {
            return 16;
        }
    }

    public Pessoa parsearLinha(String linha) {
        String[] colunas = linha.split(";", -1);

        if (colunas.length != TOTAL_COLUNAS_ESPERADO) {
            throw new IllegalArgumentException("Quantidade de colunas inválida: esperado "
                    + TOTAL_COLUNAS_ESPERADO + ", encontrado " + colunas.length);
        }

        int id = Integer.parseInt(colunas[0].trim());
        if (id <= 0) {
            throw new IllegalArgumentException("ID deve ser maior que zero.");
        }

        String tipoStr = colunas[1].trim();
        TipoRegistro tipo;
        if ("ALUNO".equals(tipoStr)) {
            tipo = TipoRegistro.ALUNO;
        } else if ("PROFESSOR".equals(tipoStr)) {
            tipo = TipoRegistro.PROFESSOR;
        } else {
            tipo = TipoRegistro.valueOf(tipoStr.toUpperCase());
        }

        String nome = colunas[2].trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }

        double valor = Double.parseDouble(colunas[3].trim());

        String listaBruta = colunas[4].trim();
        if (listaBruta.isEmpty()) {
            throw new IllegalArgumentException("Lista de atributos extras não pode ser vazia.");
        }

        LocalDate data = LocalDate.parse(colunas[5].trim());

        return switch (tipo) {
            case ALUNO -> parsearAluno(id, nome, data, valor, listaBruta);
            case PROFESSOR -> {
                String[] partes = listaBruta.split(",");
                List<String> materias = new ArrayList<>(partes.length);
                for (String m : partes) {
                    String limpa = m.trim();
                    if (!limpa.isEmpty()) {
                        materias.add(limpa);
                    }
                }
                yield new Professor(id, nome, data, valor, materias);
            }
            default -> throw new IllegalArgumentException("Tipo de registro não suportado neste arquivo: " + tipo);
        };
    }

    private Aluno parsearAluno(int id, String nome, LocalDate data, double nota, String listaBruta) {
        int idxVirgula = listaBruta.indexOf(',');
        if (idxVirgula < 0) {
            int frequencia = Integer.parseInt(listaBruta.trim());
            return new Aluno(id, nome, data, nota, frequencia, "Turma-A");
        }

        int frequencia = Integer.parseInt(listaBruta.substring(0, idxVirgula).trim());
        String sufixoTurma = listaBruta.substring(idxVirgula + 1).trim();
        String turma;
        if (sufixoTurma.isEmpty()) {
            turma = "Turma-A";
        } else if (sufixoTurma.startsWith("Turma-")) {
            turma = sufixoTurma;
        } else {
            turma = "Turma-" + sufixoTurma;
        }
        return new Aluno(id, nome, data, nota, frequencia, turma);
    }
}