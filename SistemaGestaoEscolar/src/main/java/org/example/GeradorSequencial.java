package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Random;

public class GeradorSequencial {

    private static final String[] NOMES = {"Ana", "Bruno", "Carlos", "Diana", "Eduardo", "Fernanda", "Gustavo", "Helena"};
    private static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Rodrigues"};
    private static final String[] MATERIAS = {"Matematica", "Fisica", "Historia", "Geografia", "Biologia", "Quimica"};

    public void gerarTodosOsArquivos() {
        int[] tamanhos = {1_000, 100_000, 1_000_000, 5_000_000};
        String[] nomesFicheiros = {"dados_pequeno.csv", "dados_medio.csv", "dados_grande.csv", "dados_gigante.csv"};
        long seedBase = 42L;

        System.out.println("A iniciar a geração sequencial de ficheiros...\n");

        for (int i = 0; i < tamanhos.length; i++) {
            gerarArquivo(nomesFicheiros[i], tamanhos[i], seedBase + i);
        }

        System.out.println("\nGeração concluída!");
    }

    private void gerarArquivo(String nomeFicheiro, int qtdLinhas, long seed) {
        Random random = new Random(seed);
        long inicioTempo = System.nanoTime();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeFicheiro))) {
            for (int id = 1; id <= qtdLinhas; id++) {
                boolean isAluno = random.nextBoolean();
                String tipo = isAluno ? "ALUNO" : "PROFESSOR";

                String nome = NOMES[random.nextInt(NOMES.length)] + " " + SOBRENOMES[random.nextInt(SOBRENOMES.length)];
                int ano = 1970 + random.nextInt(41);
                int mes = 1 + random.nextInt(12);
                int dia = 1 + random.nextInt(28);
                String data = LocalDate.of(ano, mes, dia).toString();

                double valor;
                String listaExtra;

                if (isAluno) {
                    valor = random.nextDouble() * 10;
                    listaExtra = (random.nextInt(30) + 70) + "," + (random.nextInt(30) + 70);
                } else {
                    valor = 2000 + (random.nextDouble() * 8000);
                    listaExtra = MATERIAS[random.nextInt(MATERIAS.length)] + "," + MATERIAS[random.nextInt(MATERIAS.length)];
                }

                String linha = String.format(Locale.US, "%d;%s;%s;%.2f;%s;%s\n",
                        id, tipo, nome, valor, listaExtra, data);
                bw.write(linha);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao escrever o ficheiro " + nomeFicheiro + ": " + e.getMessage());
        }

        long fimTempo = System.nanoTime();
        double tempoMs = (fimTempo - inicioTempo) / 1_000_000.0;

        // Cálculo do tamanho final do ficheiro gerado no disco em MB
        File arquivoGerado = new File(nomeFicheiro);
        double tamanhoMB = arquivoGerado.length() / (1024.0 * 1024.0);

        System.out.printf("Ficheiro: %-20s | Linhas: %-9d | Tempo: %8.2f ms | Tamanho: %6.2f MB%n",
                nomeFicheiro, qtdLinhas, tempoMs, tamanhoMB);
    }
}