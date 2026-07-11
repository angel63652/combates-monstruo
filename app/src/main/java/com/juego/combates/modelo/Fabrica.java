package com.juego.combates.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Crea las especies del juego y equipos aleatorios de 3 criaturas.
 * Cada llamada devuelve instancias nuevas (HP y PP a tope).
 */
public final class Fabrica {

    public static final int TAMANO_EQUIPO = 3;

    private static final String[] ESPECIES = {
            "Fogarto", "Acuarin", "Hojaruz", "Chispin", "Rocardo", "Normalino"
    };

    private Fabrica() {
    }

    /** Nombres de todas las especies que el jugador puede elegir. */
    public static String[] nombresEspecies() {
        return ESPECIES.clone();
    }

    public static Criatura crear(String especie) {
        switch (especie) {
            case "Fogarto":
                return new Criatura("Fogarto", Tipo.FUEGO, 50, 175, 85, 70, 90)
                        .conMovimientos(
                                Movimiento.ataque("Ascuas", Tipo.FUEGO, 40, 100, 25),
                                Movimiento.ataque("Llamarada", Tipo.FUEGO, 95, 85, 10),
                                Movimiento.ataque("Mordisco", Tipo.NORMAL, 60, 95, 20),
                                Movimiento.ataque("Placaje", Tipo.NORMAL, 40, 100, 35));
            case "Acuarin":
                return new Criatura("Acuarin", Tipo.AGUA, 50, 185, 78, 80, 75)
                        .conMovimientos(
                                Movimiento.ataque("Pistola Agua", Tipo.AGUA, 40, 100, 25),
                                Movimiento.ataque("Hidrobomba", Tipo.AGUA, 105, 80, 8),
                                Movimiento.ataque("Placaje", Tipo.NORMAL, 40, 100, 35),
                                Movimiento.curacion("Recuperación", Tipo.NORMAL, 8));
            case "Hojaruz":
                return new Criatura("Hojaruz", Tipo.PLANTA, 50, 190, 75, 85, 65)
                        .conMovimientos(
                                Movimiento.ataque("Hoja Afilada", Tipo.PLANTA, 55, 95, 25),
                                Movimiento.ataque("Rayo Vegetal", Tipo.PLANTA, 110, 80, 8),
                                Movimiento.ataque("Placaje", Tipo.NORMAL, 40, 100, 35),
                                Movimiento.curacion("Síntesis", Tipo.PLANTA, 8));
            case "Chispin":
                return new Criatura("Chispin", Tipo.ELECTRICO, 50, 165, 82, 65, 105)
                        .conMovimientos(
                                Movimiento.ataque("Impactrueno", Tipo.ELECTRICO, 40, 100, 25),
                                Movimiento.ataque("Trueno", Tipo.ELECTRICO, 105, 70, 10),
                                Movimiento.ataque("Ataque Rápido", Tipo.NORMAL, 40, 100, 30),
                                Movimiento.ataque("Mordisco", Tipo.NORMAL, 60, 95, 20));
            case "Rocardo":
                return new Criatura("Rocardo", Tipo.ROCA, 50, 200, 90, 100, 45)
                        .conMovimientos(
                                Movimiento.ataque("Lanzarrocas", Tipo.ROCA, 50, 90, 20),
                                Movimiento.ataque("Avalancha", Tipo.ROCA, 80, 90, 10),
                                Movimiento.ataque("Placaje", Tipo.NORMAL, 40, 100, 35),
                                Movimiento.curacion("Descanso", Tipo.NORMAL, 6));
            case "Normalino":
            default:
                return new Criatura("Normalino", Tipo.NORMAL, 50, 210, 80, 75, 70)
                        .conMovimientos(
                                Movimiento.ataque("Placaje", Tipo.NORMAL, 40, 100, 35),
                                Movimiento.ataque("Golpe Cuerpo", Tipo.NORMAL, 85, 100, 12),
                                Movimiento.ataque("Mordisco", Tipo.NORMAL, 60, 95, 20),
                                Movimiento.curacion("Recuperación", Tipo.NORMAL, 8));
        }
    }

    /** Equipo aleatorio de 3 especies distintas. */
    public static List<Criatura> equipoAleatorio(Random rng) {
        List<String> nombres = new ArrayList<>();
        Collections.addAll(nombres, ESPECIES);
        Collections.shuffle(nombres, rng);
        List<Criatura> equipo = new ArrayList<>();
        for (int i = 0; i < TAMANO_EQUIPO; i++) {
            equipo.add(crear(nombres.get(i)));
        }
        return equipo;
    }
}
