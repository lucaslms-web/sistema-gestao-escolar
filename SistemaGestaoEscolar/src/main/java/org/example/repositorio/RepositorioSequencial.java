package org.example.repositorio;

import org.example.dominio.Pessoa;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorCsv;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositorioSequencial implements Repositorio<Pessoa> {

    private final Map<Integer, Pessoa> dados = new HashMap<>();
    private final Formatador<Pessoa> formatadorCsv = new FormatadorCsv();

    @Override
    public boolean adicionar(Pessoa pessoa) {
        if (pessoa == null || dados.containsKey(pessoa.getId())) {
            return false;
        }
        dados.put(pessoa.getId(), pessoa);
        return true;
    }

    @Override
    public boolean remover(int id) {
        return dados.remove(id) != null;
    }

    @Override
    public Pessoa buscarPorId(int id) {
        return dados.get(id);
    }

    @Override
    public List<Pessoa> listarTodos() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public int tamanho() {
        return dados.size();
    }

    @Override
    public ResultadoLeitura carregarDeArquivo(String caminhoArquivo) {
        LeitorSequencial leitor = new LeitorSequencial();
        ResultadoLeitura resultado = leitor.lerArquivo(caminhoArquivo);

        dados.clear();
        for (Pessoa p : resultado.getRegistros()) {
            dados.put(p.getId(), p);
        }
        return resultado;
    }

    @Override
    public void salvarEmArquivo(String caminhoArquivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            for (Pessoa p : dados.values()) {
                bw.write(formatadorCsv.formatar(p));
                bw.newLine();
            }
        }
    }
}