package com.example.firebaseagenda;

public class Agenda {
    private long ID;
    private String nome;
    private String telefone;

    public Agenda() {
    }

    public Agenda(long ID, String nome, String telefone) {
        this.ID = ID;
        this.nome = nome;
        this.telefone = telefone;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agenda agenda = (Agenda) o;

        if (ID != agenda.ID) return false;
        if (!nome.equals(agenda.nome)) return false;
        return telefone.equals(agenda.telefone);
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + nome.hashCode();
        result = 31 * result + telefone.hashCode();
        return result;
    }
}
