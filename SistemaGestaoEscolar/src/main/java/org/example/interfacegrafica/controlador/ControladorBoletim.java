package org.example.interfacegrafica.controlador;

import org.example.dominio.Aluno;
import org.example.dominio.Pessoa;
import org.example.repositorio.BoletimService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ControladorBoletim {

    public static class ResumoBoletim {
        public final Aluno aluno;
        public final String situacao;

        public ResumoBoletim(Aluno aluno, String situacao) {
            this.aluno = aluno;
            this.situacao = situacao;
        }
    }

    private final GerenciadorRepositorio gerenciador;
    private final BoletimService boletimService;

    public ControladorBoletim(GerenciadorRepositorio gerenciador) {
        this.gerenciador = gerenciador;
        this.boletimService = new BoletimService();
    }

    public ResumoBoletim consultarPorId(int id) {
        Pessoa p = gerenciador.getRepositorio().buscarPorId(id);
        if (!(p instanceof Aluno aluno)) {
            return null;
        }
        String situacao = gerenciador.getRepositorio().consultarSituacaoAlunoAtomico(id);
        return new ResumoBoletim(aluno, situacao);
    }

    public List<ResumoBoletim> listarBoletins(String filtro) {
        String termo = (filtro == null) ? "" : filtro.trim().toLowerCase(Locale.ROOT);
        return gerenciador.getRepositorio().listarTodos().stream()
                .filter(p -> p instanceof Aluno)
                .map(p -> (Aluno) p)
                .filter(a -> termo.isEmpty()
                        || String.valueOf(a.getId()).contains(termo)
                        || a.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || a.getTurma().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparingInt(Aluno::getId))
                .map(a -> new ResumoBoletim(a, boletimService.verificarSituacao(a)))
                .collect(Collectors.toList());
    }

    public double calcularMediaGeral() {
        return boletimService.calcularMediaNotas(gerenciador.getRepositorio().listarTodos());
    }
}