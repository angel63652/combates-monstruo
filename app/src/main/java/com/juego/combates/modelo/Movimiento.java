package com.juego.combates.modelo;

/**
 * Un movimiento de combate. Si esCuracion es true, en vez de dañar
 * restaura el 50% de la vida máxima del usuario.
 */
public class Movimiento {
    public final String nombre;
    public final Tipo tipo;
    public final int potencia;   // ignorada en movimientos de curación
    public final int precision;  // 0..100
    public final int ppMax;
    public final boolean esCuracion;
    public int pp;

    public Movimiento(String nombre, Tipo tipo, int potencia, int precision, int ppMax, boolean esCuracion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.potencia = potencia;
        this.precision = precision;
        this.ppMax = ppMax;
        this.esCuracion = esCuracion;
        this.pp = ppMax;
    }

    public static Movimiento ataque(String nombre, Tipo tipo, int potencia, int precision, int ppMax) {
        return new Movimiento(nombre, tipo, potencia, precision, ppMax, false);
    }

    public static Movimiento curacion(String nombre, Tipo tipo, int ppMax) {
        return new Movimiento(nombre, tipo, 0, 100, ppMax, true);
    }

    public boolean tienePP() {
        return pp > 0;
    }
}
