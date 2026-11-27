package org.example.formatacao;

public interface Formatador<T> {
    String formatar(T entidade);
    String getNomeFormato();
}