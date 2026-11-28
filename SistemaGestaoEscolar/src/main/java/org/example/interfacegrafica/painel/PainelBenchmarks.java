package org.example.interfacegrafica.painel;

import org.example.BenchmarkRunner;
import org.example.BenchmarkRunner.Estatisticas;
import org.example.BenchmarkRunner.ResultadoBenchmarkCompleto;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PainelBenchmarks extends JPanel {

    private final JCheckBox chkEtapa1;
    private final JCheckBox chkEtapa2;
    private final JCheckBox chkEtapa3;
    private final JCheckBox chkIncluir5M;
    private final JCheckBox chkSalvarMd;
    private final JButton btnExecutar;
    private final JProgressBar barraProgresso;
    private final JLabel lblStatus;

    private final EstatisticasTableModel modelEtapa1;
    private final EstatisticasTableModel modelEtapa2;
    private final EstatisticasTableModel modelEtapa3;
    private final JTextArea txtLog;

    public PainelBenchmarks() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Topo: Configurações e Botão Executar
        JPanel painelConfig = new JPanel(new BorderLayout(10, 8));
        painelConfig.setBackground(Color.WHITE);
        painelConfig.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel opcoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        opcoes.setOpaque(false);

        chkEtapa1 = new JCheckBox("Etapa 1 (Geração CSV)", true);
        chkEtapa2 = new JCheckBox("Etapa 2 (Leitura CSV)", true);
        chkEtapa3 = new JCheckBox("Etapa 3 (Locks vs Atores)", true);
        chkIncluir5M = new JCheckBox("Incluir Dataset Gigante (5M)", false);
        chkSalvarMd = new JCheckBox("Salvar resultados_benchmark.md", true);

        btnExecutar = new JButton("Executar Benchmark");
        btnExecutar.setBackground(new Color(22, 163, 74));
        btnExecutar.setForeground(Color.WHITE);
        btnExecutar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecutar.addActionListener(e -> iniciarBenchmark());

        opcoes.add(chkEtapa1);
        opcoes.add(chkEtapa2);
        opcoes.add(chkEtapa3);
        opcoes.add(chkIncluir5M);
        opcoes.add(chkSalvarMd);
        opcoes.add(btnExecutar);

        JPanel painelStatus = new JPanel(new BorderLayout(10, 0));
        painelStatus.setOpaque(false);
        lblStatus = new JLabel("Pronto para executar. Dica: desmarque '5M' para um teste rápido ou marque para a suíte completa.");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        barraProgresso = new JProgressBar();
        barraProgresso.setIndeterminate(false);
        barraProgresso.setVisible(false);

        painelStatus.add(lblStatus, BorderLayout.CENTER);
        painelStatus.add(barraProgresso, BorderLayout.EAST);

        painelConfig.add(opcoes, BorderLayout.NORTH);
        painelConfig.add(painelStatus, BorderLayout.SOUTH);

        // Centro: Abas com Tabelas Estatísticas e Console de Saída
        modelEtapa1 = new EstatisticasTableModel();
        modelEtapa2 = new EstatisticasTableModel();
        modelEtapa3 = new EstatisticasTableModel();

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLog.setText(BenchmarkRunner.gerarCabecalhoAmbiente(false));

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Etapa 1 — Geração", new JScrollPane(criarTabela(modelEtapa1)));
        abas.addTab("Etapa 2 — Leitura", new JScrollPane(criarTabela(modelEtapa2)));
        abas.addTab("Etapa 3 — Repositório (Locks vs Atores)", new JScrollPane(criarTabela(modelEtapa3)));
        abas.addTab("Log & Resumo Executivo", new JScrollPane(txtLog));

        add(painelConfig, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
    }

    private JTable criarTabela(EstatisticasTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        return table;
    }

    private void iniciarBenchmark() {
        boolean e1 = chkEtapa1.isSelected();
        boolean e2 = chkEtapa2.isSelected();
        boolean e3 = chkEtapa3.isSelected();

        if (!e1 && !e2 && !e3) {
            JOptionPane.showMessageDialog(this, "Selecione pelo menos uma etapa para executar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean incluir5M = chkIncluir5M.isSelected();
        boolean salvarMd = chkSalvarMd.isSelected();

        btnExecutar.setEnabled(false);
        barraProgresso.setVisible(true);
        barraProgresso.setIndeterminate(true);
        lblStatus.setText("Executando warmup JIT e medições cronometradas em segundo plano...");
        txtLog.setText("");

        SwingWorker<ResultadoBenchmarkCompleto, String> worker = new SwingWorker<>() {
            @Override
            protected ResultadoBenchmarkCompleto doInBackground() {
                return BenchmarkRunner.executarSuiteCompleta(
                        e1, e2, e3, incluir5M, salvarMd, this::publish
                );
            }

            @Override
            protected void process(List<String> linhas) {
                for (String linha : linhas) {
                    txtLog.append(linha + "\n");
                }
            }

            @Override
            protected void done() {
                btnExecutar.setEnabled(true);
                barraProgresso.setIndeterminate(false);
                barraProgresso.setVisible(false);
                try {
                    ResultadoBenchmarkCompleto res = get();
                    modelEtapa1.setDados(res.etapa1Geracao);
                    modelEtapa2.setDados(res.etapa2Leitura);
                    modelEtapa3.setDados(res.etapa3Repositorio);
                    txtLog.append("\n" + res.resumoExecutivo);
                    lblStatus.setText("Benchmark concluído! Navegue pelas abas para analisar os resultados.");
                } catch (Exception ex) {
                    lblStatus.setText("Erro ao executar benchmark.");
                    JOptionPane.showMessageDialog(PainelBenchmarks.this,
                            "Falha na execução do benchmark: " + ex.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private static class EstatisticasTableModel extends AbstractTableModel {
        private final String[] colunas = {
                "Cenário / Volume", "Execução", "Média (ms)", "Mediana (ms)",
                "Desvio Padrão", "CV (%)", "Vazão", "Speedup", "Eficiência"
        };
        private List<Estatisticas> dados = new ArrayList<>();

        public void setDados(List<Estatisticas> novosDados) {
            this.dados = novosDados;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return dados.size();
        }

        @Override
        public int getColumnCount() {
            return colunas.length;
        }

        @Override
        public String getColumnName(int column) {
            return colunas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Estatisticas e = dados.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.cenario;
                case 1 -> e.modo;
                case 2 -> String.format(Locale.US, "%.2f", e.mediaMs);
                case 3 -> String.format(Locale.US, "%.2f", e.medianaMs);
                case 4 -> String.format(Locale.US, "%.2f", e.desvioPadraoMs);
                case 5 -> String.format(Locale.US, "%.1f%%", e.coefVariacaoPct);
                case 6 -> String.format(Locale.US, "%.0f %s", e.throughputPorSeg, e.unidadeCarga);
                case 7 -> String.format(Locale.US, "%.2fx", e.speedup);
                case 8 -> String.format(Locale.US, "%.1f%%", e.eficienciaPct);
                default -> "";
            };
        }
    }
}