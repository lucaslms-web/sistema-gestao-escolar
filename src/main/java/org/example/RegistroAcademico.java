package org.example;

import java.time.LocalDate;

public abstract class RegistroAcademico {
    private int id;
    private LocalDate dataRegistro;
    private TipoRegistro tipo;

    public RegistroAcademico(int id, LocalDate dataRegistro, TipoRegistro tipo) {

        if (id <= 0) {
            throw new IllegalArgumentException("O ID do registro deve ser positivo.");
        }
        if (dataRegistro == null) {
            throw new IllegalArgumentException("A data do registro não pode ser nula.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo do registro não pode ser nulo.");
        }

        this.id = id;
        this.dataRegistro = dataRegistro;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public TipoRegistro getTipo() {
        return tipo;
    }

    public abstract String gerarResumo();
}
