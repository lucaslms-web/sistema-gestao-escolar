package org.example.interfacegrafica.controlador;

import org.example.dominio.Pessoa;
import org.example.leitura.ResultadoLeitura;
import org.example.repositorio.RepositorioComAtores;
import org.example.repositorio.RepositorioComLocks;
import org.example.repositorio.RepositorioEscolar;
import org.example.repositorio.RepositorioSequencial;

import java.io.IOException;
import java.util.List;

public class GerenciadorRepositorio implements AutoCloseable {

    public enum TipoMotor {
        LOCKS("Locks (ReentrantReadWriteLock)"),
        ATORES("Atores (Apache Pekko Typed)"),
        SEQUENCIAL("Sequencial (HashMap)");

        private final String descricao;

        TipoMotor(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private RepositorioEscolar repositorioAtual;
    private TipoMotor motorAtual;
    private String arquivoCarregadoAtual;

    public GerenciadorRepositorio() {
        this.motorAtual = TipoMotor.LOCKS;
        this.repositorioAtual = new RepositorioComLocks();
        this.arquivoCarregadoAtual = "Nenhum arquivo anexado (repositório vazio)";
    }

    public synchronized RepositorioEscolar getRepositorio() {
        return repositorioAtual;
    }

    public synchronized TipoMotor getMotorAtual() {
        return motorAtual;
    }

    public synchronized String getArquivoCarregadoAtual() {
        return arquivoCarregadoAtual;
    }

    public synchronized void alternarMotor(TipoMotor novoMotor) {
        if (novoMotor == null || novoMotor == this.motorAtual) {
            return;
        }

        List<Pessoa> snapshot = this.repositorioAtual.listarTodos();
        RepositorioEscolar novoRepositorio = criarInstancia(novoMotor);

        for (Pessoa p : snapshot) {
            novoRepositorio.adicionar(p);
        }

        encerrarSilenciosamente(this.repositorioAtual);
        this.repositorioAtual = novoRepositorio;
        this.motorAtual = novoMotor;
    }

    public synchronized ResultadoLeitura carregarCsv(String caminhoArquivo) {
        ResultadoLeitura resultado;
        if (repositorioAtual instanceof RepositorioComLocks locks) {
            resultado = locks.carregarDeArquivoConcorrente(caminhoArquivo, 4);
        } else if (repositorioAtual instanceof RepositorioComAtores atores) {
            resultado = atores.carregarDeArquivoConcorrente(caminhoArquivo, 4, false);
        } else {
            resultado = repositorioAtual.carregarDeArquivo(caminhoArquivo);
        }
        this.arquivoCarregadoAtual = caminhoArquivo;
        return resultado;
    }

    public synchronized void salvarCsv(String caminhoArquivo) throws IOException {
        repositorioAtual.salvarEmArquivo(caminhoArquivo);
        this.arquivoCarregadoAtual = caminhoArquivo;
    }

    private RepositorioEscolar criarInstancia(TipoMotor tipo) {
        return switch (tipo) {
            case LOCKS -> new RepositorioComLocks();
            case ATORES -> new RepositorioComAtores();
            case SEQUENCIAL -> new RepositorioSequencial();
        };
    }

    private void encerrarSilenciosamente(RepositorioEscolar repo) {
        if (repo instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public synchronized void close() {
        encerrarSilenciosamente(repositorioAtual);
    }
}