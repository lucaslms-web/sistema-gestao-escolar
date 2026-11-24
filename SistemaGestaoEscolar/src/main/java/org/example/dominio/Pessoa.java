package org.example.dominio;

import java.time.LocalDate;

public abstract class Pessoa {
    private int id;
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
        this.nome = nome;
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

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID deve ser positivo.");
        }
        this.id = id;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido.");
        }
        this.nome = nome;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento não pode ser nula.");
        }
        this.dataNascimento = dataNascimento;
    }

    /**
     * Método protegido acessível apenas pela própria classe e suas subclasses (Aluno e Professor).
     */
    protected String formatarIdentificacaoBase() {
        return String.format("ID=%d | Nome=%s | Nascimento=%s", id, nome, dataNascimento);
    }

    /**
     * Contrato polimórfico sobrescrito por cada subclasse para exibição formatada.
     */
    public abstract String exibirDetalhes();

    /**
     * Contrato polimórfico sobrescrito por cada subclasse para serialização em CSV.
     */
    public abstract String toCsvLinha();
}