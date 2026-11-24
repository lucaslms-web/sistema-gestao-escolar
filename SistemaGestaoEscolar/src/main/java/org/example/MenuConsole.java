package org.example;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorTexto;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.BoletimService;
import org.example.repositorio.Repositorio;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MenuConsole {

    private final Repositorio<Pessoa> repositorio;
    private final BoletimService boletimService;
    private final Formatador<Pessoa> formatadorTexto;
    private final Scanner scanner;

    public MenuConsole(Repositorio<Pessoa> repositorio) {
        this.repositorio = repositorio;
        this.boletimService = new BoletimService();
        this.formatadorTexto = new FormatadorTexto();
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n=== SISTEMA DE GESTÃO ESCOLAR [" + repositorio.getClass().getSimpleName() + "] ===");
            System.out.println("1. Adicionar registro (Aluno/Professor)");
            System.out.println("2. Buscar registro por ID");
            System.out.println("3. Listar registros e média geral");
            System.out.println("4. Remover registro por ID");
            System.out.println("5. Carregar ou Salvar arquivo CSV");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine().trim());
                switch (opcao) {
                    case 1 -> menuAdicionar();
                    case 2 -> menuBuscar();
                    case 3 -> menuListar();
                    case 4 -> menuRemover();
                    case 5 -> menuPersistencia();
                    case 0 -> System.out.println("Encerrando o sistema...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println("Erro na operação: " + e.getMessage());
            }
        }
    }

    private void menuAdicionar() {
        System.out.print("Tipo (1 - Aluno | 2 - Professor): ");
        int tipo = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();

        System.out.print("Data de nascimento (AAAA-MM-DD): ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());

        Pessoa novoRegistro;
        if (tipo == 1) {
            System.out.print("Nota (0.0 a 10.0): ");
            double nota = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Frequência (0 a 100): ");
            int freq = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Turma (ex: Turma-10): ");
            String turma = scanner.nextLine().trim();
            novoRegistro = new Aluno(id, nome, data, nota, freq, turma);
        } else {
            System.out.print("Salário: ");
            double salario = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Disciplinas (separadas por vírgula): ");
            List<String> disciplinas = Arrays.asList(scanner.nextLine().trim().split(","));
            novoRegistro = new Professor(id, nome, data, salario, disciplinas);
        }

        long t0 = System.nanoTime();
        boolean inserido = repositorio.adicionar(novoRegistro);
        double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;

        System.out.printf("%s (Tempo: %.4f ms)%n",
                inserido ? "Registro adicionado com sucesso!" : "Falha: já existe um registro com esse ID.",
                tempoMs);
    }

    private void menuBuscar() {
        System.out.print("Informe o ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        long t0 = System.nanoTime();
        Pessoa p = repositorio.buscarPorId(id);
        double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;

        if (p == null) {
            System.out.printf("Registro não encontrado. (Tempo: %.4f ms)%n", tempoMs);
            return;
        }

        // Chamada polimórfica via Formatador<Pessoa> -> p.exibirDetalhes()
        System.out.println(formatadorTexto.formatar(p));
        if (p instanceof Aluno aluno) {
            System.out.println("Situação no Boletim: " + boletimService.verificarSituacao(aluno));
        }
        System.out.printf("Tempo da busca: %.4f ms%n", tempoMs);
    }

    private void menuListar() {
        long t0 = System.nanoTime();
        List<Pessoa> todos = repositorio.listarTodos();
        double media = boletimService.calcularMediaNotas(todos);
        double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;

        System.out.println("Total de registros em memória: " + todos.size());
        System.out.printf("Média geral das notas dos alunos: %.2f%n", media);

        todos.stream().limit(15).forEach(p -> System.out.println("- " + formatadorTexto.formatar(p)));
        if (todos.size() > 15) {
            System.out.println("... (exibindo apenas os 15 primeiros registros)");
        }
        System.out.printf("Tempo de listagem e cálculo: %.4f ms%n", tempoMs);
    }

    private void menuRemover() {
        System.out.print("Informe o ID para remover: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        long t0 = System.nanoTime();
        boolean removido = repositorio.remover(id);
        double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;

        System.out.printf("%s (Tempo: %.4f ms)%n",
                removido ? "Registro removido com sucesso!" : "ID não encontrado.",
                tempoMs);
    }

    private void menuPersistencia() throws IOException {
        System.out.print("1 - Carregar de CSV | 2 - Salvar em CSV: ");
        int escolha = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Nome do arquivo (ex: dados_pequeno.csv): ");
        String arquivo = scanner.nextLine().trim();

        long t0 = System.nanoTime();
        if (escolha == 1) {
            ResultadoLeitura res = repositorio.carregarDeArquivo(arquivo);
            double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;
            System.out.printf("Carregados %d registros no repositório em %.2f ms.%n", res.getLinhasValidas(), tempoMs);
        } else if (escolha == 2) {
            repositorio.salvarEmArquivo(arquivo);
            double tempoMs = (System.nanoTime() - t0) / 1_000_000.0;
            System.out.printf("Repositório salvo com sucesso em %s (%.2f ms).%n", arquivo, tempoMs);
        }
    }
}