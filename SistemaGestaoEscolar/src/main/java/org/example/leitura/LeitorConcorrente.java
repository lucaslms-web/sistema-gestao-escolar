package org.example.leitura;

import org.example.dominio.Pessoa;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LeitorConcorrente {

    private static final int TAMANHO_LOTE_PADRAO = 10_000;
    private final LeitorSequencial parserCompartilhado = new LeitorSequencial();

    private static class ResultadoLote {
        final List<Pessoa> registros;
        final int validas;
        final int invalidas;

        ResultadoLote(List<Pessoa> registros, int validas, int invalidas) {
            this.registros = registros;
            this.validas = validas;
            this.invalidas = invalidas;
        }
    }

    public void lerTodosOsArquivosConcorrentes(int numThreads) {
        String[] arquivos = {
                "dados_pequeno.csv",
                "dados_medio.csv",
                "dados_grande.csv",
                "dados_gigante.csv"
        };

        System.out.println("Iniciando leitura concorrente (" + numThreads + " thread(s))...\n");
        for (String arquivo : arquivos) {
            lerArquivoConcorrente(arquivo, numThreads, TAMANHO_LOTE_PADRAO, true);
        }
        System.out.println("\nLeitura concorrente concluída!");
    }

    public ResultadoLeitura lerArquivoConcorrente(String caminhoArquivo, int numThreads) {
        return lerArquivoConcorrente(caminhoArquivo, numThreads, TAMANHO_LOTE_PADRAO, true);
    }

    public ResultadoLeitura lerArquivoConcorrente(String caminhoArquivo, int numThreads, int tamanhoLote) {
        return lerArquivoConcorrente(caminhoArquivo, numThreads, tamanhoLote, true);
    }

    public ResultadoLeitura lerArquivoConcorrente(
            String caminhoArquivo, int numThreads, int tamanhoLote, boolean logConsole) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("O número de threads deve ser pelo menos 1.");
        }
        if (tamanhoLote < 1) {
            throw new IllegalArgumentException("O tamanho do lote deve ser pelo menos 1.");
        }

        Path path = Path.of(caminhoArquivo);
        long inicioTempo = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int maxLotesEmVoo = numThreads * 4;
        List<Future<ResultadoLote>> futurosJanela = new ArrayList<>(maxLotesEmVoo);

        ArrayList<Pessoa> registrosConsolidados = new ArrayList<>(LeitorSequencial.estimarCapacidade(path));
        int[] totais = new int[2];
        int loteOffset = 0;

        try (BufferedReader br = new BufferedReader(
                Files.newBufferedReader(path, StandardCharsets.UTF_8), LeitorSequencial.BUFFER_SIZE)) {
            List<String> loteAtual = new ArrayList<>(tamanhoLote);
            String linha;

            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) {
                    totais[1]++;
                    continue;
                }
                loteAtual.add(linha);

                if (loteAtual.size() == tamanhoLote) {
                    final List<String> loteParaProcessar = loteAtual;
                    futurosJanela.add(executor.submit(() -> processarLote(loteParaProcessar)));
                    loteAtual = new ArrayList<>(tamanhoLote);

                    if (futurosJanela.size() >= maxLotesEmVoo) {
                        drenarJanela(futurosJanela, registrosConsolidados, totais, caminhoArquivo, loteOffset);
                        loteOffset += futurosJanela.size();
                        futurosJanela.clear();
                    }
                }
            }

            if (!loteAtual.isEmpty()) {
                final List<String> loteRestante = loteAtual;
                futurosJanela.add(executor.submit(() -> processarLote(loteRestante)));
            }

            if (!futurosJanela.isEmpty()) {
                drenarJanela(futurosJanela, registrosConsolidados, totais, caminhoArquivo, loteOffset);
                futurosJanela.clear();
            }

            registrosConsolidados.trimToSize();

            double tempoMs = (System.nanoTime() - inicioTempo) / 1_000_000.0;

            if (logConsole) {
                System.out.printf("Arquivo: %-22s | Threads: %-2d | Válidas: %-9d | Inválidas: %-5d | Tempo: %8.2f ms%n",
                        caminhoArquivo, numThreads, totais[0], totais[1], tempoMs);
            }
            return new ResultadoLeitura(registrosConsolidados, totais[0], totais[1], tempoMs);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Leitura concorrente interrompida: " + caminhoArquivo, e);
        } catch (IOException e) {
            throw new RuntimeException("Erro de I/O ao ler o arquivo: " + caminhoArquivo, e);
        } finally {
            executor.shutdownNow();
        }
    }

    private void drenarJanela(
            List<Future<ResultadoLote>> futuros,
            List<Pessoa> destino,
            int[] totais,
            String caminhoArquivo,
            int loteOffset) throws InterruptedException {

        for (int i = 0; i < futuros.size(); i++) {
            try {
                ResultadoLote resLote = futuros.get(i).get();
                destino.addAll(resLote.registros);
                totais[0] += resLote.validas;
                totais[1] += resLote.invalidas;
            } catch (ExecutionException e) {
                String msg = "Falha ao processar o lote #" + (loteOffset + i + 1) + " do arquivo " + caminhoArquivo;
                throw new RuntimeException(msg, e.getCause());
            }
        }
    }

    private ResultadoLote processarLote(List<String> linhas) {
        List<Pessoa> registrosLocais = new ArrayList<>(linhas.size());
        int validas = 0;
        int invalidas = 0;

        for (String linha : linhas) {
            try {
                registrosLocais.add(parserCompartilhado.parsearLinha(linha));
                validas++;
            } catch (IllegalArgumentException | DateTimeParseException e) {
                invalidas++;
            }
        }
        return new ResultadoLote(registrosLocais, validas, invalidas);
    }
}