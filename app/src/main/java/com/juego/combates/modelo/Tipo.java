package com.juego.combates.modelo;

/**
 * Tipos elementales con su tabla de efectividades y color para la interfaz.
 */
public enum Tipo {
    NORMAL("Normal", 0xFF9AA0A6),
    FUEGO("Fuego", 0xFFE25822),
    AGUA("Agua", 0xFF3B82F6),
    PLANTA("Planta", 0xFF34A853),
    ELECTRICO("Eléctrico", 0xFFF0A500),
    ROCA("Roca", 0xFF8D6E63);

    public final String nombre;
    public final int color;

    Tipo(String nombre, int color) {
        this.nombre = nombre;
        this.color = color;
    }

    // Tabla de efectividades: TABLA[atacante][defensor]
    // Orden: NORMAL, FUEGO, AGUA, PLANTA, ELECTRICO, ROCA
    private static final double[][] TABLA = {
            /* NORMAL    */ {1.0, 1.0, 1.0, 1.0, 1.0, 0.5},
            /* FUEGO     */ {1.0, 0.5, 0.5, 2.0, 1.0, 0.5},
            /* AGUA      */ {1.0, 2.0, 0.5, 0.5, 1.0, 2.0},
            /* PLANTA    */ {1.0, 0.5, 2.0, 0.5, 1.0, 2.0},
            /* ELECTRICO */ {1.0, 1.0, 2.0, 0.5, 0.5, 1.0},
            /* ROCA      */ {1.0, 2.0, 1.0, 1.0, 1.0, 0.5}
    };

    /** Multiplicador de daño de este tipo atacando al tipo defensor. */
    public double multiplicadorContra(Tipo defensor) {
        return TABLA[this.ordinal()][defensor.ordinal()];
    }
}
