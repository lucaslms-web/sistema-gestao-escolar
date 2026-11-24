package org.example.atores;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.example.repositorio.BoletimService;
import org.example.formatacao.Formatador;
import org.example.formatacao.FormatadorCsv;
import org.example.atores.MensagensRepositorio.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RepositorioActor extends AbstractBehavior<ComandoRepositorio> {

    // Estado interno exclusivo do ator — zero locks necessários
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
        if (p == null || dados.containsKey(p.getId())) {
            cmd.getReplyTo().tell(false);
            return this;
        }
        dados.put(p.getId(), p);
        cmd.getReplyTo().tell(true);
        return this;
    }

    private Behavior<ComandoRepositorio> onRemover(ComandoRemover cmd) {
        boolean removido = dados.remove(cmd.getId()) != null;
        cmd.getReplyTo().tell(removido);
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
        Aluno atualizado = new Aluno(
                alunoAtual.getId(),
                alunoAtual.getNome(),
                alunoAtual.getDataNascimento(),
                cmd.getNovaNota(),
                cmd.getNovaFrequencia(),
                alunoAtual.getTurma()
        );
        dados.put(cmd.getId(), atualizado);
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
        boolean eraNovo = !dados.containsKey(p.getId());
        dados.put(p.getId(), p);
        cmd.getReplyTo().tell(eraNovo);
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(cmd.getCaminhoArquivo()))) {
            for (Pessoa p : dados.values()) {
                bw.write(formatadorCsv.formatar(p));
                bw.newLine();
            }
            cmd.getReplyTo().tell(new RespostaArquivo(true, null));
        } catch (IOException e) {
            cmd.getReplyTo().tell(new RespostaArquivo(false, e.getMessage()));
        }
        return this;
    }
}