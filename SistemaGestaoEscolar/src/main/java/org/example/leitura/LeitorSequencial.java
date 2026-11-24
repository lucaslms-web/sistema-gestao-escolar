package org.example.leitura;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.dominio.TipoRegistro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeitorSequencial {

    private static final int TOTAL_COLUNAS_ESPERADO = 6;

    public ResultadoLeitura lerArquivo(String caminhoArquivo) {
        List<Pessoa> registrosValidos = new ArrayList<>();
        int linhasValidas = 0;
        int linhasInvalidas = 0;

        long inicioTempo = System.nanoTime();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) {
                    linhasInvalidas++;
                    continue;
                }

                try {
                    Pessoa pessoa = parsearLinha(linha);
                    registrosValidos.add(pessoa);
                    linhasValidas++;
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    linhasInvalidas++;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao abrir ou ler o arquivo " + caminhoArquivo + ": " + e.getMessage());
        }

        long fimTempo = System.nanoTime();
        double tempoMs = (fimTempo - inicioTempo) / 1_000_000.0;

        System.out.printf("Leitura: %-22s | Válidas: %-8d | Inválidas: %-6d | Tempo: %8.2f ms%n",
                caminhoArquivo, linhasValidas, linhasInvalidas, tempoMs);

        return new ResultadoLeitura(registrosValidos, linhasValidas, linhasInvalidas, tempoMs);
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

        TipoRegistro tipo = TipoRegistro.valueOf(colunas[1].trim().toUpperCase());

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
            case ALUNO -> {
                List<Double> valoresExtras = parsearListaDoubles(listaBruta);
                int frequencia = (int) Math.round(valoresExtras.get(0));
                String turma = (valoresExtras.size() > 1)
                        ? "Turma-" + valoresExtras.get(1).intValue()
                        : "Turma-A";
                yield new Aluno(id, nome, data, valor, frequencia, turma);
            }
            case PROFESSOR -> {
                List<String> materias = Arrays.asList(listaBruta.split(","));
                yield new Professor(id, nome, data, valor, materias);
            }
            default -> throw new IllegalArgumentException("Tipo de registro não suportado neste arquivo: " + tipo);
        };
    }

    private List<Double> parsearListaDoubles(String texto) {
        String[] itens = texto.split(",");
        List<Double> valores = new ArrayList<>(itens.length);
        for (String item : itens) {
            valores.add(Double.parseDouble(item.trim()));
        }
        return valores;
    }
}