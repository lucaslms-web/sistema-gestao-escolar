package org.example;

import org.example.dominio.Pessoa;
import org.example.geracao.GeradorSequencial;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.Repositorio;
import org.example.repositorio.RepositorioComAtores;
import org.example.repositorio.RepositorioComLocks;
import org.example.repositorio.SimuladorClientesConcorrentes;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== BATERIA DE TESTES DE GERAÇÃO (ETAPA 1) ===\n");
        GeradorSequencial geradorSeq = new GeradorSequencial();
        geradorSeq.gerarTodosOsArquivos();

        System.out.println("\n=== VALIDAÇÃO FINAL DE LEITURA SEQUENCIAL ===\n");
        LeitorSequencial leitorSeq = new LeitorSequencial();

        ResultadoLeitura resPequeno = leitorSeq.lerArquivo("dados_pequeno.csv");
        ResultadoLeitura resMedio = leitorSeq.lerArquivo("dados_medio.csv");
        ResultadoLeitura resGrande = leitorSeq.lerArquivo("dados_grande.csv");
        ResultadoLeitura resGigante = leitorSeq.lerArquivo("dados_gigante.csv");

        boolean integro = resPequeno.getLinhasValidas() == 1_000 && resPequeno.getLinhasInvalidas() == 0
                && resMedio.getLinhasValidas() == 100_000 && resMedio.getLinhasInvalidas() == 0
                && resGrande.getLinhasValidas() == 1_000_000 && resGrande.getLinhasInvalidas() == 0
                && resGigante.getLinhasValidas() == 5_000_000 && resGigante.getLinhasInvalidas() == 0;

        System.out.println("\nIntegridade dos 4 arquivos validada: " + (integro ? "OK (100% válidos)" : "FALHA"));

        if (!resPequeno.getRegistros().isEmpty()) {
            Pessoa primeiro = resPequeno.getRegistros().get(0);
            System.out.println("Amostra polimórfica: " + primeiro.exibirDetalhes());
        }

        System.out.println("\n=== VALIDAÇÃO LEITOR CONCORRENTE ===\n");
        LeitorConcorrente leitorConc = new LeitorConcorrente();
        ResultadoLeitura resConc = leitorConc.lerArquivoConcorrente("dados_grande.csv", 4);

        System.out.println("Contagem idêntica (Sequencial vs Concorrente): "
                + (resGrande.getLinhasValidas() == resConc.getLinhasValidas()));

        System.out.println("\n=== VALIDAÇÃO REPOSITÓRIO COM LOCKS ===\n");
        RepositorioComLocks repoLocks = new RepositorioComLocks();
        repoLocks.carregarDeArquivoConcorrente("dados_medio.csv", 4);

        SimuladorClientesConcorrentes simulador = new SimuladorClientesConcorrentes();
        SimuladorClientesConcorrentes.ResultadoSimulacao simLocks =
                simulador.executarCargaConcorrente(repoLocks, 4, 100_000, 100_000, 42L);

        System.out.printf("Simulação Locks (4t): %d ops em %.2f ms (%.0f ops/s) | Tamanho final: %d%n",
                simLocks.totalOperacoes, simLocks.tempoMs, simLocks.opsPorSegundo, simLocks.tamanhoFinalRepositorio);

        System.out.println("\n=== VALIDAÇÃO REPOSITÓRIO COM ATORES (APACHE PEKKO TYPED) ===\n");
        try (RepositorioComAtores repoAtores = new RepositorioComAtores()) {
            repoAtores.carregarDeArquivoConcorrente("dados_medio.csv", 4);
            SimuladorClientesConcorrentes.ResultadoSimulacao simAtores =
                    simulador.executarCargaConcorrenteAtores(repoAtores, 4, 100_000, 100_000, 42L);

            System.out.printf("Simulação Atores (4t): %d ops em %.2f ms (%.0f ops/s) | Tamanho final: %d%n",
                    simAtores.totalOperacoes, simAtores.tempoMs, simAtores.opsPorSegundo, simAtores.tamanhoFinalRepositorio);
        }

        System.out.println("\n=================================================\n");
        Repositorio<Pessoa> repoMenu = new RepositorioComLocks();
        repoMenu.carregarDeArquivo("dados_pequeno.csv");

        MenuConsole menu = new MenuConsole(repoMenu);
        menu.iniciar();
    }
}