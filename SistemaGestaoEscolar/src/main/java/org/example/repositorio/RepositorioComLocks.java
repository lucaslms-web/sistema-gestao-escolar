package org.example.repositorio;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorCsv;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.ResultadoLeitura;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RepositorioComLocks implements Repositorio<Pessoa> {

    private final Map<Integer, Pessoa> dados = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final BoletimService boletimService = new BoletimService();
    private final Formatador<Pessoa> formatadorCsv = new FormatadorCsv();

    @Override
    public boolean adicionar(Pessoa pessoa) {
        if (pessoa == null) {
            return false;
        }
        writeLock.lock();
        try {
            if (dados.containsKey(pessoa.getId())) {
                return false;
            }
            dados.put(pessoa.getId(), pessoa);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remover(int id) {
        writeLock.lock();
        try {
            return dados.remove(id) != null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Pessoa buscarPorId(int id) {
        readLock.lock();
        try {
            return dados.get(id);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Pessoa> listarTodos() {
        readLock.lock();
        try {
            return new ArrayList<>(dados.values());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int tamanho() {
        readLock.lock();
        try {
            return dados.size();
        } finally {
            readLock.unlock();
        }
    }

    public boolean atualizarNotaEFrequenciaAtomico(int id, double novaNota, int novaFrequencia) {
        writeLock.lock();
        try {
            Pessoa existente = dados.get(id);
            if (!(existente instanceof Aluno alunoAtual)) {
                return false;
            }
            Aluno alunoAtualizado = new Aluno(
                    alunoAtual.getId(),
                    alunoAtual.getNome(),
                    alunoAtual.getDataNascimento(),
                    novaNota,
                    novaFrequencia,
                    alunoAtual.getTurma()
            );
            dados.put(id, alunoAtualizado);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public String consultarSituacaoAlunoAtomico(int id) {
        readLock.lock();
        try {
            Pessoa p = dados.get(id);
            if (!(p instanceof Aluno aluno)) {
                return "NAO_ENCONTRADO_OU_NAO_ALUNO";
            }
            return boletimService.verificarSituacao(aluno);
        } finally {
            readLock.unlock();
        }
    }

    public boolean upsertAtomico(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula.");
        }
        writeLock.lock();
        try {
            boolean eraNovo = !dados.containsKey(pessoa.getId());
            dados.put(pessoa.getId(), pessoa);
            return eraNovo;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public ResultadoLeitura carregarDeArquivo(String caminhoArquivo) {
        return carregarDeArquivoConcorrente(caminhoArquivo, 4);
    }

    public ResultadoLeitura carregarDeArquivoConcorrente(String caminhoArquivo, int numThreadsLeitura) {
        LeitorConcorrente leitor = new LeitorConcorrente();
        ResultadoLeitura resultado = leitor.lerArquivoConcorrente(caminhoArquivo, numThreadsLeitura);

        writeLock.lock();
        try {
            dados.clear();
            for (Pessoa p : resultado.getRegistros()) {
                dados.put(p.getId(), p);
            }
        } finally {
            writeLock.unlock();
        }
        return resultado;
    }

    @Override
    public void salvarEmArquivo(String caminhoArquivo) throws IOException {
        List<Pessoa> snapshot = listarTodos();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            for (Pessoa p : snapshot) {
                bw.write(formatadorCsv.formatar(p));
                bw.newLine();
            }
        }
    }
}