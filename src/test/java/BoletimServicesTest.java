import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.repositorio.BoletimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoletimServiceTest {

    private BoletimService boletimService;

    @BeforeEach
    void setUp() {
        boletimService = new BoletimService();
    }

    @Test
    void deveRespeitarValoresExatosDeFronteira() {
        // Fronteiras de aprovação e recuperação com frequência mínima exata (75%)
        assertEquals("APROVADO", boletimService.verificarSituacao(7.0, 75));
        assertEquals("RECUPERACAO", boletimService.verificarSituacao(6.99, 75));
        assertEquals("RECUPERACAO", boletimService.verificarSituacao(5.0, 75));
        assertEquals("REPROVADO POR NOTA", boletimService.verificarSituacao(4.99, 75));

        // Frequência abaixo de 75% reprova mesmo com nota máxima
        assertEquals("REPROVADO POR FALTA", boletimService.verificarSituacao(10.0, 74));
        assertEquals("REPROVADO POR FALTA", boletimService.verificarSituacao(0.0, 0));
    }

    @Test
    void deveRejeitarAlunoNulo() {
        assertThrows(IllegalArgumentException.class, () -> boletimService.verificarSituacao(null));
    }

    @Test
    void deveCalcularMediaApenasSobreAlunosIgnorandoProfessoresEListasVazias() {
        assertEquals(0.0, boletimService.calcularMediaNotas(List.of()), 0.0001);

        Professor prof1 = new Professor(1, "Carlos", LocalDate.of(1980, 1, 1), 5000.0, List.of("POO"));
        Professor prof2 = new Professor(2, "Diana", LocalDate.of(1982, 2, 2), 6000.0, List.of("BD"));

        assertEquals(0.0, boletimService.calcularMediaNotas(List.of(prof1, prof2)), 0.0001);

        Aluno a1 = new Aluno(3, "Lucas", LocalDate.of(2003, 3, 3), 8.0, 90, "Turma-1");
        Aluno a2 = new Aluno(4, "Ana", LocalDate.of(2003, 4, 4), 6.0, 85, "Turma-1");

        List<Pessoa> misto = List.of(prof1, a1, prof2, a2);
        assertEquals(7.0, boletimService.calcularMediaNotas(misto), 0.0001);
    }
}