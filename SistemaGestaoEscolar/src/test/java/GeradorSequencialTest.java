import org.example.dominio.Pessoa;
import org.example.geracao.GeradorSequencial;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorSequencialTest {

    @Test
    void deveGerarArquivoValidoComIdsSequenciaisSemDuplicidade(@TempDir Path tempDir) {
        GeradorSequencial gerador = new GeradorSequencial();
        LeitorSequencial leitor = new LeitorSequencial();

        int totalLinhas = 1_500;
        Path arquivo = tempDir.resolve("saida_sequencial.csv");

        double tempoMs = gerador.gerarArquivo(arquivo.toString(), totalLinhas, 42L, false);
        assertTrue(tempoMs >= 0.0);

        ResultadoLeitura resultado = leitor.lerArquivo(arquivo.toString(), false);
        assertEquals(totalLinhas, resultado.getLinhasValidas());
        assertEquals(0, resultado.getLinhasInvalidas());

        List<Pessoa> registros = resultado.getRegistros();
        for (int i = 0; i < totalLinhas; i++) {
            assertEquals(i + 1, registros.get(i).getId(), "ID fora de sequência no índice " + i);
        }
    }

    @Test
    void deveSerDeterministicoComMesmaSeedEDiferenteComSeedsDistintas(@TempDir Path tempDir) throws IOException {
        GeradorSequencial gerador = new GeradorSequencial();

        Path run1 = tempDir.resolve("seq_run1.csv");
        Path run2 = tempDir.resolve("seq_run2.csv");
        Path runSeedDiferente = tempDir.resolve("seq_run3.csv");

        gerador.gerarArquivo(run1.toString(), 500, 123L, false);
        gerador.gerarArquivo(run2.toString(), 500, 123L, false);
        gerador.gerarArquivo(runSeedDiferente.toString(), 500, 999L, false);

        String conteudo1 = Files.readString(run1);
        assertEquals(conteudo1, Files.readString(run2));
        assertNotEquals(conteudo1, Files.readString(runSeedDiferente));
    }

    @Test
    void deveLancarExcecaoQuandoDiretorioDestinoNaoExiste(@TempDir Path tempDir) {
        GeradorSequencial gerador = new GeradorSequencial();
        Path caminhoInexistente = tempDir.resolve("pasta_inexistente").resolve("arquivo.csv");

        assertThrows(UncheckedIOException.class,
                () -> gerador.gerarArquivo(caminhoInexistente.toString(), 10, 42L, false));
    }
}