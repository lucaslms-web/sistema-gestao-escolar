package org.example.interfacegrafica.componentes;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MenuLateral extends JPanel {

    public static final String TELA_INICIO = "INICIO";
    public static final String TELA_ALUNOS = "ALUNOS";
    public static final String TELA_PROFESSORES = "PROFESSORES";
    public static final String TELA_BOLETINS = "BOLETINS";
    public static final String TELA_BENCHMARKS = "BENCHMARKS";

    private static final Color COR_FUNDO = new Color(30, 41, 59);
    private static final Color COR_BOTAO_ATIVO = new Color(51, 65, 85);
    private static final Color COR_TEXTO = new Color(241, 245, 249);
    private static final Color COR_SUBTEXTO = new Color(148, 163, 184);

    private final Map<String, JButton> botoes = new LinkedHashMap<>();
    private final Consumer<String> onSelecionarTela;

    public MenuLateral(Consumer<String> onSelecionarTela) {
        this.onSelecionarTela = onSelecionarTela;

        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);

        JLabel lblSistema = new JLabel("Gestão Escolar");
        lblSistema.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblSistema.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("UFF • POO & Concorrência");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitulo.setForeground(COR_SUBTEXTO);

        cabecalho.add(lblSistema);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(lblSubtitulo);
        cabecalho.add(Box.createVerticalStrut(24));

        JPanel listaMenu = new JPanel();
        listaMenu.setLayout(new BoxLayout(listaMenu, BoxLayout.Y_AXIS));
        listaMenu.setOpaque(false);

        adicionarItemMenu(listaMenu, TELA_INICIO, "Início");
        adicionarItemMenu(listaMenu, TELA_ALUNOS, "Alunos");
        adicionarItemMenu(listaMenu, TELA_PROFESSORES, "Professores");
        adicionarItemMenu(listaMenu, TELA_BOLETINS, "Boletins");
        listaMenu.add(Box.createVerticalStrut(12));
        adicionarItemMenu(listaMenu, TELA_BENCHMARKS, "Benchmarks");

        JLabel lblRodape = new JLabel("Java 24 • Pekko & Locks");
        lblRodape.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRodape.setForeground(COR_SUBTEXTO);

        add(cabecalho, BorderLayout.NORTH);
        add(listaMenu, BorderLayout.CENTER);
        add(lblRodape, BorderLayout.SOUTH);

        marcarAtivo(TELA_INICIO);
    }

    private void adicionarItemMenu(JPanel container, String chave, String rotulo) {
        JButton btn = new JButton(rotulo);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(190, 40));
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(COR_TEXTO);
        btn.setBackground(COR_FUNDO);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            marcarAtivo(chave);
            if (onSelecionarTela != null) {
                onSelecionarTela.accept(chave);
            }
        });

        botoes.put(chave, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    public void marcarAtivo(String chaveAtiva) {
        for (Map.Entry<String, JButton> entry : botoes.entrySet()) {
            boolean ativo = entry.getKey().equals(chaveAtiva);
            entry.getValue().setBackground(ativo ? COR_BOTAO_ATIVO : COR_FUNDO);
            entry.getValue().setForeground(ativo ? Color.WHITE : COR_TEXTO);
        }
    }
}