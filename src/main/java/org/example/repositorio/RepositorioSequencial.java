package org.example.repositorio;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorCsv;
import org.example.leitura.LeitorSequencial;
import org.example.leitura.ResultadoLeitura;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositorioSequencial implements RepositorioEscolar {

    private final Map<Integer, Pessoa> dados = new HashMap<>();
    private final BoletimService boletimService = new BoletimService();
    private final Formatador<Pessoa> formatadorCsv = new FormatadorCsv();

    @Override
    public boolean adicionar(Pessoa pessoa) {
        if (pessoa == null) {
            return false;
        }
        return dados.putIfAbsent(pessoa.getId(), pessoa) == null;
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
    public boolean atualizarNotaEFrequenciaAtomico(int id, double novaNota, int novaFrequencia) {
        Pessoa existente = dados.get(id);
        if (!(existente instanceof Aluno alunoAtual)) {
            return false;
        }
        dados.put(id, new Aluno(
                alunoAtual.getId(),
                alunoAtual.getNome(),
                alunoAtual.getDataNascimento(),
                novaNota,
                novaFrequencia,
                alunoAtual.getTurma()
        ));
        return true;
    }

    @Override
    public String consultarSituacaoAlunoAtomico(int id) {
        Pessoa p = dados.get(id);
        if (!(p instanceof Aluno aluno)) {
            return "NAO_ENCONTRADO_OU_NAO_ALUNO";
        }
        return boletimService.verificarSituacao(aluno);
    }

    @Override
    public boolean upsertAtomico(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula.");
        }
        return dados.put(pessoa.getId(), pessoa) == null;
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
        try (BufferedWriter bw = new BufferedWriter(
                Files.newBufferedWriter(Path.of(caminhoArquivo), StandardCharsets.UTF_8), 64 * 1024)) {
            for (Pessoa p : dados.values()) {
                bw.write(formatadorCsv.formatar(p));
                bw.newLine();
            }
        }
    }
}