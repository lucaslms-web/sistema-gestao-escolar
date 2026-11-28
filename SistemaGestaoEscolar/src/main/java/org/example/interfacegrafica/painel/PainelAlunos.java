package org.example.interfacegrafica.painel;

import org.example.dominio.Aluno;
import org.example.interfacegrafica.controlador.ControladorAluno;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PainelAlunos extends JPanel {

    private final ControladorAluno controlador;
    private final Runnable onDadosAlterados;

    private final JTextField txtBusca;
    private final JTable tabela;
    private final AlunoTableModel tableModel;

    private final JTextField txtId;
    private final JTextField txtNome;
    private final JTextField txtNascimento;
    private final JTextField txtNota;
    private final JTextField txtFrequencia;
    private final JTextField txtTurma;
    private final JButton btnSalvar;
    private final JButton btnExcluir;

    private boolean modoEdicao = false;

    public PainelAlunos(ControladorAluno controlador, Runnable onDadosAlterados) {
        this.controlador = controlador;
        this.onDadosAlterados = onDadosAlterados;

        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Topo: Barra de Pesquisa
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        painelBusca.setBackground(Color.WHITE);
        painelBusca.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        txtBusca = new JTextField(28);
        JButton btnBuscar = new JButton("Pesquisar");
        JButton btnLimparBusca = new JButton("Limpar");

        btnBuscar.addActionListener(e -> atualizarTabela());
        txtBusca.addActionListener(e -> atualizarTabela());
        btnLimparBusca.addActionListener(e -> {
            txtBusca.setText("");
            atualizarTabela();
        });

        painelBusca.add(new JLabel("Filtrar (ID, Nome ou Turma):"));
        painelBusca.add(txtBusca);
        painelBusca.add(btnBuscar);
        painelBusca.add(btnLimparBusca);

        // Centro: Tabela
        tableModel = new AlunoTableModel();
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioDaSelecao();
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);

        // Direita: Formulário de Cadastro / Edição
        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBackground(Color.WHITE);
        painelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cadastro / Edição de Aluno"),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = new JTextField(16);
        txtNome = new JTextField(16);
        txtNascimento = new JTextField(16);
        txtNota = new JTextField(16);
        txtFrequencia = new JTextField(16);
        txtTurma = new JTextField(16);

        adicionarCampo(painelForm, gbc, 0, "ID (positivo):", txtId);
        adicionarCampo(painelForm, gbc, 1, "Nome:", txtNome);
        adicionarCampo(painelForm, gbc, 2, "Nascimento (AAAA-MM-DD):", txtNascimento);
        adicionarCampo(painelForm, gbc, 3, "Nota (0.0 a 10.0):", txtNota);
        adicionarCampo(painelForm, gbc, 4, "Frequência (0 a 100%):", txtFrequencia);
        adicionarCampo(painelForm, gbc, 5, "Turma (ex.: Turma-1):", txtTurma);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        painelBotoes.setOpaque(false);

        btnSalvar = new JButton("Cadastrar");
        btnSalvar.setBackground(new Color(37, 99, 235));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> salvarAluno());

        btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 38, 38));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setEnabled(false);
        btnExcluir.addActionListener(e -> excluirAlunoSelecionado());

        JButton btnNovo = new JButton("Limpar");
        btnNovo.addActionListener(e -> limparFormulario());

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnNovo);

        gbc.gridx = 0;
        gbc.gridy = 12;
        painelForm.add(painelBotoes, gbc);

        add(painelBusca, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);
        add(painelForm, BorderLayout.EAST);
    }

    private void adicionarCampo(JPanel painel, GridBagConstraints gbc, int linha, String rotulo, JTextField campo) {
        gbc.gridx = 0;
        gbc.gridy = linha * 2;
        painel.add(new JLabel(rotulo), gbc);
        gbc.gridy = linha * 2 + 1;
        painel.add(campo, gbc);
    }

    public void atualizarTabela() {
        List<Aluno> lista = controlador.listarAlunos(txtBusca.getText());
        tableModel.setDados(lista);
    }

    private void preencherFormularioDaSelecao() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        Aluno a = tableModel.getAlunoAt(linha);
        modoEdicao = true;
        txtId.setText(String.valueOf(a.getId()));
        txtId.setEditable(false);
        txtNome.setText(a.getNome());
        txtNascimento.setText(a.getDataNascimento().toString());
        txtNota.setText(String.format(Locale.US, "%.2f", a.getNota()));
        txtFrequencia.setText(String.valueOf(a.getFrequencia()));
        txtTurma.setText(a.getTurma());
        btnSalvar.setText("Salvar Alterações");
        btnExcluir.setEnabled(true);
    }

    private void salvarAluno() {
        try {
            controlador.salvarOuAtualizar(
                    txtId.getText(),
                    txtNome.getText(),
                    txtNascimento.getText(),
                    txtNota.getText(),
                    txtFrequencia.getText(),
                    txtTurma.getText(),
                    modoEdicao
            );
            atualizarTabela();
            limparFormulario();
            if (onDadosAlterados != null) onDadosAlterados.run();
            JOptionPane.showMessageDialog(this, "Aluno salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao salvar aluno: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirAlunoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return;

        Aluno a = tableModel.getAlunoAt(linha);
        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir o aluno '" + a.getNome() + "' (ID " + a.getId() + ")?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao == JOptionPane.YES_OPTION) {
            if (controlador.excluir(a.getId())) {
                atualizarTabela();
                limparFormulario();
                if (onDadosAlterados != null) onDadosAlterados.run();
            }
        }
    }

    private void limparFormulario() {
        modoEdicao = false;
        tabela.clearSelection();
        txtId.setText("");
        txtId.setEditable(true);
        txtNome.setText("");
        txtNascimento.setText("");
        txtNota.setText("");
        txtFrequencia.setText("");
        txtTurma.setText("");
        btnSalvar.setText("Cadastrar");
        btnExcluir.setEnabled(false);
    }

    private static class AlunoTableModel extends AbstractTableModel {
        private final String[] colunas = {"ID", "Nome", "Nascimento", "Nota", "Frequência", "Turma"};
        private List<Aluno> dados = new ArrayList<>();

        public void setDados(List<Aluno> novosDados) {
            this.dados = novosDados;
            fireTableDataChanged();
        }

        public Aluno getAlunoAt(int row) {
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
            Aluno a = dados.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> a.getId();
                case 1 -> a.getNome();
                case 2 -> a.getDataNascimento();
                case 3 -> String.format(Locale.US, "%.2f", a.getNota());
                case 4 -> a.getFrequencia() + "%";
                case 5 -> a.getTurma();
                default -> "";
            };
        }
    }
}