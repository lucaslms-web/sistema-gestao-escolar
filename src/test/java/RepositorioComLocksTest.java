import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.repositorio.RepositorioComLocks;
import org.example.repositorio.SimuladorClientesConcorrentes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioComLocksTest {

    @Test
    void deveInserirExatamenteTodosOsRegistrosSemPerdaSob8Threads() throws Exception {
        RepositorioComLocks repo = new RepositorioComLocks();
        int numThreads = 8;
        int insercoesPorThread = 1_000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int t = 0; t < numThreads; t++) {
            final int offset = t * insercoesPorThread;
            tarefas.add(() -> {
                int inseridos = 0;
                for (int i = 1; i <= insercoesPorThread; i++) {
                    int id = offset + i;
                    Aluno a = new Aluno(id, "Aluno-" + id, LocalDate.of(2003, 1, 1), 8.0, 90, "Turma-1");
                    if (repo.adicionar(a)) {
                        inseridos++;
                    }
                }
                return inseridos;
            });
        }

        List<Future<Integer>> resultados = executor.invokeAll(tarefas);
        executor.shutdown();

        int somaInseridos = 0;
        for (Future<Integer> f : resultados) {
            somaInseridos += f.get();
        }

        assertEquals(numThreads * insercoesPorThread, somaInseridos);
        assertEquals(numThreads * insercoesPorThread, repo.tamanho());
    }

    @Test
    void deveGarantirConsistenciaExataComInclusoesERemocoesConcorrentes() throws Exception {
        RepositorioComLocks repo = new RepositorioComLocks();
        int inicial = 200;
        for (int i = 1; i <= inicial; i++) {
            repo.adicionar(new Aluno(i, "Inicial-" + i, LocalDate.of(2003, 1, 1), 7.5, 85, "Turma-1"));
        }

        int numThreads = 8;
        int inclusoesPorThread = 250;
        int remocoesPorThread = 20; // Cada thread remove 20 IDs distintos da base inicial (total = 160)

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<int[]>> tarefas = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int threadIdx = t;
            tarefas.add(() -> {
                int inclusoes = 0;
                int remocoes = 0;

                for (int i = 1; i <= inclusoesPorThread; i++) {
                    int novoId = 10_000 + (threadIdx * 1_000) + i;
                    if (repo.adicionar(new Aluno(novoId, "Novo-" + novoId, LocalDate.of(2004, 1, 1), 8.0, 90, "Turma-2"))) {
                        inclusoes++;
                    }
                }

                for (int r = 1; r <= remocoesPorThread; r++) {
                    int idRemover = (threadIdx * remocoesPorThread) + r;
                    if (repo.remover(idRemover)) {
                        remocoes++;
                    }
                }
                return new int[]{inclusoes, remocoes};
            });
        }

        int totalInclusoes = 0;
        int totalRemocoes = 0;
        for (Future<int[]> f : executor.invokeAll(tarefas)) {
            int[] res = f.get();
            totalInclusoes += res[0];
            totalRemocoes += res[1];
        }
        executor.shutdown();

        int esperado = inicial + totalInclusoes - totalRemocoes;
        assertEquals(esperado, repo.tamanho());
    }

    @Test
    void deveGarantirAtomicidadeNasOperacoesCompostasConcorrentes() throws Exception {
        RepositorioComLocks repo = new RepositorioComLocks();
        repo.adicionar(new Aluno(1, "Lucas", LocalDate.of(2002, 6, 10), 9.0, 60, "Turma-10"));

        int numThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final boolean estadoA = (t % 2 == 0);
            tarefas.add(() -> {
                for (int i = 0; i < 500; i++) {
                    if (estadoA) {
                        assertTrue(repo.atualizarNotaEFrequenciaAtomico(1, 9.0, 60));
                    } else {
                        assertTrue(repo.atualizarNotaEFrequenciaAtomico(1, 3.0, 95));
                    }
                    String situacao = repo.consultarSituacaoAlunoAtomico(1);
                    assertTrue(situacao.equals("REPROVADO POR FALTA") || situacao.equals("REPROVADO POR NOTA"),
                            "Estado inconsistente observado: " + situacao);
                }
                return null;
            });
        }

        for (Future<Void> f : executor.invokeAll(tarefas)) {
            f.get();
        }
        executor.shutdown();

        Pessoa finalRegistro = repo.buscarPorId(1);
        assertNotNull(finalRegistro);
        assertEquals(1, repo.tamanho());
    }

    @Test
    void deveExecutarSimulacaoDeClientesConcorrentesSemErrosPara1_2_4_E_8Threads() {
        RepositorioComLocks repo = new RepositorioComLocks();
        for (int i = 1; i <= 500; i++) {
            repo.adicionar(new Aluno(i, "Base-" + i, LocalDate.of(2003, 3, 3), 7.5, 85, "Turma-5"));
        }

        SimuladorClientesConcorrentes simulador = new SimuladorClientesConcorrentes();
        for (int threads : new int[]{1, 2, 4, 8}) {
            SimuladorClientesConcorrentes.ResultadoSimulacao res =
                    simulador.executarCargaConcorrente(repo, threads, 20_000, 500, 42L);

            assertEquals(20_000, res.totalOperacoes);
            assertTrue(res.tamanhoFinalRepositorio >= 500);
        }
    }
}