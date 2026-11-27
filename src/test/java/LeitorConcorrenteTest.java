import org.example.dominio.Pessoa;
import org.example.geracao.GeradorConcorrente;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeitorConcorrenteTest {

    @Test
    void deveProduzirResultadoIdenticoAoSequencialPara1_2_4_E_8Threads(@TempDir Path tempDir) {
        GeradorConcorrente gerador = new GeradorConcorrente();
        LeitorSequencial leitorSeq = new LeitorSequencial();
        LeitorConcorrente leitorConc = new LeitorConcorrente();

        Path arquivo = tempDir.resolve("dataset_concorrente.csv");
        gerador.gerarArquivoConcorrente(arquivo.toString(), 600, 4, 42L);

        ResultadoLeitura esperado = leitorSeq.lerArquivo(arquivo.toString());
        int[] configuracoesThreads = {1, 2, 4, 8};

        for (int threads : configuracoesThreads) {
            // Lote de 25 linhas força 24 tarefas paralelas
            ResultadoLeitura atual = leitorConc.lerArquivoConcorrente(arquivo.toString(), threads, 25);

            assertEquals(esperado.getLinhasValidas(), atual.getLinhasValidas(),
                    "Divergência de linhas válidas com " + threads + " threads");
            assertEquals(esperado.getLinhasInvalidas(), atual.getLinhasInvalidas(),
                    "Divergência de linhas inválidas com " + threads + " threads");
            assertEquals(esperado.getRegistros().size(), atual.getRegistros().size());

            List<Pessoa> listaEsperada = esperado.getRegistros();
            List<Pessoa> listaAtual = atual.getRegistros();
            for (int i = 0; i < listaEsperada.size(); i++) {
                assertEquals(listaEsperada.get(i).getId(), listaAtual.get(i).getId());
                assertEquals(listaEsperada.get(i).getNome(), listaAtual.get(i).getNome());
            }
        }
    }

    @Test
    void deveIsolarLinhasInvalidasIdenticamenteAoSequencialEmMultiplasThreads(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("misto_erros.csv");
        List<String> linhas = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            linhas.add(i + ";ALUNO;Aluno Teste;8.50;90,10;2004-05-10");
        }
        // Injeta os 3 tipos de falhas obrigatórios
        linhas.add("51;ALUNO;Coluna Faltando;7.00;85,10");             // Coluna faltando
        linhas.add("52;PROFESSOR;Data Errada;4500.00;Fisica;99-99-9999"); // Data inválida
        linhas.add("53;DIRETOR;Enum Invalido;9000.00;Gestao;1980-01-01"); // Enum inválido

        for (int i = 54; i <= 100; i++) {
            linhas.add(i + ";PROFESSOR;Prof Teste;5200.00;Matematica,Fisica;1982-03-15");
        }

        Files.write(arquivo, linhas);

        LeitorSequencial leitorSeq = new LeitorSequencial();
        LeitorConcorrente leitorConc = new LeitorConcorrente();

        ResultadoLeitura esperado = leitorSeq.lerArquivo(arquivo.toString());
        assertEquals(97, esperado.getLinhasValidas());
        assertEquals(3, esperado.getLinhasInvalidas());

        for (int threads : new int[]{1, 2, 4, 8}) {
            ResultadoLeitura atual = leitorConc.lerArquivoConcorrente(arquivo.toString(), threads, 10);
            assertEquals(esperado.getLinhasValidas(), atual.getLinhasValidas());
            assertEquals(esperado.getLinhasInvalidas(), atual.getLinhasInvalidas());
        }
    }

    @Test
    void deveLancarExcecaoParaParametrosOuArquivoInvalido() {
        LeitorConcorrente leitorConc = new LeitorConcorrente();

        assertThrows(IllegalArgumentException.class,
                () -> leitorConc.lerArquivoConcorrente("dados_pequeno.csv", 0));
        assertThrows(IllegalArgumentException.class,
                () -> leitorConc.lerArquivoConcorrente("dados_pequeno.csv", 2, 0));
        assertThrows(RuntimeException.class,
                () -> leitorConc.lerArquivoConcorrente("arquivo_inexistente_xyz.csv", 2));
    }
}