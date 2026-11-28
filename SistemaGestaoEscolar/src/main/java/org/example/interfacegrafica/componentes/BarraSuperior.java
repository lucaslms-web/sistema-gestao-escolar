package org.example.interfacegrafica.componentes;

import org.example.interfacegrafica.controlador.GerenciadorRepositorio.TipoMotor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

public class BarraSuperior extends JPanel {

    private final JLabel lblTituloSecao;
    private final JLabel lblStatusRegistros;
    private final JComboBox<TipoMotor> comboMotor;
    private final JButton btnCarregarCsv;
    private final JButton btnSalvarCsv;

    public BarraSuperior(
            Consumer<TipoMotor> onTrocarMotor,
            Runnable onCarregarCsv,
            Runnable onSalvarCsv) {

        setLayout(new BorderLayout(15, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        JPanel painelEsquerda = new JPanel(new BorderLayout(0, 2));
        painelEsquerda.setOpaque(false);

        lblTituloSecao = new JLabel("Início — Visão Geral");
        lblTituloSecao.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTituloSecao.setForeground(new Color(30, 41, 59));

        lblStatusRegistros = new JLabel("0 registros em memória | Repositório vazio");
        lblStatusRegistros.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatusRegistros.setForeground(new Color(100, 116, 139));

        painelEsquerda.add(lblTituloSecao, BorderLayout.NORTH);
        painelEsquerda.add(lblStatusRegistros, BorderLayout.SOUTH);

        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        painelDireita.setOpaque(false);

        JLabel lblMotor = new JLabel("Motor:");
        lblMotor.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMotor.setForeground(new Color(51, 65, 85));

        comboMotor = new JComboBox<>(TipoMotor.values());
        comboMotor.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboMotor.addActionListener(e -> {
            TipoMotor selecionado = (TipoMotor) comboMotor.getSelectedItem();
            if (selecionado != null && onTrocarMotor != null) {
                onTrocarMotor.accept(selecionado);
            }
        });

        btnCarregarCsv = criarBotaoAcao("Anexar / Carregar CSV", new Color(37, 99, 235), Color.WHITE);
        btnCarregarCsv.addActionListener(e -> {
            if (onCarregarCsv != null) onCarregarCsv.run();
        });

        btnSalvarCsv = criarBotaoAcao("Exportar CSV", new Color(241, 245, 249), new Color(30, 41, 59));
        btnSalvarCsv.addActionListener(e -> {
            if (onSalvarCsv != null) onSalvarCsv.run();
        });

        painelDireita.add(lblMotor);
        painelDireita.add(comboMotor);
        painelDireita.add(btnCarregarCsv);
        painelDireita.add(btnSalvarCsv);

        add(painelEsquerda, BorderLayout.WEST);
        add(painelDireita, BorderLayout.EAST);
    }

    private JButton criarBotaoAcao(String texto, Color fundo, Color textoCor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(fundo);
        btn.setForeground(textoCor);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void atualizarTitulo(String titulo) {
        lblTituloSecao.setText(titulo);
    }

    public void atualizarStatus(int totalRegistros, String infoArquivo) {
        lblStatusRegistros.setText(totalRegistros + " registro(s) em memória | " + infoArquivo);
    }

    public void setControlesHabilitados(boolean habilitado) {
        comboMotor.setEnabled(habilitado);
        btnCarregarCsv.setEnabled(habilitado);
        btnSalvarCsv.setEnabled(habilitado);
    }
}