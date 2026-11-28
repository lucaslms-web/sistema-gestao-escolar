package org.example.interfacegrafica.painel;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.dominio.Professor;
import org.example.interfacegrafica.componentes.MenuLateral;
import org.example.interfacegrafica.controlador.ControladorBoletim;
import org.example.interfacegrafica.controlador.GerenciadorRepositorio;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PainelInicio extends JPanel {

    private final GerenciadorRepositorio gerenciador;
    private final ControladorBoletim controladorBoletim;

    private final JLabel lblValorTotal;
    private final JLabel lblValorAlunos;
    private final JLabel lblValorProfessores;
    private final JLabel lblValorMedia;
    private final JLabel lblEstadoVazio;

    public PainelInicio(
            GerenciadorRepositorio gerenciador,
            ControladorBoletim controladorBoletim,
            Consumer<String> onNavegar,
            Runnable onCarregarCsv) {

        this.gerenciador = gerenciador;
        this.controladorBoletim = controladorBoletim;

        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel gridCards = new JPanel(new GridLayout(1, 4, 14, 0));
        gridCards.setOpaque(false);

        lblValorTotal = new JLabel("0");
        lblValorAlunos = new JLabel("0");
        lblValorProfessores = new JLabel("0");
        lblValorMedia = new JLabel("—");

        gridCards.add(criarCard("Total de Registros", lblValorTotal, new Color(37, 99, 235)));
        gridCards.add(criarCard("Alunos Cadastrados", lblValorAlunos, new Color(16, 185, 129)));
        gridCards.add(criarCard("Professores", lblValorProfessores, new Color(139, 92, 246)));
        gridCards.add(criarCard("Média Geral (Alunos)", lblValorMedia, new Color(245, 158, 11)));

        JPanel painelCentral = new JPanel();
        painelCentral.setLayout(new BoxLayout(painelCentral, BoxLayout.Y_AXIS));
        painelCentral.setBackground(Color.WHITE);
        painelCentral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        JLabel lblBemVindo = new JLabel("Painel Administrativo — Sistema de Gestão Escolar");
        lblBemVindo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBemVindo.setForeground(new Color(30, 41, 59));

        lblEstadoVazio = new JLabel();
        lblEstadoVazio.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEstadoVazio.setForeground(new Color(71, 85, 105));

        JPanel barraAtalhos = new JPanel(new GridLayout(1, 5, 10, 0));
        barraAtalhos.setOpaque(false);

        JButton btnAnexar = criarBotaoAtalho("1. Anexar Arquivo CSV", new Color(37, 99, 235), Color.WHITE);
        btnAnexar.addActionListener(e -> onCarregarCsv.run());

        JButton btnAlunos = criarBotaoAtalho("Gerenciar Alunos", new Color(241, 245, 249), new Color(30, 41, 59));
        btnAlunos.addActionListener(e -> onNavegar.accept(MenuLateral.TELA_ALUNOS));

        JButton btnProf = criarBotaoAtalho("Gerenciar Professores", new Color(241, 245, 249), new Color(30, 41, 59));
        btnProf.addActionListener(e -> onNavegar.accept(MenuLateral.TELA_PROFESSORES));

        JButton btnBoletins = criarBotaoAtalho("Consultar Boletins", new Color(241, 245, 249), new Color(30, 41, 59));
        btnBoletins.addActionListener(e -> onNavegar.accept(MenuLateral.TELA_BOLETINS));

        JButton btnBench = criarBotaoAtalho("Executar Benchmarks", new Color(15, 23, 42), Color.WHITE);
        btnBench.addActionListener(e -> onNavegar.accept(MenuLateral.TELA_BENCHMARKS));

        barraAtalhos.add(btnAnexar);
        barraAtalhos.add(btnAlunos);
        barraAtalhos.add(btnProf);
        barraAtalhos.add(btnBoletins);
        barraAtalhos.add(btnBench);

        painelCentral.add(lblBemVindo);
        painelCentral.add(Box.createVerticalStrut(10));
        painelCentral.add(lblEstadoVazio);
        painelCentral.add(Box.createVerticalStrut(22));
        painelCentral.add(barraAtalhos);

        add(gridCards, BorderLayout.NORTH);
        add(painelCentral, BorderLayout.CENTER);

        atualizarDados();
    }

    private JPanel criarCard(String titulo, JLabel lblValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 1, 1, 1, corDestaque),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(100, 116, 139));

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblValor.setForeground(new Color(15, 23, 42));

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JButton criarBotaoAtalho(String texto, Color fundo, Color corTexto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(fundo);
        btn.setForeground(corTexto);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void atualizarDados() {
        List<Pessoa> todos = gerenciador.getRepositorio().listarTodos();
        long qtdAlunos = todos.stream().filter(p -> p instanceof Aluno).count();
        long qtdProfessores = todos.stream().filter(p -> p instanceof Professor).count();

        lblValorTotal.setText(String.valueOf(todos.size()));
        lblValorAlunos.setText(String.valueOf(qtdAlunos));
        lblValorProfessores.setText(String.valueOf(qtdProfessores));

        if (qtdAlunos == 0) {
            lblValorMedia.setText("—");
        } else {
            lblValorMedia.setText(String.format(Locale.US, "%.2f", controladorBoletim.calcularMediaGeral()));
        }

        if (todos.isEmpty()) {
            lblEstadoVazio.setText("<html>O repositório está <b>vazio</b> no momento. Clique em <b>Anexar Arquivo CSV</b> na barra superior (ou no botão abaixo) para importar registros, ou cadastre alunos e professores manualmente.</html>");
        } else {
            lblEstadoVazio.setText(String.format(
                    "<html>Motor ativo: <b>%s</b>. Arquivo atual: <b>%s</b>. Use os atalhos abaixo ou o menu lateral para navegar entre os módulos e rodar benchmarks.</html>",
                    gerenciador.getMotorAtual(),
                    gerenciador.getArquivoCarregadoAtual()
            ));
        }
    }
}