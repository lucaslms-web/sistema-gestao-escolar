package org.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== BATETIA DE TESTES DE GERAÇÃO (ETAPA 1) ===\n");

        // 1. Teste Sequencial
        GeradorSequencial geradorSeq = new GeradorSequencial();
        geradorSeq.gerarTodosOsArquivos();

        System.out.println("\n------------------------------------------------\n");

        // 2. Teste Concorrente
        GeradorConcorrente geradorConc = new GeradorConcorrente();
        geradorConc.gerarTodosOsArquivosConcorrentes();
    }
}