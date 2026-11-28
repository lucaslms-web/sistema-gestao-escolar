package org.example.interfacegrafica.painel;

import org.example.dominio.Professor;
import org.example.interfacegrafica.controlador.ControladorProfessor;

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

public class PainelProfessores extends JPanel {

    private final ControladorProfessor controlador;
    private final Runnable onDadosAlterados;

    private final JTextField txtBusca;
    private final JTable tabela;
    private final ProfessorTableModel tableModel;

    private final JTextField txtId;
    private final JTextField txtNome;
    private final JTextField txtNascimento;
    private final JTextField txtSalario;
    private final JTextField txtDisciplinas;
    private final JButton btnSalvar;
    private final JButton btnExcluir;

    private boolean modoEdicao = false;

    public PainelProfessores(ControladorProfessor controlador, Runnable onDadosAlterados) {
        this.controlador = controlador;
        this.onDadosAlterados = onDadosAlterados;

        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

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

        painelBusca.add(new JLabel("Filtrar (ID, Nome ou Disciplina):"));
        painelBusca.add(txtBusca);
        painelBusca.add(btnBuscar);
        painelBusca.add(btnLimparBusca);

        tableModel = new ProfessorTableModel();
        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioDaSelecao();
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);

        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBackground(Color.WHITE);
        painelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cadastro / Edição de Professor"),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtId = new JTextField(16);
        txtNome = new JTextField(16);
        txtNascimento = new JTextField(16);
        txtSalario = new JTextField(16);
        txtDisciplinas = new JTextField(16);

        adicionarCampo(painelForm, gbc, 0, "ID (positivo):", txtId);
        adicionarCampo(painelForm, gbc, 1, "Nome:", txtNome);
        adicionarCampo(painelForm, gbc, 2, "Nascimento (AAAA-MM-DD):", txtNascimento);
        adicionarCampo(painelForm, gbc, 3, "Salário (R$):", txtSalario);
        adicionarCampo(painelForm, gbc, 4, "Disciplinas (separadas por vírgula):", txtDisciplinas);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        painelBotoes.setOpaque(false);

        btnSalvar = new JButton("Cadastrar");
        btnSalvar.setBackground(new Color(37, 99, 235));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> salvarProfessor());

        btnExcluir = new JButton("Excluir");
        btnExcluir.setBackground(new Color(220, 38, 38));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setEnabled(false);
        btnExcluir.addActionListener(e -> excluirProfessorSelecionado());

        JButton btnNovo = new JButton("Limpar");
        btnNovo.addActionListener(e -> limparFormulario());

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnNovo);

        gbc.gridx = 0;
        gbc.gridy = 10;
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
        List<Professor> lista = controlador.listarProfessores(txtBusca.getText());
        tableModel.setDados(lista);
    }

    private void preencherFormularioDaSelecao() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return;

        Professor p = tableModel.getProfessorAt(linha);
        modoEdicao = true;
        txtId.setText(String.valueOf(p.getId()));
        txtId.setEditable(false);
        txtNome.setText(p.getNome());
        txtNascimento.setText(p.getDataNascimento().toString());
        txtSalario.setText(String.format(Locale.US, "%.2f", p.getSalario()));
        txtDisciplinas.setText(String.join(", ", p.getDisciplinas()));
        btnSalvar.setText("Salvar Alterações");
        btnExcluir.setEnabled(true);
    }

    private void salvarProfessor() {
        try {
            controlador.salvarOuAtualizar(
                    txtId.getText(),
                    txtNome.getText(),
                    txtNascimento.getText(),
                    txtSalario.getText(),
                    txtDisciplinas.getText(),
                    modoEdicao
            );
            atualizarTabela();
            limparFormulario();
            if (onDadosAlterados != null) onDadosAlterados.run();
            JOptionPane.showMessageDialog(this, "Professor salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Falha ao salvar professor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProfessorSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) return;

        Professor p = tableModel.getProfessorAt(linha);
        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir o professor '" + p.getNome() + "' (ID " + p.getId() + ")?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (opcao == JOptionPane.YES_OPTION) {
            if (controlador.excluir(p.getId())) {
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
        txtSalario.setText("");
        txtDisciplinas.setText("");
        btnSalvar.setText("Cadastrar");
        btnExcluir.setEnabled(false);
    }

    private static class ProfessorTableModel extends AbstractTableModel {
        private final String[] colunas = {"ID", "Nome", "Nascimento", "Salário", "Disciplinas"};
        private List<Professor> dados = new ArrayList<>();

        public void setDados(List<Professor> novosDados) {
            this.dados = novosDados;
            fireTableDataChanged();
        }

        public Professor getProfessorAt(int row) {
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
            Professor p = dados.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getId();
                case 1 -> p.getNome();
                case 2 -> p.getDataNascimento();
                case 3 -> String.format(Locale.US, "R$ %.2f", p.getSalario());
                case 4 -> p.getDisciplinas().isEmpty() ? "Geral" : String.join(", ", p.getDisciplinas());
                default -> "";
            };
        }
    }
}