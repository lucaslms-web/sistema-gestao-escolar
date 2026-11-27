import org.example.dominio.Aluno;
import org.example.repositorio.RepositorioComAtores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioComAtoresTest {

    @Test
    void deveExecutarCrudESalvarCarregarViaAtores(@TempDir Path tempDir) throws Exception {
        try (RepositorioComAtores repo = new RepositorioComAtores()) {
            Aluno a1 = new Aluno(1, "Lucas", LocalDate.of(2002, 5, 10), 8.5, 90, "Turma-1");
            Aluno a2 = new Aluno(2, "Matheus", LocalDate.of(2003, 2, 20), 6.0, 80, "Turma-2");

            assertTrue(repo.adicionar(a1));
            assertFalse(repo.adicionar(a1)); // Bloqueia ID duplicado
            assertTrue(repo.adicionar(a2));
            assertEquals(2, repo.tamanho());

            assertNotNull(repo.buscarPorId(1));
            assertNull(repo.buscarPorId(999)); // Não lança InvalidMessageException

            Path arquivoSaida = tempDir.resolve("atores_saida.csv");
            repo.salvarEmArquivo(arquivoSaida.toString());

            assertTrue(repo.remover(1));
            assertEquals(1, repo.tamanho());

            repo.carregarDeArquivoConcorrente(arquivoSaida.toString(), 2);
            assertEquals(2, repo.tamanho());
        }
    }

    @Test
    void deveGarantirConsistenciaExataSob8ClientesConcorrentes() throws Exception {
        try (RepositorioComAtores repo = new RepositorioComAtores()) {
            int inicial = 200;
            for (int i = 1; i <= inicial; i++) {
                repo.adicionar(new Aluno(i, "Inicial-" + i, LocalDate.of(2003, 1, 1), 7.5, 85, "Turma-1"));
            }

            int numThreads = 8;
            int inclusoesPorThread = 250;
            int remocoesPorThread = 20; // Cada thread remove 20 registros da base inicial (total = 160)

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

            // Verificação obrigatória: final = inicial + adicionados - removidos
            int esperado = inicial + totalInclusoes - totalRemocoes;
            assertEquals(esperado, repo.tamanho());
        }
    }

    @Test
    void deveGarantirAtomicidadeSemLocksEntreLeituraEEscritaDeBoletim() throws Exception {
        try (RepositorioComAtores repo = new RepositorioComAtores()) {
            repo.adicionar(new Aluno(1, "Renan", LocalDate.of(2002, 8, 15), 9.0, 60, "Turma-3"));

            ExecutorService executor = Executors.newFixedThreadPool(8);
            List<Callable<Void>> tarefas = new ArrayList<>();

            for (int t = 0; t < 8; t++) {
                final boolean estadoA = (t % 2 == 0);
                tarefas.add(() -> {
                    for (int i = 0; i < 200; i++) {
                        if (estadoA) {
                            repo.atualizarNotaEFrequenciaAtomico(1, 9.0, 60); // REPROVADO POR FALTA
                        } else {
                            repo.atualizarNotaEFrequenciaAtomico(1, 3.0, 95); // REPROVADO POR NOTA
                        }
                        String situacao = repo.consultarSituacaoAlunoAtomico(1);
                        assertTrue(situacao.equals("REPROVADO POR FALTA") || situacao.equals("REPROVADO POR NOTA"));
                    }
                    return null;
                });
            }

            for (Future<Void> f : executor.invokeAll(tarefas)) {
                f.get();
            }
            executor.shutdown();
            assertEquals(1, repo.tamanho());
        }
    }
}