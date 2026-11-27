import org.example.dominio.Aluno;
import org.example.dominio.Frequencia;
import org.example.dominio.Nota;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.dominio.RegistroAcademico;
import org.example.dominio.TipoRegistro;
import org.example.leitura.LeitorSequencial;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DominioValidacaoTest {

    @Test
    void deveValidarInvariantesBaseDePessoa() {
        LocalDate nascimento = LocalDate.of(2002, 5, 10);

        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(0, "Ana", nascimento, 8.0, 90, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(-5, "Ana", nascimento, 8.0, 90, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, null, nascimento, 8.0, 90, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "   ", nascimento, 8.0, 90, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Ana", null, 8.0, 90, "Turma-1"));

        Aluno aluno = new Aluno(1, "  Ana Silva  ", nascimento, 8.0, 90, "  Turma-1  ");
        assertEquals("Ana Silva", aluno.getNome());
        assertEquals("Turma-1", aluno.getTurma());

        assertThrows(IllegalArgumentException.class, () -> aluno.setNome(" "));
        assertThrows(IllegalArgumentException.class, () -> aluno.setDataNascimento(null));
    }

    @Test
    void deveValidarLimitesEPreservarTurmaNoRoundTripCsvDoAluno() {
        LocalDate nascimento = LocalDate.of(2003, 8, 20);

        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Lucas", nascimento, -0.1, 80, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Lucas", nascimento, 10.1, 80, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Lucas", nascimento, 7.0, -1, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Lucas", nascimento, 7.0, 101, "Turma-1"));
        assertThrows(IllegalArgumentException.class,
                () -> new Aluno(1, "Lucas", nascimento, 7.0, 80, "  "));

        Aluno alunoNumerico = new Aluno(10, "Bruno Costa", nascimento, 8.75, 92, "Turma-12");
        Aluno alunoTextual = new Aluno(11, "Clara Dias", nascimento, 9.25, 98, "Turma-A");

        LeitorSequencial parser = new LeitorSequencial();
        Pessoa lidoNumerico = parser.parsearLinha(alunoNumerico.toCsvLinha());
        Pessoa lidoTextual = parser.parsearLinha(alunoTextual.toCsvLinha());

        assertInstanceOf(Aluno.class, lidoNumerico);
        assertEquals("Turma-12", ((Aluno) lidoNumerico).getTurma());
        assertEquals(8.75, ((Aluno) lidoNumerico).getNota(), 0.001);

        assertInstanceOf(Aluno.class, lidoTextual);
        assertEquals("Turma-A", ((Aluno) lidoTextual).getTurma());
    }

    @Test
    void deveValidarSalarioEHigienizarEProtegerDisciplinasDoProfessor() {
        LocalDate nascimento = LocalDate.of(1980, 3, 15);

        assertThrows(IllegalArgumentException.class,
                () -> new Professor(1, "Carlos", nascimento, -0.01, List.of("Java")));

        List<String> entrada = new ArrayList<>(List.of("  Matematica ", "", "   ", "Fisica  "));
        Professor prof = new Professor(2, "Carlos Souza", nascimento, 5500.0, entrada);

        assertEquals(List.of("Matematica", "Fisica"), prof.getDisciplinas());

        // Alterar a lista externa original não afeta o estado interno
        entrada.add("Quimica");
        assertEquals(2, prof.getDisciplinas().size());

        // Lista retornada pelo getter deve ser imutável
        assertThrows(UnsupportedOperationException.class,
                () -> prof.getDisciplinas().add("Biologia"));

        // Professor sem disciplinas serializa com fallback "Geral"
        Professor profSemMateria = new Professor(3, "Diana Lima", nascimento, 4200.0, List.of());
        assertTrue(profSemMateria.toCsvLinha().contains(";Geral;"));
    }

    @Test
    void deveValidarEProtegerEncapsulamentoDeNotaEFrequencia() {
        LocalDate hoje = LocalDate.of(2026, 11, 24);

        assertThrows(IllegalArgumentException.class,
                () -> new Nota(0, hoje, 8.0, "POO", List.of(8.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Nota(1, null, 8.0, "POO", List.of(8.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Nota(1, hoje, 10.5, "POO", List.of(8.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Nota(1, hoje, 8.0, "  ", List.of(8.0)));

        Nota nota = new Nota(1, hoje, 8.5, "POO", List.of(8.0, 9.0));
        assertEquals(TipoRegistro.NOTA, nota.getTipo());
        assertThrows(UnsupportedOperationException.class,
                () -> nota.getHistoricoAvaliacoes().add(10.0));
        assertTrue(nota.gerarResumo().contains("POO"));

        assertThrows(IllegalArgumentException.class,
                () -> new Frequencia(1, hoje, -1.0, "Falta", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Frequencia(1, hoje, 100.1, "Excesso", List.of()));

        Frequencia freq = new Frequencia(2, hoje, 90.0, "", List.of("2026-11-20", "2026-11-24"));
        assertEquals(TipoRegistro.FREQUENCIA, freq.getTipo());
        assertThrows(UnsupportedOperationException.class,
                () -> freq.getDatasPresencas().add("2026-11-25"));
        assertTrue(freq.gerarResumo().contains("Nenhuma"));

        // Polimorfismo na hierarquia RegistroAcademico
        List<RegistroAcademico> registros = List.of(nota, freq);
        assertEquals(2, registros.size());
    }
}