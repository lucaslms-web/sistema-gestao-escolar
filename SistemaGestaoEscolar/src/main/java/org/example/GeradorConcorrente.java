package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GeradorConcorrente {

    private static final String[] NOMES = {"Ana", "Bruno", "Carlos", "Diana", "Eduardo", "Fernanda", "Gustavo", "Helena"};
    private static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Rodrigues"};
    private static final String[] MATERIAS = {"Matematica", "Fisica", "Historia", "Geografia", "Biologia", "Quimica"};

    public void gerarTodosOsArquivosConcorrentes() {
        int[] tamanhos = {1_000, 100_000, 1_000_000, 5_000_000};
        String[] nomesFicheiros = {
                "dados_pequeno_conc.csv",
                "dados_medio_conc.csv",
                "dados_grande_conc.csv",
                "dados_gigante_conc.csv"
        };

        // Identifica o número de núcleos de processamento da CPU
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("A iniciar a geração concorrente de ficheiros (" + numThreads + " threads)...\n");

        for (int i = 0; i < tamanhos.length; i++) {
            gerarArquivoConcorrente(nomesFicheiros[i], tamanhos[i], numThreads);
        }

        System.out.println("\nGeração concorrente concluída!");
    }

    private void gerarArquivoConcorrente(String nomeFicheiro, int qtdLinhas, int numThreads) {
        long inicioTempo = System.nanoTime();

        // Pool de threads com tamanho fixo baseado nos núcleos da máquina
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int linhasPorThread = qtdLinhas / numThreads;
        int linhasRestantes = qtdLinhas % numThreads;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeFicheiro))) {

            for (int t = 0; t < numThreads; t++) {
                final int inicioId = t * linhasPorThread + 1;
                final int fimId = (t == numThreads - 1) ? (inicioId + linhasPorThread - 1 + linhasRestantes) : (inicioId + linhasPorThread - 1);

                // Submete cada fatia de trabalho para uma thread do pool
                executor.submit(() -> {
                    // ThreadLocalRandom isolado por thread (sem gargalo de sincronização)
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    StringBuilder chunk = new StringBuilder();
                    int bufferLimit = 1000;

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

                        chunk.append(String.format(Locale.US, "%d;%s;%s;%.2f;%s;%s\n",
                                id, tipo, nome, valor, listaExtra, data));

                        // Descarrega o buffer em lote para evitar concorrência excessiva no I/O do disco
                        if (id % bufferLimit == 0) {
                            synchronized (bw) {
                                try {
                                    bw.write(chunk.toString());
                                } catch (IOException e) {
                                    System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
                                }
                            }
                            chunk.setLength(0);
                        }
                    }

                    // Escreve o restante do buffer acumulado
                    if (chunk.length() > 0) {
                        synchronized (bw) {
                            try {
                                bw.write(chunk.toString());
                            } catch (IOException e) {
                                System.err.println("Erro ao escrever resto no arquivo: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            // Encerra o executor e aguarda todas as threads terminarem a escrita
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro durante a geração concorrente: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long fimTempo = System.nanoTime();
        double tempoMs = (fimTempo - inicioTempo) / 1_000_000.0;

        File arquivoGerado = new File(nomeFicheiro);
        double tamanhoMB = arquivoGerado.length() / (1024.0 * 1024.0);

        System.out.printf("Ficheiro: %-25s | Linhas: %-9d | Tempo: %8.2f ms | Tamanho: %6.2f MB%n",
                nomeFicheiro, qtdLinhas, tempoMs, tamanhoMB);
    }
}