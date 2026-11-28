package org.example;

import org.example.interfacegrafica.JanelaPrincipal;
import org.example.interfacegrafica.controlador.GerenciadorRepositorio;
import org.example.repositorio.RepositorioComLocks;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            MenuConsole menu = new MenuConsole(new RepositorioComLocks());
            menu.iniciar();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            configurarAparenciaSistema();
            GerenciadorRepositorio gerenciador = new GerenciadorRepositorio();
            JanelaPrincipal janela = new JanelaPrincipal(gerenciador);
            janela.setVisible(true);
        });
    }

    private static void configurarAparenciaSistema() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}