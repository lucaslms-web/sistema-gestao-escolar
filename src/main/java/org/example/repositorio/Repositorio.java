package org.example.repositorio;

import org.example.leitura.ResultadoLeitura;

import java.io.IOException;
import java.util.List;

public interface Repositorio<T> {
    boolean adicionar(T entidade);
    boolean remover(int id);
    T buscarPorId(int id);
    List<T> listarTodos();
    int tamanho();
    ResultadoLeitura carregarDeArquivo(String caminhoArquivo);
    void salvarEmArquivo(String caminhoArquivo) throws IOException;
}