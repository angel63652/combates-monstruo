package com.juego.combates.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Una criatura combatiente con sus estadísticas y hasta 4 movimientos.
 */
public class Criatura {
    public final String nombre;
    public final Tipo tipo;
    public final int nivel;
    public final int hpMax;
    public final int ataque;
    public final int defensa;
    public final int velocidad;
    public final List<Movimiento> movimientos = new ArrayList<>();
    public int hp;

    public Criatura(String nombre, Tipo tipo, int nivel,
                    int hpMax, int ataque, int defensa, int velocidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.nivel = nivel;
        this.hpMax = hpMax;
        this.hp = hpMax;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
    }

    public Criatura conMovimientos(Movimiento... movs) {
        for (Movimiento m : movs) {
            movimientos.add(m);
        }
        return this;
    }

    public boolean estaDebilitada() {
        return hp <= 0;
    }

    public void recibirDano(int dano) {
        hp = Math.max(0, hp - dano);
    }

    public int curar(int cantidad) {
        int antes = hp;
        hp = Math.min(hpMax, hp + cantidad);
        return hp - antes;
    }

    public float fraccionVida() {
        return hpMax <= 0 ? 0f : (float) hp / (float) hpMax;
    }

    /** True si le queda al menos un movimiento con PP. */
    public boolean tieneAlgunPP() {
        for (Movimiento m : movimientos) {
            if (m.tienePP()) return true;
        }
        return false;
    }
}
