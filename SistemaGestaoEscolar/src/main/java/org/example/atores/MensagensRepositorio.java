package org.example.atores;

import org.example.dominio.Pessoa;
import org.apache.pekko.actor.typed.ActorRef;

import java.util.List;
import java.util.Optional;

public final class MensagensRepositorio {

    private MensagensRepositorio() {
    }

    public static final class ComandoAdicionar implements ComandoRepositorio {
        private final Pessoa pessoa;
        private final ActorRef<Boolean> replyTo;

        public ComandoAdicionar(Pessoa pessoa, ActorRef<Boolean> replyTo) {
            this.pessoa = pessoa;
            this.replyTo = replyTo;
        }

        public Pessoa getPessoa() { return pessoa; }
        public ActorRef<Boolean> getReplyTo() { return replyTo; }
    }

    public static final class ComandoRemover implements ComandoRepositorio {
        private final int id;
        private final ActorRef<Boolean> replyTo;

        public ComandoRemover(int id, ActorRef<Boolean> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }

        public int getId() { return id; }
        public ActorRef<Boolean> getReplyTo() { return replyTo; }
    }

    public static final class ComandoBuscar implements ComandoRepositorio {
        private final int id;
        private final ActorRef<Optional<Pessoa>> replyTo;

        public ComandoBuscar(int id, ActorRef<Optional<Pessoa>> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }

        public int getId() { return id; }
        public ActorRef<Optional<Pessoa>> getReplyTo() { return replyTo; }
    }

    public static final class ComandoListarTodos implements ComandoRepositorio {
        private final ActorRef<List<Pessoa>> replyTo;

        public ComandoListarTodos(ActorRef<List<Pessoa>> replyTo) {
            this.replyTo = replyTo;
        }

        public ActorRef<List<Pessoa>> getReplyTo() { return replyTo; }
    }

    public static final class ComandoTamanho implements ComandoRepositorio {
        private final ActorRef<Integer> replyTo;

        public ComandoTamanho(ActorRef<Integer> replyTo) {
            this.replyTo = replyTo;
        }

        public ActorRef<Integer> getReplyTo() { return replyTo; }
    }

    public static final class ComandoAtualizarNotaEFrequencia implements ComandoRepositorio {
        private final int id;
        private final double novaNota;
        private final int novaFrequencia;
        private final ActorRef<Boolean> replyTo;

        public ComandoAtualizarNotaEFrequencia(int id, double novaNota, int novaFrequencia, ActorRef<Boolean> replyTo) {
            this.id = id;
            this.novaNota = novaNota;
            this.novaFrequencia = novaFrequencia;
            this.replyTo = replyTo;
        }

        public int getId() { return id; }
        public double getNovaNota() { return novaNota; }
        public int getNovaFrequencia() { return novaFrequencia; }
        public ActorRef<Boolean> getReplyTo() { return replyTo; }
    }

    public static final class ComandoConsultarSituacao implements ComandoRepositorio {
        private final int id;
        private final ActorRef<String> replyTo;

        public ComandoConsultarSituacao(int id, ActorRef<String> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }

        public int getId() { return id; }
        public ActorRef<String> getReplyTo() { return replyTo; }
    }

    public static final class ComandoUpsert implements ComandoRepositorio {
        private final Pessoa pessoa;
        private final ActorRef<Boolean> replyTo;

        public ComandoUpsert(Pessoa pessoa, ActorRef<Boolean> replyTo) {
            this.pessoa = pessoa;
            this.replyTo = replyTo;
        }

        public Pessoa getPessoa() { return pessoa; }
        public ActorRef<Boolean> getReplyTo() { return replyTo; }
    }

    public static final class ComandoSalvar implements ComandoRepositorio {
        private final String caminhoArquivo;
        private final ActorRef<RespostaArquivo> replyTo;

        public ComandoSalvar(String caminhoArquivo, ActorRef<RespostaArquivo> replyTo) {
            this.caminhoArquivo = caminhoArquivo;
            this.replyTo = replyTo;
        }

        public String getCaminhoArquivo() { return caminhoArquivo; }
        public ActorRef<RespostaArquivo> getReplyTo() { return replyTo; }
    }

    public static final class ComandoSubstituirTodos implements ComandoRepositorio {
        private final List<Pessoa> novosRegistros;
        private final ActorRef<Integer> replyTo;

        public ComandoSubstituirTodos(List<Pessoa> novosRegistros, ActorRef<Integer> replyTo) {
            this.novosRegistros = List.copyOf(novosRegistros);
            this.replyTo = replyTo;
        }

        public List<Pessoa> getNovosRegistros() { return novosRegistros; }
        public ActorRef<Integer> getReplyTo() { return replyTo; }
    }

    public static final class RespostaArquivo {
        private final boolean sucesso;
        private final String erro;

        public RespostaArquivo(boolean sucesso, String erro) {
            this.sucesso = sucesso;
            this.erro = erro;
        }

        public boolean isSucesso() { return sucesso; }
        public String getErro() { return erro; }
    }
}