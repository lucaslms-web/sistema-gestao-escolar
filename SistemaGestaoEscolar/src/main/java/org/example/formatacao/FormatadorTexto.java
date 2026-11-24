package org.example.formatacao;

import org.example.dominio.Pessoa;

public class FormatadorTexto implements Formatador<Pessoa> {

    @Override
    public String formatar(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula.");
        }
        return pessoa.exibirDetalhes();
    }

    @Override
    public String getNomeFormato() {
        return "Texto Legível (Console)";
    }
}