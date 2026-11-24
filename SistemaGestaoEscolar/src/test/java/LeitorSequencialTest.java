import org.example.dominio.Aluno;
import org.example.dominio.Professor;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeitorSequencialTest {

    private LeitorSequencial leitor;

    @BeforeEach
    void setUp() {
        leitor = new LeitorSequencial();
    }

    @Test
    void deveLerLinhasValidasEInstanciarSubclassesCorretas(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("validos.csv");
        List<String> linhas = List.of(
                "1;ALUNO;Ana Silva;8.50;85,90;2002-05-14",
                "2;PROFESSOR;Carlos Souza;5400.00;Matematica,Fisica;1980-11-20"
        );
        Files.write(arquivo, linhas);

        ResultadoLeitura resultado = leitor.lerArquivo(arquivo.toString());

        assertEquals(2, resultado.getLinhasValidas());
        assertEquals(0, resultado.getLinhasInvalidas());
        assertInstanceOf(Aluno.class, resultado.getRegistros().get(0));
        assertInstanceOf(Professor.class, resultado.getRegistros().get(1));
    }

    @Test
    void deveRejeitarLinhaComColunaFaltandoSemDerrubarPrograma(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("coluna_faltando.csv");
        List<String> linhas = List.of(
                "1;ALUNO;Bruno Costa;7.20;80,85",
                "2;ALUNO;Diana Oliveira;9.10;95,98;2003-03-10"
        );
        Files.write(arquivo, linhas);

        ResultadoLeitura resultado = assertDoesNotThrow(() -> leitor.lerArquivo(arquivo.toString()));

        assertEquals(1, resultado.getLinhasValidas());
        assertEquals(1, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRejeitarLinhaComDataInvalidaSemDerrubarPrograma(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("data_invalida.csv");
        List<String> linhas = List.of(
                "1;ALUNO;Eduardo Pereira;6.50;75,80;2025-02-30",
                "2;PROFESSOR;Fernanda Lima;4800.00;Historia,Geografia;data-errada",
                "3;ALUNO;Gustavo Santos;8.00;88,92;2001-08-22"
        );
        Files.write(arquivo, linhas);

        ResultadoLeitura resultado = assertDoesNotThrow(() -> leitor.lerArquivo(arquivo.toString()));

        assertEquals(1, resultado.getLinhasValidas());
        assertEquals(2, resultado.getLinhasInvalidas());
    }

    @Test
    void deveRejeitarLinhaComEnumInvalidoSemDerrubarPrograma(@TempDir Path tempDir) throws IOException {
        Path arquivo = tempDir.resolve("enum_invalido.csv");
        List<String> linhas = List.of(
                "1;DIRETOR;Helena Rodrigues;9000.00;Gestao;1975-01-15",
                "2;;Ana Silva;8.50;85,90;2002-05-14",
                "3;PROFESSOR;Bruno Silva;6100.00;Biologia,Quimica;1985-07-09"
        );
        Files.write(arquivo, linhas);

        ResultadoLeitura resultado = assertDoesNotThrow(() -> leitor.lerArquivo(arquivo.toString()));

        assertEquals(1, resultado.getLinhasValidas());
        assertEquals(2, resultado.getLinhasInvalidas());
    }
}