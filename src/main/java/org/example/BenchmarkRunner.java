package org.example;

import org.example.dominio.Pessoa;
import org.example.geracao.GeradorConcorrente;
import org.example.geracao.GeradorSequencial;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.RepositorioComAtores;
import org.example.repositorio.RepositorioComLocks;
import org.example.repositorio.RepositorioEscolar;
import org.example.repositorio.RepositorioSequencial;
import org.example.repositorio.SimuladorClientesConcorrentes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class BenchmarkRunner {

    private static final int WARMUP_RUNS = 2;
    private static final int MEDICOES_RUNS = 5;
    private static final long SEED_BASE = 42L;
    private static final int[] CONFIGS_THREADS = {1, 2, 4, 8};
    private static final int TAMANHO_LOTE_LEITURA = 10_000;

    private static final boolean RODAR_ETAPA_1_GERACAO = true;
    private static final boolean RODAR_ETAPA_2_LEITURA = true;
    private static final boolean RODAR_ETAPA_3_REPOSITORIO = true;
    private static final boolean INCLUIR_DATASET_GIGANTE_5M = true;

    private static final String ARQUIVO_SAIDA_MD = "resultados_benchmark.md";

    // Impede Dead Code Elimination (DCE) pelo compilador JIT da JVM
    private static volatile long blackhole;

    private static class Estatisticas {
        final String cenario;
        final String modo;
        final int numThreads;
        final long carga;
        final String unidadeCarga;
        final double mediaMs;
        final double medianaMs;
        final double desvioPadraoMs;
        final double coefVariacaoPct;
        final double throughputPorSeg;
        final double speedup;
        final double eficienciaPct;

        Estatisticas(
                String cenario,
                String modo,
                int numThreads,
                long carga,
                String unidadeCarga,
                double[] amostrasMs,
                double baselineSeqMs) {

            this.cenario = cenario;
            this.modo = modo;
            this.numThreads = numThreads;
            this.carga = carga;
            this.unidadeCarga = unidadeCarga;
            this.mediaMs = calcularMedia(amostrasMs);
            this.medianaMs = calcularMediana(amostrasMs);
            this.desvioPadraoMs = calcularDesvioPadrao(amostrasMs, this.mediaMs);
            this.coefVariacaoPct = (this.mediaMs > 0) ? (this.desvioPadraoMs / this.mediaMs) * 100.0 : 0.0;
            this.throughputPorSeg = (this.mediaMs > 0) ? (carga / (this.mediaMs / 1000.0)) : 0.0;
            this.speedup = (baselineSeqMs > 0 && this.mediaMs > 0) ? (baselineSeqMs / this.mediaMs) : 1.0;
            this.eficienciaPct = (numThreads > 0) ? (this.speedup / numThreads) * 100.0 : 100.0;
        }

        private static double calcularMedia(double[] valores) {
            double soma = 0.0;
            for (double v : valores) {
                soma += v;
            }
            return soma / valores.length;
        }

        private static double calcularMediana(double[] valores) {
            double[] copia = Arrays.copyOf(valores, valores.length);
            Arrays.sort(copia);
            int meio = copia.length / 2;
            if (copia.length % 2 == 0) {
                return (copia[meio - 1] + copia[meio]) / 2.0;
            }
            return copia[meio];
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
        Path tempDir = criarDiretorioTemporarioSeguro();

        StringBuilder relatorioMd = new StringBuilder();
        String cabecalhoAmbiente = gerarCabecalhoAmbiente();

        System.out.println(cabecalhoAmbiente);
        relatorioMd.append(cabecalhoAmbiente).append("\n");

        List<Estatisticas> resEtapa1 = List.of();
        List<Estatisticas> resEtapa2 = List.of();
        List<Estatisticas> resEtapa3 = List.of();

        try {
            if (RODAR_ETAPA_1_GERACAO) {
                resEtapa1 = executarBenchmarkGeracao(tempDir);
                relatorioMd.append(formatarTabelaMarkdown(
                        "## Etapa 1 — Geração de Arquivos CSV (Sequencial vs. Concorrente)", resEtapa1)).append("\n");
            }

            if (RODAR_ETAPA_2_LEITURA) {
                resEtapa2 = executarBenchmarkLeitura(tempDir);
                relatorioMd.append(formatarTabelaMarkdown(
                        "## Etapa 2 — Leitura e Parsing de CSV (Sequencial vs. Concorrente)", resEtapa2)).append("\n");
            }

            if (RODAR_ETAPA_3_REPOSITORIO) {
                resEtapa3 = executarBenchmarkRepositorio(tempDir);
                relatorioMd.append(formatarTabelaMarkdown(
                        "## Etapa 3 — Repositório Concorrente: Sequencial vs. Locks vs. Atores (70% Leitura / 20% Escrita / 10% Remoção)",
                        resEtapa3)).append("\n");
            }

            relatorioMd.append(gerarResumoExecutivo(resEtapa1, resEtapa2, resEtapa3));

            salvarRelatorioMarkdown(relatorioMd.toString());
            System.out.println("\nBenchmark finalizado com sucesso! Resultados salvos em: " + ARQUIVO_SAIDA_MD);

        } finally {
            limparDiretorioTemporario(tempDir);
        }
    }

    private static Path criarDiretorioTemporarioSeguro() {
        try {
            Path dir = Files.createTempDirectory("trabpoo_benchmark_");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> limparDiretorioTemporario(dir)));
            return dir;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar diretório temporário isolado para o benchmark.", e);
        }
    }

    private static String gerarCabecalhoAmbiente() {
        Runtime rt = Runtime.getRuntime();
        long maxHeapMb = rt.maxMemory() / (1024 * 1024);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String avisoHeap = (INCLUIR_DATASET_GIGANTE_5M && maxHeapMb < 2048)
                ? String.format("\n> **Aviso:** Heap máxima atual (%d MB) abaixo de 2048 MB; recomenda-se `-Xmx4g` para o dataset de 5M.\n", maxHeapMb)
                : "";

        return String.format(Locale.US,
                "# Resultados de Benchmark — Sistema de Gestão Escolar\n\n" +
                        "- **Data/Hora da Execução:** %s\n" +
                        "- **Sistema Operacional:** %s (%s) — versão %s\n" +
                        "- **Versão do Java / JVM:** %s (%s)\n" +
                        "- **Núcleos Lógicos Disponíveis:** %d\n" +
                        "- **Memória Heap Máxima (JVM):** %d MB\n" +
                        "- **Protocolo de Medição:** %d rodada(s) de warmup descartada(s) + %d medições cronometradas por cenário\n%s",
                timestamp,
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                System.getProperty("os.version"),
                System.getProperty("java.version"),
                System.getProperty("java.vm.name"),
                rt.availableProcessors(),
                maxHeapMb,
                WARMUP_RUNS,
                MEDICOES_RUNS,
                avisoHeap
        );
    }

    private static List<Estatisticas> executarBenchmarkGeracao(Path tempDir) {
        System.out.println("\n==================== ETAPA 1: BENCHMARK DE GERAÇÃO ====================\n");

        int[] tamanhos = INCLUIR_DATASET_GIGANTE_5M
                ? new int[]{1_000, 100_000, 1_000_000, 5_000_000}
                : new int[]{1_000, 100_000, 1_000_000};
        String[] rotulos = {"Pequeno (1k)", "Médio (100k)", "Grande (1M)", "Gigante (5M)"};

        GeradorSequencial geradorSeq = new GeradorSequencial();
        GeradorConcorrente geradorConc = new GeradorConcorrente();

        Path arquivoTemp = tempDir.resolve("bench_etapa1_saida.csv");
        String caminhoStr = arquivoTemp.toString();

        List<Estatisticas> resultados = new ArrayList<>();

        for (int i = 0; i < tamanhos.length; i++) {
            int qtdLinhas = tamanhos[i];
            String cenario = rotulos[i];
            long seed = SEED_BASE + i;

            for (int w = 0; w < WARMUP_RUNS; w++) {
                deletarSeExistir(arquivoTemp);
                geradorSeq.gerarArquivo(caminhoStr, qtdLinhas, seed, false);
                consumirTamanhoArquivo(arquivoTemp);
            }

            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                deletarSeExistir(arquivoTemp);
                amostrasSeq[r] = geradorSeq.gerarArquivo(caminhoStr, qtdLinhas, seed + r, false);
                consumirTamanhoArquivo(arquivoTemp);
            }

            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", 1, qtdLinhas, "linhas/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    deletarSeExistir(arquivoTemp);
                    geradorConc.gerarArquivoConcorrente(caminhoStr, qtdLinhas, threads, seed, false);
                    consumirTamanhoArquivo(arquivoTemp);
                }

                double[] amostrasConc = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    deletarSeExistir(arquivoTemp);
                    amostrasConc[r] = geradorConc.gerarArquivoConcorrente(caminhoStr, qtdLinhas, threads, seed + r, false);
                    consumirTamanhoArquivo(arquivoTemp);
                }

                Estatisticas estConc = new Estatisticas(
                        cenario, threads + " thread(s)", threads, qtdLinhas, "linhas/s", amostrasConc, estSeq.mediaMs);
                resultados.add(estConc);
                imprimirLinhaProgresso(estConc);
            }
        }

        deletarSeExistir(arquivoTemp);
        return resultados;
    }

    private static List<Estatisticas> executarBenchmarkLeitura(Path tempDir) {
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
            Path arquivoDataset = tempDir.resolve("bench_etapa2_" + qtdLinhas + ".csv");
            String caminhoDataset = arquivoDataset.toString();

            gerador.gerarArquivoConcorrente(caminhoDataset, qtdLinhas, 4, SEED_BASE + i, false);
            liberarMemoriaSeNecessario();

            for (int w = 0; w < WARMUP_RUNS; w++) {
                ResultadoLeitura warmupRes = leitorSeq.lerArquivo(caminhoDataset, false);
                blackhole ^= warmupRes.getLinhasValidas();
                warmupRes.getRegistros().clear();
                warmupRes = null;
                liberarMemoriaSeNecessario();
            }

            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                ResultadoLeitura res = leitorSeq.lerArquivo(caminhoDataset, false);
                amostrasSeq[r] = res.getTempoMs();
                int validas = res.getLinhasValidas();
                int invalidas = res.getLinhasInvalidas();
                blackhole ^= validas;
                res.getRegistros().clear();
                res = null;
                liberarMemoriaSeNecessario();

                if (validas != qtdLinhas || invalidas != 0) {
                    throw new IllegalStateException(
                            "Divergência na leitura sequencial: válidas=" + validas + ", inválidas=" + invalidas);
                }
            }

            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", 1, qtdLinhas, "linhas/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    ResultadoLeitura warmupRes = leitorConc.lerArquivoConcorrente(
                            caminhoDataset, threads, TAMANHO_LOTE_LEITURA, false);
                    blackhole ^= warmupRes.getLinhasValidas();
                    warmupRes.getRegistros().clear();
                    warmupRes = null;
                    liberarMemoriaSeNecessario();
                }

                double[] amostrasConc = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    ResultadoLeitura res = leitorConc.lerArquivoConcorrente(
                            caminhoDataset, threads, TAMANHO_LOTE_LEITURA, false);
                    amostrasConc[r] = res.getTempoMs();
                    int validas = res.getLinhasValidas();
                    int invalidas = res.getLinhasInvalidas();
                    blackhole ^= validas;
                    res.getRegistros().clear();
                    res = null;
                    liberarMemoriaSeNecessario();

                    if (validas != qtdLinhas || invalidas != 0) {
                        throw new IllegalStateException(
                                "Divergência na leitura concorrente: válidas=" + validas + ", inválidas=" + invalidas);
                    }
                }

                Estatisticas estConc = new Estatisticas(
                        cenario, threads + " thread(s)", threads, qtdLinhas, "linhas/s", amostrasConc, estSeq.mediaMs);
                resultados.add(estConc);
                imprimirLinhaProgresso(estConc);
            }

            deletarSeExistir(arquivoDataset);
        }

        return resultados;
    }

    private static List<Estatisticas> executarBenchmarkRepositorio(Path tempDir) {
        System.out.println("\n==================== ETAPA 3: BENCHMARK DE REPOSITÓRIO (SEQUENCIAL vs LOCKS vs ATORES) ====================\n");

        int registrosBase = 100_000;
        Path arquivoBase = tempDir.resolve("bench_etapa3_base.csv");
        String caminhoBase = arquivoBase.toString();

        GeradorConcorrente gerador = new GeradorConcorrente();
        gerador.gerarArquivoConcorrente(caminhoBase, registrosBase, 4, SEED_BASE, false);

        // Pré-carrega os registros base uma única vez para evitar I/O redundante entre as rodadas
        LeitorSequencial leitor = new LeitorSequencial();
        List<Pessoa> registrosBaseMemoria = leitor.lerArquivo(caminhoBase, false).getRegistros();

        int[] cargasOperacoes = {100_000, 500_000};
        String[] rotulosCarga = {"100k ops (Base 100k)", "500k ops (Base 100k)"};

        SimuladorClientesConcorrentes simulador = new SimuladorClientesConcorrentes();
        List<Estatisticas> resultados = new ArrayList<>();

        for (int c = 0; c < cargasOperacoes.length; c++) {
            int totalOps = cargasOperacoes[c];
            String cenario = rotulosCarga[c];

            // 1. Baseline Sequencial
            for (int w = 0; w < WARMUP_RUNS; w++) {
                RepositorioSequencial repoSeq = new RepositorioSequencial();
                popularRepositorio(repoSeq, registrosBaseMemoria);
                SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                        simulador.executarCargaSequencial(repoSeq, totalOps, registrosBase, SEED_BASE);
                blackhole ^= sim.tamanhoFinalRepositorio;
                repoSeq = null;
                liberarMemoriaSeNecessario();
            }

            double[] amostrasSeq = new double[MEDICOES_RUNS];
            for (int r = 0; r < MEDICOES_RUNS; r++) {
                RepositorioSequencial repoSeq = new RepositorioSequencial();
                popularRepositorio(repoSeq, registrosBaseMemoria);
                SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                        simulador.executarCargaSequencial(repoSeq, totalOps, registrosBase, SEED_BASE + r);
                amostrasSeq[r] = sim.tempoMs;
                blackhole ^= sim.tamanhoFinalRepositorio;
                repoSeq = null;
                liberarMemoriaSeNecessario();
            }

            Estatisticas estSeq = new Estatisticas(cenario, "Sequencial", 1, totalOps, "ops/s", amostrasSeq, 0.0);
            resultados.add(estSeq);
            imprimirLinhaProgresso(estSeq);

            // 2. Repositório com Locks (ReentrantReadWriteLock)
            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    RepositorioComLocks repoLocks = new RepositorioComLocks();
                    popularRepositorio(repoLocks, registrosBaseMemoria);
                    SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                            simulador.executarCargaConcorrente(repoLocks, threads, totalOps, registrosBase, SEED_BASE);
                    blackhole ^= sim.tamanhoFinalRepositorio;
                    repoLocks = null;
                    liberarMemoriaSeNecessario();
                }

                double[] amostrasLocks = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    RepositorioComLocks repoLocks = new RepositorioComLocks();
                    popularRepositorio(repoLocks, registrosBaseMemoria);
                    SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                            simulador.executarCargaConcorrente(repoLocks, threads, totalOps, registrosBase, SEED_BASE + r);
                    amostrasLocks[r] = sim.tempoMs;
                    blackhole ^= sim.tamanhoFinalRepositorio;
                    repoLocks = null;
                    liberarMemoriaSeNecessario();
                }

                Estatisticas estLocks = new Estatisticas(
                        cenario, "Locks (" + threads + "t)", threads, totalOps, "ops/s", amostrasLocks, estSeq.mediaMs);
                resultados.add(estLocks);
                imprimirLinhaProgresso(estLocks);
            }

            // 3. Repositório com Atores (Apache Pekko Typed)
            for (int threads : CONFIGS_THREADS) {
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    try (RepositorioComAtores repoAtores = new RepositorioComAtores()) {
                        repoAtores.carregarDeArquivoConcorrente(caminhoBase, 4);
                        SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                                simulador.executarCargaConcorrenteAtores(repoAtores, threads, totalOps, registrosBase, SEED_BASE);
                        blackhole ^= sim.tamanhoFinalRepositorio;
                    }
                    liberarMemoriaSeNecessario();
                }

                double[] amostrasAtores = new double[MEDICOES_RUNS];
                for (int r = 0; r < MEDICOES_RUNS; r++) {
                    try (RepositorioComAtores repoAtores = new RepositorioComAtores()) {
                        repoAtores.carregarDeArquivoConcorrente(caminhoBase, 4);
                        SimuladorClientesConcorrentes.ResultadoSimulacao sim =
                                simulador.executarCargaConcorrenteAtores(repoAtores, threads, totalOps, registrosBase, SEED_BASE + r);
                        amostrasAtores[r] = sim.tempoMs;
                        blackhole ^= sim.tamanhoFinalRepositorio;
                    }
                    liberarMemoriaSeNecessario();
                }

                Estatisticas estAtores = new Estatisticas(
                        cenario, "Atores (" + threads + "t)", threads, totalOps, "ops/s", amostrasAtores, estSeq.mediaMs);
                resultados.add(estAtores);
                imprimirLinhaProgresso(estAtores);
            }
        }

        registrosBaseMemoria.clear();
        deletarSeExistir(arquivoBase);
        return resultados;
    }

    private static void popularRepositorio(RepositorioEscolar repo, List<Pessoa> registros) {
        for (Pessoa p : registros) {
            repo.adicionar(p);
        }
    }

    private static void imprimirLinhaProgresso(Estatisticas e) {
        System.out.printf(Locale.US,
                "[%-20s | %-13s] Média: %8.2f ms | Mediana: %8.2f ms | DP: %6.2f ms (CV %4.1f%%) | Vazão: %10.0f %-8s | Speedup: %5.2fx (%5.1f%%)%n",
                e.cenario, e.modo, e.mediaMs, e.medianaMs, e.desvioPadraoMs, e.coefVariacaoPct,
                e.throughputPorSeg, e.unidadeCarga, e.speedup, e.eficienciaPct);
    }

    private static String formatarTabelaMarkdown(String tituloSecao, List<Estatisticas> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append(tituloSecao).append("\n\n");
        sb.append("| Cenário / Volume | Execução | Média (ms) | Mediana (ms) | Desvio Padrão (ms) | CV (%) | Vazão | Speedup | Eficiência |\n");
        sb.append("| :--- | :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n");

        for (Estatisticas e : lista) {
            sb.append(String.format(Locale.US,
                    "| %s | %s | %.2f | %.2f | %.2f | %.1f%% | %.0f %s | %.2fx | %.1f%% |\n",
                    e.cenario, e.modo, e.mediaMs, e.medianaMs, e.desvioPadraoMs, e.coefVariacaoPct,
                    e.throughputPorSeg, e.unidadeCarga, e.speedup, e.eficienciaPct));
        }
        return sb.toString();
    }

    private static String gerarResumoExecutivo(
            List<Estatisticas> etapa1,
            List<Estatisticas> etapa2,
            List<Estatisticas> etapa3) {

        StringBuilder sb = new StringBuilder("## Resumo Executivo dos Resultados\n\n");

        melhorPorSpeedup(etapa1).ifPresent(best -> sb.append(String.format(Locale.US,
                "- **Etapa 1 (Geração):** Maior speedup de **%.2fx** em `%s` com `%s` (vazão de **%.0f %s**, mediana de %.2f ms).\n",
                best.speedup, best.cenario, best.modo, best.throughputPorSeg, best.unidadeCarga, best.medianaMs)));

        melhorPorSpeedup(etapa2).ifPresent(best -> sb.append(String.format(Locale.US,
                "- **Etapa 2 (Leitura):** Maior speedup de **%.2fx** em `%s` com `%s` (vazão de **%.0f %s**, mediana de %.2f ms).\n",
                best.speedup, best.cenario, best.modo, best.throughputPorSeg, best.unidadeCarga, best.medianaMs)));

        melhorPorVazao(etapa3).ifPresent(best -> sb.append(String.format(Locale.US,
                "- **Etapa 3 (Repositório):** Maior vazão de **%.0f %s** em `%s` com `%s` (speedup de **%.2fx**, mediana de %.2f ms).\n",
                best.throughputPorSeg, best.unidadeCarga, best.cenario, best.modo, best.speedup, best.medianaMs)));

        return sb.toString();
    }

    private static java.util.Optional<Estatisticas> melhorPorSpeedup(List<Estatisticas> lista) {
        return lista.stream()
                .filter(e -> e.numThreads > 1)
                .max(Comparator.comparingDouble(e -> e.speedup));
    }

    private static java.util.Optional<Estatisticas> melhorPorVazao(List<Estatisticas> lista) {
        return lista.stream()
                .max(Comparator.comparingDouble(e -> e.throughputPorSeg));
    }

    private static void salvarRelatorioMarkdown(String conteudo) {
        try (BufferedWriter bw = Files.newBufferedWriter(Path.of(ARQUIVO_SAIDA_MD), StandardCharsets.UTF_8)) {
            bw.write(conteudo);
        } catch (IOException e) {
            System.err.println("Não foi possível gravar " + ARQUIVO_SAIDA_MD + ": " + e.getMessage());
        }
    }

    private static void liberarMemoriaSeNecessario() {
        Runtime rt = Runtime.getRuntime();
        long memoriaUsada = rt.totalMemory() - rt.freeMemory();
        double fracaoUso = (double) memoriaUsada / rt.maxMemory();

        if (fracaoUso > 0.35) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void consumirTamanhoArquivo(Path arquivo) {
        try {
            blackhole ^= Files.size(arquivo);
        } catch (IOException ignored) {
        }
    }

    private static void deletarSeExistir(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignored) {
        }
    }

    private static void limparDiretorioTemporario(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(BenchmarkRunner::deletarSeExistir);
        } catch (IOException ignored) {
        }
    }
}