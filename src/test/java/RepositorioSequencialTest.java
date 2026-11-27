import org.example.repositorio.BoletimService;
import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.RepositorioSequencial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioSequencialTest {

    private RepositorioSequencial repositorio;
    private BoletimService boletimService;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioSequencial();
        boletimService = new BoletimService();
    }

    @Test
    void deveAdicionarBuscarListarERemoverRegistros() {
        Aluno a1 = new Aluno(1, "Ana Silva", LocalDate.of(2003, 5, 10), 8.5, 90, "Turma-10");
        Professor p1 = new Professor(2, "Carlos Souza", LocalDate.of(1980, 3, 20), 5000.0, List.of("Matematica"));

        assertTrue(repositorio.adicionar(a1));
        assertTrue(repositorio.adicionar(p1));
        assertFalse(repositorio.adicionar(a1), "Não deve permitir adicionar ID duplicado");

        assertEquals(2, repositorio.tamanho());
        assertEquals("Ana Silva", repositorio.buscarPorId(1).getNome());
        assertEquals(2, repositorio.listarTodos().size());

        assertTrue(repositorio.remover(1));
        assertNull(repositorio.buscarPorId(1));
        assertEquals(1, repositorio.tamanho());
    }

    @Test
    void deveSalvarECarregarArquivoCsvMantendoIntegridade(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("backup_repo.csv");

        repositorio.adicionar(new Aluno(10, "Bruno Costa", LocalDate.of(2002, 8, 15), 7.8, 88, "Turma-20"));
        repositorio.adicionar(new Professor(20, "Diana Lima", LocalDate.of(1985, 1, 12), 6200.0, List.of("Fisica", "Quimica")));

        repositorio.salvarEmArquivo(arquivo.toString());

        RepositorioSequencial novoRepo = new RepositorioSequencial();
        ResultadoLeitura res = novoRepo.carregarDeArquivo(arquivo.toString());

        assertEquals(2, res.getLinhasValidas());
        assertEquals(0, res.getLinhasInvalidas());
        assertEquals(2, novoRepo.tamanho());
        assertEquals("Bruno Costa", novoRepo.buscarPorId(10).getNome());
        assertEquals("Diana Lima", novoRepo.buscarPorId(20).getNome());
    }

    @Test
    void deveCalcularSituacaoBoletimEMediaComCasosConhecidos() {
        Aluno aprovado = new Aluno(1, "Aprovado", LocalDate.of(2004, 1, 1), 8.0, 80, "Turma-1");
        Aluno recuperacao = new Aluno(2, "Recuperacao", LocalDate.of(2004, 1, 1), 6.0, 85, "Turma-1");
        Aluno reprovadoNota = new Aluno(3, "Reprovado Nota", LocalDate.of(2004, 1, 1), 4.0, 90, "Turma-1");
        Aluno reprovadoFalta = new Aluno(4, "Reprovado Falta", LocalDate.of(2004, 1, 1), 9.5, 60, "Turma-1");

        assertEquals("APROVADO", boletimService.verificarSituacao(aprovado));
        assertEquals("RECUPERACAO", boletimService.verificarSituacao(recuperacao));
        assertEquals("REPROVADO POR NOTA", boletimService.verificarSituacao(reprovadoNota));
        assertEquals("REPROVADO POR FALTA", boletimService.verificarSituacao(reprovadoFalta));

        List<Pessoa> lista = List.of(aprovado, recuperacao, reprovadoNota, reprovadoFalta);
        assertEquals(6.875, boletimService.calcularMediaNotas(lista), 0.001);
    }
}