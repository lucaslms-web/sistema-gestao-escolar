package org.example.repositorio;

import org.example.dominio.Aluno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimuladorClientesConcorrentes {

    public static class ResultadoSimulacao {
        public final int numThreads;
        public final int totalOperacoes;
        public final double tempoMs;
        public final double opsPorSegundo;
        public final int tamanhoFinalRepositorio;

        public ResultadoSimulacao(int numThreads, int totalOperacoes, double tempoMs, int tamanhoFinalRepositorio) {
            this.numThreads = numThreads;
            this.totalOperacoes = totalOperacoes;
            this.tempoMs = tempoMs;
            this.opsPorSegundo = (tempoMs > 0) ? (totalOperacoes / (tempoMs / 1000.0)) : 0.0;
            this.tamanhoFinalRepositorio = tamanhoFinalRepositorio;
        }
    }

    public ResultadoSimulacao executarCargaConcorrente(
            RepositorioEscolar repositorio,
            int numThreads,
            int totalOperacoes,
            int faixaIdsExistentes,
            long seed) {

        if (numThreads < 1 || totalOperacoes < 1) {
            throw new IllegalArgumentException("Threads e total de operações devem ser >= 1.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        SplittableRandom baseRandom = new SplittableRandom(seed);

        int opsPorThread = totalOperacoes / numThreads;
        int opsRestantes = totalOperacoes % numThreads;

        List<Callable<Integer>> clientes = new ArrayList<>(numThreads);

        for (int t = 0; t < numThreads; t++) {
            final int qtdOps = (t == numThreads - 1) ? (opsPorThread + opsRestantes) : opsPorThread;
            final SplittableRandom rnd = baseRandom.split();
            final int idBaseThread = faixaIdsExistentes + (t * 100_000) + 1;

            clientes.add(() -> {
                int operacoesConcluidas = 0;
                for (int i = 0; i < qtdOps; i++) {
                    executarOperacaoMista(repositorio, rnd, faixaIdsExistentes, idBaseThread + (i % 5_000));
                    operacoesConcluidas++;
                }
                return operacoesConcluidas;
            });
        }

        long inicio = System.nanoTime();
        int totalExecutado = 0;

        try {
            List<Future<Integer>> resultados = executor.invokeAll(clientes);
            for (Future<Integer> f : resultados) {
                totalExecutado += f.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Simulação concorrente interrompida.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro durante simulação de clientes concorrentes.", e.getCause());
        } finally {
            executor.shutdownNow();
        }

        double tempoMs = (System.nanoTime() - inicio) / 1_000_000.0;
        return new ResultadoSimulacao(numThreads, totalExecutado, tempoMs, repositorio.tamanho());
    }

    public ResultadoSimulacao executarCargaConcorrenteAtores(
            RepositorioComAtores repositorio,
            int numThreads,
            int totalOperacoes,
            int faixaIdsExistentes,
            long seed) {
        return executarCargaConcorrente(repositorio, numThreads, totalOperacoes, faixaIdsExistentes, seed);
    }

    public ResultadoSimulacao executarCargaSequencial(
            RepositorioEscolar repositorio,
            int totalOperacoes,
            int faixaIdsExistentes,
            long seed) {

        SplittableRandom rnd = new SplittableRandom(seed);
        long inicio = System.nanoTime();

        for (int i = 0; i < totalOperacoes; i++) {
            executarOperacaoMista(repositorio, rnd, faixaIdsExistentes, faixaIdsExistentes + (i % 5_000) + 1);
        }

        double tempoMs = (System.nanoTime() - inicio) / 1_000_000.0;
        return new ResultadoSimulacao(1, totalOperacoes, tempoMs, repositorio.tamanho());
    }

    private static void executarOperacaoMista(
            RepositorioEscolar repositorio,
            SplittableRandom rnd,
            int faixaIdsExistentes,
            int idDinamico) {

        int sorteio = rnd.nextInt(100);
        int idAlvo = 1 + rnd.nextInt(Math.max(1, faixaIdsExistentes));

        if (sorteio < 40) {
            repositorio.buscarPorId(idAlvo);
        } else if (sorteio < 70) {
            repositorio.consultarSituacaoAlunoAtomico(idAlvo);
        } else if (sorteio < 85) {
            double novaNota = rnd.nextDouble() * 10.0;
            int novaFreq = 60 + rnd.nextInt(41);
            repositorio.atualizarNotaEFrequenciaAtomico(idAlvo, novaNota, novaFreq);
        } else if (sorteio < 95) {
            Aluno novoAluno = new Aluno(
                    idDinamico,
                    "Cliente-" + idDinamico,
                    LocalDate.of(2004, 4, 15),
                    7.5,
                    85,
                    "Turma-99"
            );
            repositorio.upsertAtomico(novoAluno);
        } else {
            repositorio.remover(idDinamico);
        }
    }
}