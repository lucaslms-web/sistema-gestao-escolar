package org.example.geracao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SplittableRandom;

public class GeradorSequencial {

    static final String[] NOMES = {"Ana", "Bruno", "Carlos", "Diana", "Eduardo", "Fernanda", "Gustavo", "Helena"};
    static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Rodrigues"};
    static final String[] MATERIAS = {"Matematica", "Fisica", "Historia", "Geografia", "Biologia", "Quimica"};
    static final int BUFFER_SIZE = 64 * 1024;

    public void gerarTodosOsArquivos() {
        int[] tamanhos = {1_000, 100_000, 1_000_000, 5_000_000};
        String[] nomesFicheiros = {"dados_pequeno.csv", "dados_medio.csv", "dados_grande.csv", "dados_gigante.csv"};
        long seedBase = 42L;

        System.out.println("A iniciar a geração sequencial de ficheiros...\n");
        for (int i = 0; i < tamanhos.length; i++) {
            gerarArquivo(nomesFicheiros[i], tamanhos[i], seedBase + i, true);
        }
        System.out.println("\nGeração concluída!");
    }

    public void gerarArquivo(String nomeFicheiro, int qtdLinhas, long seed) {
        gerarArquivo(nomeFicheiro, qtdLinhas, seed, true);
    }

    public double gerarArquivo(String nomeFicheiro, int qtdLinhas, long seed, boolean logConsole) {
        SplittableRandom random = new SplittableRandom(seed);
        StringBuilder sb = new StringBuilder(128);
        Path destino = Path.of(nomeFicheiro);

        long inicioTempo = System.nanoTime();

        try (BufferedWriter bw = new BufferedWriter(
                Files.newBufferedWriter(destino, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            for (int id = 1; id <= qtdLinhas; id++) {
                sb.setLength(0);
                escreverLinhaCsv(sb, id, random);
                bw.write(sb.toString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro crítico ao escrever o ficheiro " + nomeFicheiro, e);
        }

        double tempoMs = (System.nanoTime() - inicioTempo) / 1_000_000.0;

        if (logConsole) {
            double tamanhoMB = destino.toFile().length() / (1024.0 * 1024.0);
            System.out.printf("Ficheiro: %-20s | Linhas: %-9d | Tempo: %8.2f ms | Tamanho: %6.2f MB%n",
                    nomeFicheiro, qtdLinhas, tempoMs, tamanhoMB);
        }
        return tempoMs;
    }

    static void escreverLinhaCsv(StringBuilder sb, int id, SplittableRandom random) {
        boolean isAluno = random.nextBoolean();
        String nome = NOMES[random.nextInt(NOMES.length)] + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
        int ano = 1970 + random.nextInt(41);
        int mes = 1 + random.nextInt(12);
        int dia = 1 + random.nextInt(28);

        sb.append(id).append(';')
                .append(isAluno ? "ALUNO" : "PROFESSOR").append(';')
                .append(nome).append(';');

        if (isAluno) {
            int notaCentavos = (int) Math.round(random.nextDouble() * 1000.0);
            appendDecimal2Casas(sb, notaCentavos);
            sb.append(';')
                    .append(random.nextInt(30) + 70).append(',')
                    .append(random.nextInt(30) + 70);
        } else {
            int salarioCentavos = 200_000 + (int) Math.round(random.nextDouble() * 800_000.0);
            appendDecimal2Casas(sb, salarioCentavos);
            sb.append(';')
                    .append(MATERIAS[random.nextInt(MATERIAS.length)]).append(',')
                    .append(MATERIAS[random.nextInt(MATERIAS.length)]);
        }

        sb.append(';').append(ano).append('-');
        if (mes < 10) sb.append('0');
        sb.append(mes).append('-');
        if (dia < 10) sb.append('0');
        sb.append(dia).append('\n');
    }

    private static void appendDecimal2Casas(StringBuilder sb, int centavos) {
        int inteiro = centavos / 100;
        int frac = centavos % 100;
        sb.append(inteiro).append('.');
        if (frac < 10) sb.append('0');
        sb.append(frac);
    }
}