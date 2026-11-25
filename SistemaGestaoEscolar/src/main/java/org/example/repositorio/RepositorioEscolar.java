package org.example.repositorio;

import org.example.dominio.Pessoa;

public interface RepositorioEscolar extends Repositorio<Pessoa> {
    boolean atualizarNotaEFrequenciaAtomico(int id, double novaNota, int novaFrequencia);
    String consultarSituacaoAlunoAtomico(int id);
    boolean upsertAtomico(Pessoa pessoa);
}