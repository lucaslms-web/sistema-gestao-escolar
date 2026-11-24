package org.example;

import org.example.geracao.GeradorConcorrente;
import org.example.geracao.GeradorSequencial;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.RepositorioComAtores;
import org.example.repositorio.RepositorioComLocks;
import org.example.repositorio.RepositorioSequencial;
import org.example.repositorio.SimuladorClientesConcorrentes;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenchmarkRunner {

    private static final int WARMUP_RUNS = 1;
    private static final int MEDICOES_RUNS = 3;
    private static final long SEED_BASE = 42L;
    private static final int[] CONFIGS_THREADS = {1, 2, 4, 8};

    private static final boolean RODAR_ETAPA_1_GERACAO = true;
    private static final boolean RODAR_ETAPA_2_LEITURA = true;
    private static final boolean RODAR_ETAPA_3_REPOSITORIO = true;
    private static final boolean INCLUIR_DATASET_GIGANTE_5M = true;

    private static final String ARQUIVO_SAIDA_MD = "resultados_benchmark.md";

    private static class Estatisticas {
        final String cenario;
        final String modo;
        final long carga;
        final String unidadeCarga;
        final double mediaMs;
        final double desvioPadraoMs;
        final double throughputPorSeg;
        final double speedup;

        Estatisticas(String cenario, String modo, long carga, String unidadeCarga, double[] amostrasMs, double baselineSeqMs) {
            this.cenario = cenario;
            this.modo = modo;
            this.carga = carga;
            this.unidadeCarga = unidadeCarga;
            this.mediaMs = calcularMedia(amostrasMs);
            this.desvioPadraoMs = calcularDesvioPadrao(amostrasMs, this.mediaMs);
            this.throughputPorSeg = (this.mediaMs > 0) ? (carga / (this.mediaMs / 1000.0)) : 0.0;
            this.speedup = (baselineSeqMs > 0 && this.mediaMs > 0) ? (baselineSeqMs / this.mediaMs) : 1.0;
        }

        private static double calcularMedia(double[] valores) {
            double soma = 0.0;
            for (double v : valores) {
                soma += v;
            }
            return soma / valores.length;
        }

        private static double calcularDesvioPadrao(double[] valores, double media) {
            if (valores.length <= 1) {
                return 0.0;
            }
            double somaQuadrados = 0.0;
            for (double v : valores) {
                double diff = v - media;
                somaQuadrados += diff * diff;
            }
            return Math.sqrt(somaQuadrados / (valores.length - 1));
        }
    }

    public static void main(String[] args) {
        StringBuilder relatorioMd = new StringBuilder();
        String cabecalhoAmbiente = gerarCabecalhoAmbiente();

        System.out.println(cabecalhoAmbiente);
        relatorioMd.append(cabecalhoAmbiente).append("\n");

        List<String> arquivosTemporarios = new ArrayList<>();

        try {
            if (RODAR_ETAPA_1_GERACAO) {
                relatorioMd.append(executarBenchmarkGeracao(arquivosTemporarios)).append("\n");
            }

            if (RODAR_ETAPA_2_LEITURA) {
                relatorioMd.append(executarBenchmarkLeitura(arquivosTemporarios)).append("\n");
            }

            if (RODAR_ETAPA_3_REPOSITORIO) {
                relatorioMd.append(executarBenchmarkRepositorio(arquivosTemporarios)).append("\n");
            }

            salvarRelatorioMarkdown(relatorioMd.toString());
            System.out.println("\nBenchmark finalizado com sucesso! Tabelas consolidadas salvas em: " + ARQUIVO_SAIDA_MD);

        } finally {
            limparArquivos(arquivosTemporarios);
        }
    }

    private static String gerarCabecalhoAmbiente() {
        Runtime rt = Runtime.getRuntime();
        long maxHeapMb = rt.maxMemory() / (1024 * 1024);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return String.format(Locale.US,
                "# Resultados de Benchmark — Sistema de Gestão Escolar\n\n" +
                        "- **Data/Hora da Execução:** %s\n" +
                        "- **Sistema Operacional:** %s (%s) — versão %s\n" +
                        "- **Versão do Java / JVM:** %s (%s)\n" +
                        "- **Núcleos Lógicos Disponíveis:** %d\n" +
                        "- **Memória Heap Máxima (JVM):** %d MB\n" +
                        "- **Protocolo de Medição:** %d rodada(s) de warmup descartada(s) + %d medições cronometradas por cenário\n",
                timestamp,
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                System.getProperty("java.version"),
                System.getProperty("java.vm.name"),
                rt.availableProcessors(),
                maxHeapMb,
                WARMUP_RUNS,
                MEDICOES_RUNS
        );
    }

    private static String executarBenchmarkGeracao(List<String> arquivosTemporarios) {
        System.out.println("\n==================== ETAPA 1: BENCHMARK DE GERAÇÃO ====================\n");

        int[] tamanhos = INCLUIR_DATASET_GIGANTE_5M
                ? new int[]{1_000, 100_000, 1_000_000, 5_000_000}
                : new int[]{1_000, 100_000, 1_000_000};
        String[] rotulos = {"Pequeno (1k)", "Médio (100k)", "Grande (1M)", "Gigante (5M)"};

        GeradorSequencial geradorSeq = new GeradorSequencial();
        GeradorConcorrente geradorConc = new GeradorConcorrente();

        String tempArquivo = "bench_etapa1_temp.csv";
        arquivosTemporarios.add(tempArquivo);

        List<Estatisticas> resultados = new ArrayList<>();

        for (int i = 0; i < tamanhos.length; i++) {
            int qtdLinhas = tamanhos[i];
            String cenario = rotulos[i];
            long seed = SEED_BASE + i;

            for (int w = 0; w < WARMUP_RUNS; w++) {
                geradorSeq.gerarArquivo(tempArquivo, qtdLinhas, seed);
            }
            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                long t0 = System.nanoTime();
                geradorSeq.gerarArquivo(tempArquivo, qtdLinhas, seed);
                amostrasSeq[r] = (System.nanoTime() - t0) / 1_000_000.0;
            }
            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", qtdLinhas, "linhas/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    geradorConc.gerarArquivoConcorrente(tempArquivo, qtdLinhas, threads, seed);
                }
                double[] amostrasConc = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    long t0 = System.nanoTime();
                    geradorConc.gerarArquivoConcorrente(tempArquivo, qtdLinhas, threads, seed);
                    amostrasConc[r] = (System.nanoTime() - t0) / 1_000_000.0;
                }
                Estatisticas estConc = new Estatisticas(cenario, threads + " thread(s)", qtdLinhas, "linhas/s", amostrasConc, estSeq.mediaMs);
                resultados.add(estConc);
                imprimirLinhaProgresso(estConc);
            }
        }

        limparArquivoUnico(tempArquivo);
        return formatarTabelaMarkdown("## Etapa 1 — Geração de Arquivos CSV (Sequencial vs Concorrente)", resultados);
    }

    private static String executarBenchmarkLeitura(List<String> arquivosTemporarios) {
        System.out.println("\n==================== ETAPA 2: BENCHMARK DE LEITURA ====================\n");

        int[] tamanhos = INCLUIR_DATASET_GIGANTE_5M
                ? new int[]{1_000, 100_000, 1_000_000, 5_000_000}
                : new int[]{1_000, 100_000, 1_000_000};
        String[] rotulos = {"Pequeno (1k)", "Médio (100k)", "Grande (1M)", "Gigante (5M)"};

        GeradorConcorrente gerador = new GeradorConcorrente();
        LeitorSequencial leitorSeq = new LeitorSequencial();
        LeitorConcorrente leitorConc = new LeitorConcorrente();

        List<Estatisticas> resultados = new ArrayList<>();

        for (int i = 0; i < tamanhos.length; i++) {
            int qtdLinhas = tamanhos[i];
            String cenario = rotulos[i];
            String arquivoDataset = "bench_etapa2_" + qtdLinhas + ".csv";
            arquivosTemporarios.add(arquivoDataset);

            gerador.gerarArquivoConcorrente(arquivoDataset, qtdLinhas, 4, SEED_BASE + i);
            liberarMemoria();

            for (int w = 0; w < WARMUP_RUNS; w++) {
                ResultadoLeitura warmupRes = leitorSeq.lerArquivo(arquivoDataset);
                warmupRes.getRegistros().clear();
                warmupRes = null;
                liberarMemoria();
            }
            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                long t0 = System.nanoTime();
                ResultadoLeitura res = leitorSeq.lerArquivo(arquivoDataset);
                amostrasSeq[r] = (System.nanoTime() - t0) / 1_000_000.0;

                int validas = res.getLinhasValidas();
                res.getRegistros().clear();
                res = null;
                liberarMemoria();

                if (validas != qtdLinhas) {
                    throw new IllegalStateException("Divergência na leitura sequencial: " + validas);
                }
            }
            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", qtdLinhas, "linhas/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    ResultadoLeitura warmupRes = leitorConc.lerArquivoConcorrente(arquivoDataset, threads);
                    warmupRes.getRegistros().clear();
                    warmupRes = null;
                    liberarMemoria();
                }
                double[] amostrasConc = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    long t0 = System.nanoTime();
                    ResultadoLeitura res = leitorConc.lerArquivoConcorrente(arquivoDataset, threads);
                    amostrasConc[r] = (System.nanoTime() - t0) / 1_000_000.0;

                    int validas = res.getLinhasValidas();
                    res.getRegistros().clear();
                    res = null;
                    liberarMemoria();

                    if (validas != qtdLinhas) {
                        throw new IllegalStateException("Divergência na leitura concorrente: " + validas);
                    }
                }
                Estatisticas estConc = new Estatisticas(cenario, threads + " thread(s)", qtdLinhas, "linhas/s", amostrasConc, estSeq.mediaMs);
                resultados.add(estConc);
                imprimirLinhaProgresso(estConc);
            }

            limparArquivoUnico(arquivoDataset);
        }

        return formatarTabelaMarkdown("## Etapa 2 — Leitura e Parsing de CSV (Sequencial vs Concorrente)", resultados);
    }

    private static String executarBenchmarkRepositorio(List<String> arquivosTemporarios) {
        System.out.println("\n==================== ETAPA 3: BENCHMARK DE REPOSITÓRIO (SEQUENCIAL vs LOCKS vs ATORES) ====================\n");

        int registrosBase = 100_000;
        String arquivoBase = "bench_etapa3_base.csv";
        arquivosTemporarios.add(arquivoBase);

        GeradorConcorrente gerador = new GeradorConcorrente();
        gerador.gerarArquivoConcorrente(arquivoBase, registrosBase, 4, SEED_BASE);

        int[] cargasOperacoes = {100_000, 500_000};
        String[] rotulosCarga = {"100k ops (Base 100k)", "500k ops (Base 100k)"};

        SimuladorClientesConcorrentes simulador = new SimuladorClientesConcorrentes();
        List<Estatisticas> resultados = new ArrayList<>();

        for (int c = 0; c < cargasOperacoes.length; c++) {
            int totalOps = cargasOperacoes[c];
            String cenario = rotulosCarga[c];

            // 1. Sequencial
            for (int w = 0; w < WARMUP_RUNS; w++) {
                RepositorioSequencial repoSeq = new RepositorioSequencial();
                repoSeq.carregarDeArquivo(arquivoBase);
                simulador.executarCargaSequencial(repoSeq, totalOps, registrosBase, SEED_BASE);
                repoSeq = null;
                liberarMemoria();
            }
            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                RepositorioSequencial repoSeq = new RepositorioSequencial();
                repoSeq.carregarDeArquivo(arquivoBase);
                SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                        simulador.executarCargaSequencial(repoSeq, totalOps, registrosBase, SEED_BASE + r);
                amostrasSeq[r] = sim.tempoMs;
                repoSeq = null;
                liberarMemoria();
            }
            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", totalOps, "ops/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            // 2. Concorrente com Locks (ReentrantReadWriteLock)
            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    RepositorioComLocks repoLocks = new RepositorioComLocks();
                    repoLocks.carregarDeArquivoConcorrente(arquivoBase, 4);
                    simulador.executarCargaConcorrente(repoLocks, threads, totalOps, registrosBase, SEED_BASE);
                    repoLocks = null;
                    liberarMemoria();
                }
                double[] amostrasLocks = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    RepositorioComLocks repoLocks = new RepositorioComLocks();
                    repoLocks.carregarDeArquivoConcorrente(arquivoBase, 4);
                    SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                            simulador.executarCargaConcorrente(repoLocks, threads, totalOps, registrosBase, SEED_BASE + r);
                    amostrasLocks[r] = sim.tempoMs;
                    repoLocks = null;
                    liberarMemoria();
                }
                Estatisticas estLocks = new Estatisticas(cenario, "Locks (" + threads + "t)", totalOps, "ops/s", amostrasLocks, estSeq.mediaMs);
                resultados.add(estLocks);
                imprimirLinhaProgresso(estLocks);
            }

            // 3. Concorrente com Atores (Apache Pekko Typed - Bônus)
            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    try (RepositorioComAtores repoAtores = new RepositorioComAtores()) {
                        repoAtores.carregarDeArquivoConcorrente(arquivoBase, 4);
                        simulador.executarCargaConcorrenteAtores(repoAtores, threads, totalOps, registrosBase, SEED_BASE);
                    }
                    liberarMemoria();
                }
                double[] amostrasAtores = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    try (RepositorioComAtores repoAtores = new RepositorioComAtores()) {
                        repoAtores.carregarDeArquivoConcorrente(arquivoBase, 4);
                        SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                                simulador.executarCargaConcorrenteAtores(repoAtores, threads, totalOps, registrosBase, SEED_BASE + r);
                        amostrasAtores[r] = sim.tempoMs;
                    }
                    liberarMemoria();
                }
                Estatisticas estAtores = new Estatisticas(cenario, "Atores (" + threads + "t)", totalOps, "ops/s", amostrasAtores, estSeq.mediaMs);
                resultados.add(estAtores);
                imprimirLinhaProgresso(estAtores);
            }
        }

        limparArquivoUnico(arquivoBase);
        return formatarTabelaMarkdown("## Etapa 3 — Repositório Concorrente: Sequencial vs. Locks vs. Atores (70% Leitura / 20% Escrita / 10% Remoção)", resultados);
    }

    private static void imprimirLinhaProgresso(Estatisticas e) {
        System.out.printf(Locale.US,
                "[%-20s | %-13s] Média: %9.2f ms | Desvio: %7.2f ms | Vazão: %10.0f %-8s | Speedup: %5.2fx%n",
                e.cenario, e.modo, e.mediaMs, e.desvioPadraoMs, e.throughputPorSeg, e.unidadeCarga, e.speedup);
    }

    private static String formatarTabelaMarkdown(String tituloSecao, List<Estatisticas> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append(tituloSecao).append("\n\n");
        sb.append("| Cenário / Volume | Execução | Média (ms) | Desvio Padrão (ms) | Vazão | Speedup |\n");
        sb.append("| :--- | :--- | ---: | ---: | ---: | ---: |\n");

        for (Estatisticas e : lista) {
            sb.append(String.format(Locale.US,
                    "| %s | %s | %.2f | %.2f | %.0f %s | %.2fx |\n",
                    e.cenario, e.modo, e.mediaMs, e.desvioPadraoMs, e.throughputPorSeg, e.unidadeCarga, e.speedup));
        }
        return sb.toString();
    }

    private static void salvarRelatorioMarkdown(String conteudo) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ARQUIVO_SAIDA_MD), StandardCharsets.UTF_8))) {
            bw.write(conteudo);
        } catch (IOException e) {
            System.err.println("Não foi possível gravar " + ARQUIVO_SAIDA_MD + ": " + e.getMessage());
        }
    }

    private static void liberarMemoria() {
        System.gc();
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void limparArquivoUnico(String caminho) {
        try {
            Files.deleteIfExists(Path.of(caminho));
        } catch (IOException ignored) {
        }
    }

    private static void limparArquivos(List<String> caminhos) {
        for (String caminho : caminhos) {
            limparArquivoUnico(caminho);
        }
    }
}