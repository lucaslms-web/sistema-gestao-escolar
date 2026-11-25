package Classes;

import java.time.LocalDate;

public class Pessoa {
    private int id;
    private String nome;
    private LocalDate dataNascimento;

    public Pessoa(int id, String nome, LocalDate dataNascimento) {

        if (id <= 0) throw new IllegalArgumentException("ID deve ser positivo");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome invalido");

        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
    }
}
