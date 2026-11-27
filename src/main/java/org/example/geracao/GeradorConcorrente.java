package org.example.geracao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeradorConcorrente {

    public void gerarTodosOsArquivosConcorrentes(int numThreads) {
        int[] tamanhos = {1_000, 100_000, 1_000_000, 5_000_000};
        String[] nomesFicheiros = {
                "dados_pequeno_conc.csv",
                "dados_medio_conc.csv",
                "dados_grande_conc.csv",
                "dados_gigante_conc.csv"
        };
        long seedBase = 42L;

        System.out.println("Iniciando geração concorrente (" + numThreads + " thread(s))...\n");
        for (int i = 0; i < tamanhos.length; i++) {
            gerarArquivoConcorrente(nomesFicheiros[i], tamanhos[i], numThreads, seedBase + i, true);
        }
        System.out.println("\nGeração concorrente concluída!");
    }

    public double gerarArquivoConcorrente(String nomeFicheiro, int qtdLinhas, int numThreads, long seed) {
        return gerarArquivoConcorrente(nomeFicheiro, qtdLinhas, numThreads, seed, true);
    }

    public double gerarArquivoConcorrente(String nomeFicheiro, int qtdLinhas, int numThreads, long seed, boolean logConsole) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("O número de threads deve ser pelo menos 1.");
        }

        Path destinoPath = Path.of(nomeFicheiro).toAbsolutePath();
        Path diretorioBase = (destinoPath.getParent() != null) ? destinoPath.getParent() : Path.of(".");

        long inicioTempo = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        SplittableRandom baseRandom = new SplittableRandom(seed);

        int linhasPorThread = qtdLinhas / numThreads;
        int linhasRestantes = qtdLinhas % numThreads;

        List<Callable<Path>> tarefas = new ArrayList<>(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int inicioId = t * linhasPorThread + 1;
            final int fimId = (t == numThreads - 1)
                    ? (inicioId + linhasPorThread - 1 + linhasRestantes)
                    : (inicioId + linhasPorThread - 1);

            final SplittableRandom taskRandom = baseRandom.split();
            final int indiceBloco = t;

            tarefas.add(() -> gerarBlocoTemporario(diretorioBase, indiceBloco, inicioId, fimId, taskRandom));
        }

        List<Path> arquivosTemporarios = new ArrayList<>(numThreads);

        try {
            List<Future<Path>> futuros = executor.invokeAll(tarefas);
            for (Future<Path> futuro : futuros) {
                arquivosTemporarios.add(futuro.get());
            }
            juntarArquivosTemporarios(arquivosTemporarios, destinoPath);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Geração concorrente interrompida.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro na execução de tarefa do gerador concorrente.", e.getCause());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao consolidar arquivo final " + nomeFicheiro, e);
        } finally {
            executor.shutdown();
            limparTemporarios(arquivosTemporarios);
        }

        double tempoMs = (System.nanoTime() - inicioTempo) / 1_000_000.0;

        if (logConsole) {
            double tamanhoMB = destinoPath.toFile().length() / (1024.0 * 1024.0);
            System.out.printf("Ficheiro: %-25s | Threads: %-2d | Linhas: %-9d | Tempo: %8.2f ms | Tamanho: %6.2f MB%n",
                    nomeFicheiro, numThreads, qtdLinhas, tempoMs, tamanhoMB);
        }
        return tempoMs;
    }

    private Path gerarBlocoTemporario(Path diretorio, int indiceBloco, int inicioId, int fimId, SplittableRandom random)
            throws IOException {
        Path tempFile = Files.createTempFile(diretorio, "bloco_gerador_" + indiceBloco + "_", ".tmp");
        StringBuilder sb = new StringBuilder(128);

        try (BufferedWriter bw = new BufferedWriter(
                Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8), GeradorSequencial.BUFFER_SIZE)) {
            for (int id = inicioId; id <= fimId; id++) {
                sb.setLength(0);
                GeradorSequencial.escreverLinhaCsv(sb, id, random);
                bw.write(sb.toString());
            }
        }
        return tempFile;
    }

    private void juntarArquivosTemporarios(List<Path> temporarios, Path destinoFinal) throws IOException {
        byte[] buffer = new byte[GeradorSequencial.BUFFER_SIZE];
        try (BufferedOutputStream out = new BufferedOutputStream(
                Files.newOutputStream(destinoFinal), GeradorSequencial.BUFFER_SIZE)) {
            for (Path temp : temporarios) {
                try (BufferedInputStream in = new BufferedInputStream(
                        Files.newInputStream(temp), GeradorSequencial.BUFFER_SIZE)) {
                    int bytesLidos;
                    while ((bytesLidos = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesLidos);
                    }
                }
            }
        }
    }

    private void limparTemporarios(List<Path> temporarios) {
        for (Path temp : temporarios) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
        }
    }
}