package org.example.interfacegrafica.painel;

import org.example.dominio.Aluno;
import org.example.interfacegrafica.controlador.ControladorBoletim;
import org.example.interfacegrafica.controlador.ControladorBoletim.ResumoBoletim;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PainelBoletins extends JPanel {

    private final ControladorBoletim controlador;
    private final JTextField txtFiltro;
    private final JTextField txtIdDireto;
    private final JTable tabela;
    private final BoletimTableModel tableModel;

    private final JLabel lblAlunoSelecionado;
    private final JLabel lblNotaFreq;
    private final JLabel lblSituacao;
    private final JLabel lblMediaTurma;

    public PainelBoletins(ControladorBoletim controlador) {
        this.controlador = controlador;

        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Topo: Pesquisa e Consulta Direta por ID
        JPanel barraPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        barraPesquisa.setBackground(Color.WHITE);
        barraPesquisa.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        txtFiltro = new JTextField(18);
        JButton btnFiltrar = new JButton("Filtrar Lista");
        btnFiltrar.addActionListener(e -> atualizarTabela());
        txtFiltro.addActionListener(e -> atualizarTabela());

        txtIdDireto = new JTextField(8);
        JButton btnConsultarId = new JButton("Consultar ID Atômico");
        btnConsultarId.setBackground(new Color(37, 99, 235));
        btnConsultarId.setForeground(Color.WHITE);
        btnConsultarId.addActionListener(e -> consultarPorIdDireto());

        barraPesquisa.add(new JLabel("Filtrar Aluno:"));
        barraPesquisa.add(txtFiltro);
        barraPesquisa.add(btnFiltrar);
        barraPesquisa.add(new JLabel("   |   ID Exato:"));
        barraPesquisa.add(txtIdDireto);
        barraPesquisa.add(btnConsultarId);

        // Centro: Tabela de Boletins
        tableModel = new BoletimTableModel();
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                exibirResumo(tableModel.getItemAt(tabela.getSelectedRow()));
            }
        });

        // Base: Card de Detalhes do Boletim Selecionado
        JPanel painelDetalhes = new JPanel(new GridLayout(1, 4, 12, 0));
        painelDetalhes.setBackground(Color.WHITE);
        painelDetalhes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Boletim Individual (Critérios BoletimService: Média >= 7.0 | Frequência >= 75%)"),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        lblAlunoSelecionado = new JLabel("Selecione um aluno na tabela");
        lblAlunoSelecionado.setFont(new Font("SansSerif", Font.BOLD, 13));

        lblNotaFreq = new JLabel("Nota: — | Frequência: —");
        lblNotaFreq.setFont(new Font("SansSerif", Font.PLAIN, 13));

        lblSituacao = new JLabel("Situação: —");
        lblSituacao.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblMediaTurma = new JLabel("Média Geral dos Alunos: —");
        lblMediaTurma.setFont(new Font("SansSerif", Font.BOLD, 13));

        painelDetalhes.add(lblAlunoSelecionado);
        painelDetalhes.add(lblNotaFreq);
        painelDetalhes.add(lblSituacao);
        painelDetalhes.add(lblMediaTurma);

        add(barraPesquisa, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelDetalhes, BorderLayout.SOUTH);
    }

    public void atualizarTabela() {
        List<ResumoBoletim> lista = controlador.listarBoletins(txtFiltro.getText());
        tableModel.setDados(lista);
        double media = controlador.calcularMediaGeral();
        lblMediaTurma.setText(lista.isEmpty()
                ? "Média Geral: —"
                : String.format(Locale.US, "Média Geral dos Alunos: %.2f", media));
    }

    private void consultarPorIdDireto() {
        try {
            int id = Integer.parseInt(txtIdDireto.getText().trim());
            ResumoBoletim resumo = controlador.consultarPorId(id);
            if (resumo == null) {
                JOptionPane.showMessageDialog(this, "Nenhum aluno encontrado com o ID " + id + ".", "Não Encontrado", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            exibirResumo(resumo);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um número inteiro válido para o ID.", "Entrada Inválida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exibirResumo(ResumoBoletim resumo) {
        Aluno a = resumo.aluno;
        lblAlunoSelecionado.setText(String.format("ID %d • %s (%s)", a.getId(), a.getNome(), a.getTurma()));
        lblNotaFreq.setText(String.format(Locale.US, "Nota: %.2f | Frequência: %d%%", a.getNota(), a.getFrequencia()));
        lblSituacao.setText("Situação: " + resumo.situacao);

        if ("APROVADO".equals(resumo.situacao)) {
            lblSituacao.setForeground(new Color(22, 163, 74));
        } else if ("RECUPERACAO".equals(resumo.situacao)) {
            lblSituacao.setForeground(new Color(217, 119, 6));
        } else {
            lblSituacao.setForeground(new Color(220, 38, 38));
        }
    }

    private static class BoletimTableModel extends AbstractTableModel {
        private final String[] colunas = {"ID", "Aluno", "Turma", "Nota", "Frequência", "Situação"};
        private List<ResumoBoletim> dados = new ArrayList<>();

        public void setDados(List<ResumoBoletim> novosDados) {
            this.dados = novosDados;
            fireTableDataChanged();
        }

        public ResumoBoletim getItemAt(int row) {
            return dados.get(row);
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
            ResumoBoletim item = dados.get(rowIndex);
            Aluno a = item.aluno;
            return switch (columnIndex) {
                case 0 -> a.getId();
                case 1 -> a.getNome();
                case 2 -> a.getTurma();
                case 3 -> String.format(Locale.US, "%.2f", a.getNota());
                case 4 -> a.getFrequencia() + "%";
                case 5 -> item.situacao;
                default -> "";
            };
        }
    }
}