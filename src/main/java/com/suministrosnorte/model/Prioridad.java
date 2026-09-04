package com.suministrosnorte.model;

public enum Prioridad {

    BAJA,
    MEDIA,
    ALTA,
    URGENTE;

    public int peso() {
        return switch (this) {
            case URGENTE -> 1;
            case ALTA -> 2;
            case MEDIA -> 3;
            case BAJA -> 4;
        };
    }

}