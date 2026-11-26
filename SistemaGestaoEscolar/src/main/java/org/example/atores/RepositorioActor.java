package org.example.atores;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.example.atores.MensagensRepositorio.*;
import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorCsv;
import org.example.repositorio.BoletimService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RepositorioActor extends AbstractBehavior<ComandoRepositorio> {

    private final Map<Integer, Pessoa> dados = new HashMap<>();
    private final BoletimService boletimService = new BoletimService();
    private final Formatador<Pessoa> formatadorCsv = new FormatadorCsv();

    public static Behavior<ComandoRepositorio> create() {
        return Behaviors.setup(RepositorioActor::new);
    }

    private RepositorioActor(ActorContext<ComandoRepositorio> context) {
        super(context);
    }

    @Override
    public Receive<ComandoRepositorio> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComandoAdicionar.class, this::onAdicionar)
                .onMessage(ComandoRemover.class, this::onRemover)
                .onMessage(ComandoBuscar.class, this::onBuscar)
                .onMessage(ComandoListarTodos.class, this::onListarTodos)
                .onMessage(ComandoTamanho.class, this::onTamanho)
                .onMessage(ComandoAtualizarNotaEFrequencia.class, this::onAtualizarNotaEFrequencia)
                .onMessage(ComandoConsultarSituacao.class, this::onConsultarSituacao)
                .onMessage(ComandoUpsert.class, this::onUpsert)
                .onMessage(ComandoSalvar.class, this::onSalvar)
                .onMessage(ComandoSubstituirTodos.class, this::onSubstituirTodos)
                .build();
    }

    private Behavior<ComandoRepositorio> onAdicionar(ComandoAdicionar cmd) {
        Pessoa p = cmd.getPessoa();
        if (p == null) {
            cmd.getReplyTo().tell(false);
            return this;
        }
        cmd.getReplyTo().tell(dados.putIfAbsent(p.getId(), p) == null);
        return this;
    }

    private Behavior<ComandoRepositorio> onRemover(ComandoRemover cmd) {
        cmd.getReplyTo().tell(dados.remove(cmd.getId()) != null);
        return this;
    }

    private Behavior<ComandoRepositorio> onBuscar(ComandoBuscar cmd) {
        cmd.getReplyTo().tell(Optional.ofNullable(dados.get(cmd.getId())));
        return this;
    }

    private Behavior<ComandoRepositorio> onListarTodos(ComandoListarTodos cmd) {
        cmd.getReplyTo().tell(List.copyOf(dados.values()));
        return this;
    }

    private Behavior<ComandoRepositorio> onTamanho(ComandoTamanho cmd) {
        cmd.getReplyTo().tell(dados.size());
        return this;
    }

    private Behavior<ComandoRepositorio> onAtualizarNotaEFrequencia(ComandoAtualizarNotaEFrequencia cmd) {
        Pessoa existente = dados.get(cmd.getId());
        if (!(existente instanceof Aluno alunoAtual)) {
            cmd.getReplyTo().tell(false);
            return this;
        }
        dados.put(cmd.getId(), new Aluno(
                alunoAtual.getId(),
                alunoAtual.getNome(),
                alunoAtual.getDataNascimento(),
                cmd.getNovaNota(),
                cmd.getNovaFrequencia(),
                alunoAtual.getTurma()
        ));
        cmd.getReplyTo().tell(true);
        return this;
    }

    private Behavior<ComandoRepositorio> onConsultarSituacao(ComandoConsultarSituacao cmd) {
        Pessoa p = dados.get(cmd.getId());
        if (!(p instanceof Aluno aluno)) {
            cmd.getReplyTo().tell("NAO_ENCONTRADO_OU_NAO_ALUNO");
            return this;
        }
        cmd.getReplyTo().tell(boletimService.verificarSituacao(aluno));
        return this;
    }

    private Behavior<ComandoRepositorio> onUpsert(ComandoUpsert cmd) {
        Pessoa p = cmd.getPessoa();
        if (p == null) {
            cmd.getReplyTo().tell(false);
            return this;
        }
        cmd.getReplyTo().tell(dados.put(p.getId(), p) == null);
        return this;
    }

    private Behavior<ComandoRepositorio> onSubstituirTodos(ComandoSubstituirTodos cmd) {
        dados.clear();
        for (Pessoa p : cmd.getNovosRegistros()) {
            dados.put(p.getId(), p);
        }
        cmd.getReplyTo().tell(dados.size());
        return this;
    }

    private Behavior<ComandoRepositorio> onSalvar(ComandoSalvar cmd) {
        // Snapshot imutável rápido no ator; I/O em disco ocorre fora da thread do dispatcher
        List<Pessoa> snapshot = List.copyOf(dados.values());
        Path destino = Path.of(cmd.getCaminhoArquivo());

        CompletableFuture.runAsync(() -> {
            try (BufferedWriter bw = new BufferedWriter(
                    Files.newBufferedWriter(destino, StandardCharsets.UTF_8), 64 * 1024)) {
                for (Pessoa p : snapshot) {
                    bw.write(formatadorCsv.formatar(p));
                    bw.newLine();
                }
                cmd.getReplyTo().tell(new RespostaArquivo(true, null));
            } catch (IOException e) {
                cmd.getReplyTo().tell(new RespostaArquivo(false, e.getMessage()));
            }
        });
        return this;
    }
}