package org.example.geracao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeradorConcorrente {

    private static final String[] NOMES = {"Ana", "Bruno", "Carlos", "Diana", "Eduardo", "Fernanda", "Gustavo", "Helena"};
    private static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Rodrigues"};
    private static final String[] MATERIAS = {"Matematica", "Fisica", "Historia", "Geografia", "Biologia", "Quimica"};

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
            gerarArquivoConcorrente(nomesFicheiros[i], tamanhos[i], numThreads, seedBase + i);
        }

        System.out.println("\nGeração concorrente concluída!");
    }

    public double gerarArquivoConcorrente(String nomeFicheiro, int qtdLinhas, int numThreads, long seed) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("O número de threads deve ser pelo menos 1.");
        }

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

            // Cada tarefa recebe seu próprio gerador derivado antes da execução paralela
            final SplittableRandom taskRandom = baseRandom.split();
            final int indiceBloco = t;

            tarefas.add(() -> gerarBlocoTemporario(indiceBloco, inicioId, fimId, taskRandom));
        }

        List<Path> arquivosTemporarios = new ArrayList<>(numThreads);

        try {
            List<Future<Path>> futuros = executor.invokeAll(tarefas);
            for (Future<Path> futuro : futuros) {
                arquivosTemporarios.add(futuro.get());
            }

            // Etapa final: consolida os blocos na ordem sequencial dos IDs
            juntarArquivosTemporarios(arquivosTemporarios, nomeFicheiro);

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

        long fimTempo = System.nanoTime();
        double tempoMs = (fimTempo - inicioTempo) / 1_000_000.0;

        File arquivoGerado = new File(nomeFicheiro);
        double tamanhoMB = arquivoGerado.length() / (1024.0 * 1024.0);

        System.out.printf("Ficheiro: %-25s | Threads: %-2d | Linhas: %-9d | Tempo: %8.2f ms | Tamanho: %6.2f MB%n",
                nomeFicheiro, numThreads, qtdLinhas, tempoMs, tamanhoMB);

        return tempoMs;
    }

    private Path gerarBlocoTemporario(int indiceBloco, int inicioId, int fimId, SplittableRandom random) throws IOException {
        Path tempFile = Files.createTempFile("bloco_gerador_" + indiceBloco + "_", ".tmp");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile.toFile()))) {
            for (int id = inicioId; id <= fimId; id++) {
                boolean isAluno = random.nextBoolean();
                String tipo = isAluno ? "ALUNO" : "PROFESSOR";

                String nome = NOMES[random.nextInt(NOMES.length)] + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
                int ano = 1970 + random.nextInt(41);
                int mes = 1 + random.nextInt(12);
                int dia = 1 + random.nextInt(28);
                String data = LocalDate.of(ano, mes, dia).toString();

                double valor;
                String listaExtra;

                if (isAluno) {
                    valor = random.nextDouble() * 10;
                    listaExtra = (random.nextInt(30) + 70) + "," + (random.nextInt(30) + 70);
                } else {
                    valor = 2000 + (random.nextDouble() * 8000);
                    listaExtra = MATERIAS[random.nextInt(MATERIAS.length)] + "," + MATERIAS[random.nextInt(MATERIAS.length)];
                }

                String linha = String.format(Locale.US, "%d;%s;%s;%.2f;%s;%s\n",
                        id, tipo, nome, valor, listaExtra, data);
                bw.write(linha);
            }
        }

        return tempFile;
    }

    private void juntarArquivosTemporarios(List<Path> temporarios, String destinoFinal) throws IOException {
        byte[] buffer = new byte[64 * 1024]; // Buffer de 64 KB para cópia rápida em disco
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destinoFinal))) {
            for (Path temp : temporarios) {
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(temp.toFile()))) {
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