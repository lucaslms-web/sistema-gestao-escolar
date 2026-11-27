package org.example.repositorio;

import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.example.atores.ComandoRepositorio;
import org.example.atores.MensagensRepositorio.*;
import org.example.atores.RepositorioActor;
import org.example.dominio.Pessoa;
import org.example.leitura.LeitorConcorrente;
import org.example.leitura.ResultadoLeitura;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RepositorioComAtores implements RepositorioEscolar, AutoCloseable {

    private static final Duration TIMEOUT_PADRAO = Duration.ofSeconds(15);
    private static final Duration TIMEOUT_ARQUIVO = Duration.ofSeconds(60);
    private final ActorSystem<ComandoRepositorio> sistemaAtores;

    public RepositorioComAtores() {
        this.sistemaAtores = ActorSystem.create(RepositorioActor.create(), "SistemaRepositorioEscolar");
    }

    @Override
    public boolean adicionar(Pessoa pessoa) {
        if (pessoa == null) {
            return false;
        }
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoAdicionar(pessoa, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public boolean remover(int id) {
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoRemover(id, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public Pessoa buscarPorId(int id) {
        Optional<Pessoa> resultado = aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoBuscar(id, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
        return resultado.orElse(null);
    }

    @Override
    public List<Pessoa> listarTodos() {
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                ComandoListarTodos::new,
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public int tamanho() {
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                ComandoTamanho::new,
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public boolean atualizarNotaEFrequenciaAtomico(int id, double novaNota, int novaFrequencia) {
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoAtualizarNotaEFrequencia(id, novaNota, novaFrequencia, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public String consultarSituacaoAlunoAtomico(int id) {
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoConsultarSituacao(id, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public boolean upsertAtomico(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa não pode ser nula.");
        }
        return aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoUpsert(pessoa, replyTo),
                TIMEOUT_PADRAO,
                sistemaAtores.scheduler()
        ), TIMEOUT_PADRAO);
    }

    @Override
    public ResultadoLeitura carregarDeArquivo(String caminhoArquivo) {
        return carregarDeArquivoConcorrente(caminhoArquivo, 4, true);
    }

    public ResultadoLeitura carregarDeArquivoConcorrente(String caminhoArquivo, int numThreadsLeitura) {
        return carregarDeArquivoConcorrente(caminhoArquivo, numThreadsLeitura, true);
    }

    public ResultadoLeitura carregarDeArquivoConcorrente(
            String caminhoArquivo, int numThreadsLeitura, boolean exibirLog) {

        LeitorConcorrente leitor = new LeitorConcorrente();
        ResultadoLeitura resultado = leitor.lerArquivoConcorrente(
                caminhoArquivo, numThreadsLeitura, 10_000, exibirLog);

        aguardarResposta(AskPattern.<ComandoRepositorio, Integer>ask(
                sistemaAtores,
                replyTo -> new ComandoSubstituirTodos(resultado.getRegistros(), replyTo),
                TIMEOUT_ARQUIVO,
                sistemaAtores.scheduler()
        ), TIMEOUT_ARQUIVO);
        return resultado;
    }

    @Override
    public void salvarEmArquivo(String caminhoArquivo) throws IOException {
        RespostaArquivo resp = aguardarResposta(AskPattern.ask(
                sistemaAtores,
                replyTo -> new ComandoSalvar(caminhoArquivo, replyTo),
                TIMEOUT_ARQUIVO,
                sistemaAtores.scheduler()
        ), TIMEOUT_ARQUIVO);
        if (!resp.isSucesso()) {
            throw new IOException("Falha ao salvar arquivo via RepositorioActor: " + resp.getErro());
        }
    }

    private <T> T aguardarResposta(CompletionStage<T> stage, Duration timeout) {
        try {
            return stage.toCompletableFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida aguardando resposta do ator.", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Falha ou timeout na comunicação com RepositorioActor.", e);
        }
    }

    @Override
    public void close() {
        sistemaAtores.terminate();
        try {
            sistemaAtores.getWhenTerminated().toCompletableFuture().get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }
}