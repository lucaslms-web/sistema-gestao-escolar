package org.example.interfacegrafica;

import org.example.interfacegrafica.componentes.BarraSuperior;
import org.example.interfacegrafica.componentes.MenuLateral;
import org.example.interfacegrafica.controlador.ControladorAluno;
import org.example.interfacegrafica.controlador.ControladorBoletim;
import org.example.interfacegrafica.controlador.ControladorProfessor;
import org.example.interfacegrafica.controlador.GerenciadorRepositorio;
import org.example.interfacegrafica.controlador.GerenciadorRepositorio.TipoMotor;
import org.example.interfacegrafica.painel.PainelAlunos;
import org.example.interfacegrafica.painel.PainelBenchmarks;
import org.example.interfacegrafica.painel.PainelBoletins;
import org.example.interfacegrafica.painel.PainelInicio;
import org.example.interfacegrafica.painel.PainelProfessores;
import org.example.leitura.ResultadoLeitura;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Locale;

public class JanelaPrincipal extends JFrame {

    private final GerenciadorRepositorio gerenciador;
    private final MenuLateral menuLateral;
    private final BarraSuperior barraSuperior;
    private final CardLayout cardLayout;
    private final JPanel areaCentral;

    private final PainelInicio painelInicio;
    private final PainelAlunos painelAlunos;
    private final PainelProfessores painelProfessores;
    private final PainelBoletins painelBoletins;
    private final PainelBenchmarks painelBenchmarks;

    public JanelaPrincipal(GerenciadorRepositorio gerenciador) {
        this.gerenciador = gerenciador;

        setTitle("Sistema de Gestão Escolar — UFF");
        setSize(1180, 720);
        setMinimumSize(new java.awt.Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ControladorAluno controladorAluno = new ControladorAluno(gerenciador);
        ControladorProfessor controladorProfessor = new ControladorProfessor(gerenciador);
        ControladorBoletim controladorBoletim = new ControladorBoletim(gerenciador);

        menuLateral = new MenuLateral(this::navegarPara);
        barraSuperior = new BarraSuperior(
                this::trocarMotorRepositorio,
                this::carregarArquivoCsvDialog,
                this::salvarArquivoCsvDialog
        );

        cardLayout = new CardLayout();
        areaCentral = new JPanel(cardLayout);

        painelInicio = new PainelInicio(gerenciador, controladorBoletim, this::navegarPara, this::carregarArquivoCsvDialog);
        painelAlunos = new PainelAlunos(controladorAluno, this::atualizarStatusGeral);
        painelProfessores = new PainelProfessores(controladorProfessor, this::atualizarStatusGeral);
        painelBoletins = new PainelBoletins(controladorBoletim);
        painelBenchmarks = new PainelBenchmarks();

        areaCentral.add(painelInicio, MenuLateral.TELA_INICIO);
        areaCentral.add(painelAlunos, MenuLateral.TELA_ALUNOS);
        areaCentral.add(painelProfessores, MenuLateral.TELA_PROFESSORES);
        areaCentral.add(painelBoletins, MenuLateral.TELA_BOLETINS);
        areaCentral.add(painelBenchmarks, MenuLateral.TELA_BENCHMARKS);

        JPanel ladoDireito = new JPanel(new BorderLayout());
        ladoDireito.add(barraSuperior, BorderLayout.NORTH);
        ladoDireito.add(areaCentral, BorderLayout.CENTER);

        add(menuLateral, BorderLayout.WEST);
        add(ladoDireito, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gerenciador.close();
            }
        });

        atualizarStatusGeral();
    }

    public void navegarPara(String tela) {
        menuLateral.marcarAtivo(tela);
        cardLayout.show(areaCentral, tela);

        switch (tela) {
            case MenuLateral.TELA_INICIO -> {
                barraSuperior.atualizarTitulo("Início — Visão Geral");
                painelInicio.atualizarDados();
            }
            case MenuLateral.TELA_ALUNOS -> {
                barraSuperior.atualizarTitulo("Gestão de Alunos");
                painelAlunos.atualizarTabela();
            }
            case MenuLateral.TELA_PROFESSORES -> {
                barraSuperior.atualizarTitulo("Gestão de Professores");
                painelProfessores.atualizarTabela();
            }
            case MenuLateral.TELA_BOLETINS -> {
                barraSuperior.atualizarTitulo("Consulta de Boletins");
                painelBoletins.atualizarTabela();
            }
            case MenuLateral.TELA_BENCHMARKS -> {
                barraSuperior.atualizarTitulo("Benchmarks de Desempenho e Concorrência");
            }
        }
    }

    private void trocarMotorRepositorio(TipoMotor novoMotor) {
        barraSuperior.setControlesHabilitados(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                gerenciador.alternarMotor(novoMotor);
                return null;
            }

            @Override
            protected void done() {
                barraSuperior.setControlesHabilitados(true);
                try {
                    get();
                    atualizarTodasAsTelas();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JanelaPrincipal.this,
                            "Erro ao alternar motor de repositório: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void carregarArquivoCsvDialog() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Selecione o arquivo CSV para importar");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = chooser.getSelectedFile();
        barraSuperior.setControlesHabilitados(false);

        SwingWorker<ResultadoLeitura, Void> worker = new SwingWorker<>() {
            @Override
            protected ResultadoLeitura doInBackground() {
                return gerenciador.carregarCsv(arquivo.getAbsolutePath());
            }

            @Override
            protected void done() {
                barraSuperior.setControlesHabilitados(true);
                try {
                    ResultadoLeitura res = get();
                    atualizarTodasAsTelas();
                    JOptionPane.showMessageDialog(JanelaPrincipal.this,
                            String.format(Locale.US,
                                    "Arquivo '%s' carregado com sucesso!\n• Linhas válidas: %d\n• Linhas inválidas ignoradas: %d\n• Tempo de leitura: %.2f ms",
                                    arquivo.getName(), res.getLinhasValidas(), res.getLinhasInvalidas(), res.getTempoMs()),
                            "Importação Concluída", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JanelaPrincipal.this,
                            "Falha ao ler o arquivo CSV: " + ex.getMessage(),
                            "Erro de Leitura", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void salvarArquivoCsvDialog() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Salvar registros em arquivo CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selecionado = chooser.getSelectedFile();
        String caminho = selecionado.getAbsolutePath().endsWith(".csv")
                ? selecionado.getAbsolutePath()
                : selecionado.getAbsolutePath() + ".csv";

        barraSuperior.setControlesHabilitados(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                gerenciador.salvarCsv(caminho);
                return null;
            }

            @Override
            protected void done() {
                barraSuperior.setControlesHabilitados(true);
                try {
                    get();
                    atualizarStatusGeral();
                    JOptionPane.showMessageDialog(JanelaPrincipal.this,
                            "Registros exportados com sucesso para:\n" + caminho,
                            "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(JanelaPrincipal.this,
                            "Erro ao exportar CSV: " + ex.getMessage(),
                            "Erro de Gravação", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void atualizarTodasAsTelas() {
        atualizarStatusGeral();
        painelInicio.atualizarDados();
        painelAlunos.atualizarTabela();
        painelProfessores.atualizarTabela();
        painelBoletins.atualizarTabela();
    }

    private void atualizarStatusGeral() {
        int total = gerenciador.getRepositorio().tamanho();
        barraSuperior.atualizarStatus(total, gerenciador.getArquivoCarregadoAtual());
        painelInicio.atualizarDados();
    }
}