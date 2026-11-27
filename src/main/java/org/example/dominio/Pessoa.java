package org.example.dominio;

import java.time.LocalDate;

public abstract class Pessoa {
    private final int id;
    private String nome;
    private LocalDate dataNascimento;

    public Pessoa(int id, String nome, LocalDate dataNascimento) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID deve ser positivo.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido.");
        }
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.dataNascimento = dataNascimento;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido.");
        }
        this.nome = nome.trim();
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        }
        this.dataNascimento = dataNascimento;
    }

    protected String formatarIdentificacaoBase() {
        return String.format("ID=%d | Nome=%s | Nascimento=%s", id, nome, dataNascimento);
    }

    public abstract String exibirDetalhes();

    public abstract String toCsvLinha();
}