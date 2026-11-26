package org.example.formatacao;

import org.example.dominio.Pessoa;

public class FormatadorCsv implements Formatador<Pessoa> {

    @Override
    public String formatar(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula.");
        }
        return pessoa.toCsvLinha();
    }

    @Override
    public String getNomeFormato() {
        return "CSV (separado por ponto e vírgula)";
    }
}