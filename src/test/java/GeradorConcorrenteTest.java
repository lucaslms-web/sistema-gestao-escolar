import org.example.dominio.Pessoa;
import org.example.geracao.GeradorConcorrente;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorConcorrenteTest {

    @Test
    void deveGerarArquivoValidoSemIdsDuplicadosPara1_2_4_E_8Threads(@TempDir Path tempDir) {
        GeradorConcorrente gerador = new GeradorConcorrente();
        LeitorSequencial leitor = new LeitorSequencial();
        int totalLinhas = 2_503; // Número primo para testar divisão desigual entre threads
        int[] configuracoesThreads = {1, 2, 4, 8};

        for (int threads : configuracoesThreads) {
            Path arquivo = tempDir.resolve("teste_" + threads + "_threads.csv");
            gerador.gerarArquivoConcorrente(arquivo.toString(), totalLinhas, threads, 42L);

            ResultadoLeitura resultado = leitor.lerArquivo(arquivo.toString());
            assertEquals(totalLinhas, resultado.getLinhasValidas(), "Falha na contagem com " + threads + " threads");
            assertEquals(0, resultado.getLinhasInvalidas());

            List<Pessoa> registros = resultado.getRegistros();
            for (int i = 0; i < totalLinhas; i++) {
                assertEquals(i + 1, registros.get(i).getId(), "ID fora de ordem ou duplicado no índice " + i);
            }
        }
    }

    @Test
    void deveSerDeterministicoComMesmaSeedENumeroDeThreads(@TempDir Path tempDir) throws IOException {
        GeradorConcorrente gerador = new GeradorConcorrente();
        Path execucao1 = tempDir.resolve("run1.csv");
        Path execucao2 = tempDir.resolve("run2.csv");

        gerador.gerarArquivoConcorrente(execucao1.toString(), 1_000, 4, 99L);
        gerador.gerarArquivoConcorrente(execucao2.toString(), 1_000, 4, 99L);

        assertEquals(Files.readString(execucao1), Files.readString(execucao2));
    }
}