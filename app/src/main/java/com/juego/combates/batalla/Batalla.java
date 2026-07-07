package com.juego.combates.batalla;

import com.juego.combates.modelo.Criatura;
import com.juego.combates.modelo.Movimiento;
import com.juego.combates.modelo.Tipo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Motor de combate por turnos. Cada acción del jugador produce una lista
 * de Eventos (texto + efecto visual) que la interfaz muestra uno a uno.
 */
public class Batalla {

    /** Un paso narrado del combate, con un efecto visual opcional. */
    public static class Evento {
        public static final int NADA = 0;
        public static final int GOLPE_RIVAL = 1;    // el rival recibe un golpe
        public static final int GOLPE_JUGADOR = 2;  // el jugador recibe un golpe
        public static final int REFRESCO = 3;       // cambió una criatura / curación

        public final String texto;
        public final int efecto;

        public Evento(String texto, int efecto) {
            this.texto = texto;
            this.efecto = efecto;
        }
    }

    public final List<Criatura> equipoJugador;
    public final List<Criatura> equipoRival;
    public Criatura activaJugador;
    public Criatura activaRival;

    private final Random rng;
    private final Movimiento forcejeo =
            Movimiento.ataque("Forcejeo", Tipo.NORMAL, 35, 100, 9999);

    public Batalla(List<Criatura> equipoJugador, List<Criatura> equipoRival, Random rng) {
        this.equipoJugador = equipoJugador;
        this.equipoRival = equipoRival;
        this.rng = rng;
        this.activaJugador = equipoJugador.get(0);
        this.activaRival = equipoRival.get(0);
    }

    public boolean jugadorDerrotado() {
        return sinCriaturasVivas(equipoJugador);
    }

    public boolean rivalDerrotado() {
        return sinCriaturasVivas(equipoRival);
    }

    private boolean sinCriaturasVivas(List<Criatura> equipo) {
        for (Criatura c : equipo) {
            if (!c.estaDebilitada()) return false;
        }
        return true;
    }

    /** Turno completo: el jugador usa el movimiento de ese índice y el rival responde. */
    public List<Evento> ejecutarTurno(int indiceMovimiento) {
        List<Evento> eventos = new ArrayList<>();
        Movimiento movJugador = movimientoOForcejeo(activaJugador, indiceMovimiento, eventos, activaJugador.nombre);
        Movimiento movRival = elegirMovimientoIA();

        boolean jugadorPrimero =
                activaJugador.velocidad > activaRival.velocidad
                        || (activaJugador.velocidad == activaRival.velocidad && rng.nextBoolean());

        if (jugadorPrimero) {
            atacar(activaJugador, activaRival, movJugador, true, eventos);
            if (!activaRival.estaDebilitada() && !activaJugador.estaDebilitada()) {
                atacar(activaRival, activaJugador, movRival, false, eventos);
            }
        } else {
            atacar(activaRival, activaJugador, movRival, false, eventos);
            if (!activaJugador.estaDebilitada() && !activaRival.estaDebilitada()) {
                atacar(activaJugador, activaRival, movJugador, true, eventos);
            }
        }

        reemplazoRivalSiHaceFalta(eventos);
        return eventos;
    }

    /** Cambio voluntario: gasta el turno y el rival ataca a la criatura entrante. */
    public List<Evento> cambioVoluntario(int indiceEquipo) {
        List<Evento> eventos = new ArrayList<>();
        Criatura entrante = equipoJugador.get(indiceEquipo);
        eventos.add(new Evento("¡Vuelve, " + activaJugador.nombre + "!", Evento.NADA));
        activaJugador = entrante;
        eventos.add(new Evento("¡Adelante, " + entrante.nombre + "!", Evento.REFRESCO));

        Movimiento movRival = elegirMovimientoIA();
        atacar(activaRival, activaJugador, movRival, false, eventos);
        reemplazoRivalSiHaceFalta(eventos);
        return eventos;
    }

    /** Cambio gratuito tras un debilitamiento: el rival no ataca. */
    public List<Evento> cambioTrasKO(int indiceEquipo) {
        List<Evento> eventos = new ArrayList<>();
        activaJugador = equipoJugador.get(indiceEquipo);
        eventos.add(new Evento("¡Adelante, " + activaJugador.nombre + "!", Evento.REFRESCO));
        return eventos;
    }

    // ------------------------------------------------------------------
    // Lógica interna
    // ------------------------------------------------------------------

    private Movimiento movimientoOForcejeo(Criatura c, int indice, List<Evento> eventos, String nombre) {
        if (indice >= 0 && indice < c.movimientos.size()) {
            Movimiento m = c.movimientos.get(indice);
            if (m.tienePP()) return m;
        }
        eventos.add(new Evento("¡A " + nombre + " no le quedan PP! Usará Forcejeo.", Evento.NADA));
        return forcejeo;
    }

    private void atacar(Criatura atacante, Criatura defensor, Movimiento mov,
                        boolean esJugador, List<Evento> eventos) {
        if (mov != forcejeo) {
            mov.pp--;
        }

        String quien = esJugador ? atacante.nombre : atacante.nombre + " enemigo";
        eventos.add(new Evento("¡" + quien + " usa " + mov.nombre + "!", Evento.NADA));

        if (mov.esCuracion) {
            int curado = atacante.curar(atacante.hpMax / 2);
            if (curado > 0) {
                eventos.add(new Evento(atacante.nombre + " recupera " + curado + " PS.", Evento.REFRESCO));
            } else {
                eventos.add(new Evento("Pero no tiene efecto...", Evento.NADA));
            }
            return;
        }

        if (rng.nextInt(100) >= mov.precision) {
            eventos.add(new Evento("¡Pero el ataque falló!", Evento.NADA));
            return;
        }

        double efectividad = mov.tipo.multiplicadorContra(defensor.tipo);
        double stab = (mov.tipo == atacante.tipo) ? 1.5 : 1.0;
        boolean critico = rng.nextInt(12) == 0;
        double variacion = 0.85 + rng.nextDouble() * 0.15;

        double base = (((2.0 * atacante.nivel / 5.0 + 2.0) * mov.potencia
                * atacante.ataque / defensor.defensa) / 50.0 + 2.0);
        int dano = Math.max(1, (int) (base * stab * efectividad * variacion * (critico ? 1.5 : 1.0)));

        defensor.recibirDano(dano);
        eventos.add(new Evento(defensor.nombre + " pierde " + dano + " PS.",
                esJugador ? Evento.GOLPE_RIVAL : Evento.GOLPE_JUGADOR));

        if (critico) {
            eventos.add(new Evento("¡Golpe crítico!", Evento.NADA));
        }
        if (efectividad > 1.0) {
            eventos.add(new Evento("¡Es muy eficaz!", Evento.NADA));
        } else if (efectividad < 1.0) {
            eventos.add(new Evento("No es muy eficaz...", Evento.NADA));
        }

        if (defensor.estaDebilitada()) {
            eventos.add(new Evento("¡" + defensor.nombre + " se debilitó!", Evento.REFRESCO));
        }
    }

    /** Si la criatura rival cayó y quedan reservas, la IA envía la mejor opción. */
    private void reemplazoRivalSiHaceFalta(List<Evento> eventos) {
        if (!activaRival.estaDebilitada() || rivalDerrotado()) {
            return;
        }
        Criatura mejor = null;
        double mejorPuntaje = -1;
        for (Criatura c : equipoRival) {
            if (c.estaDebilitada()) continue;
            double puntaje = c.tipo.multiplicadorContra(activaJugador.tipo)
                    / activaJugador.tipo.multiplicadorContra(c.tipo);
            if (puntaje > mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejor = c;
            }
        }
        if (mejor != null) {
            activaRival = mejor;
            eventos.add(new Evento("¡El rival envía a " + mejor.nombre + "!", Evento.REFRESCO));
        }
    }

    /**
     * IA rival: se cura si está baja de vida (a veces); si no, suele elegir
     * el ataque con mayor daño esperado, con un 20% de elección aleatoria.
     */
    private Movimiento elegirMovimientoIA() {
        List<Movimiento> disponibles = new ArrayList<>();
        for (Movimiento m : activaRival.movimientos) {
            if (m.tienePP()) disponibles.add(m);
        }
        if (disponibles.isEmpty()) {
            return forcejeo;
        }

        if (activaRival.fraccionVida() < 0.35f && rng.nextInt(100) < 55) {
            for (Movimiento m : disponibles) {
                if (m.esCuracion) return m;
            }
        }

        if (rng.nextInt(100) < 20) {
            return disponibles.get(rng.nextInt(disponibles.size()));
        }

        Movimiento mejor = disponibles.get(0);
        double mejorEsperado = -1;
        for (Movimiento m : disponibles) {
            if (m.esCuracion) continue;
            double stab = (m.tipo == activaRival.tipo) ? 1.5 : 1.0;
            double esperado = m.potencia * stab
                    * m.tipo.multiplicadorContra(activaJugador.tipo)
                    * (m.precision / 100.0);
            if (esperado > mejorEsperado) {
                mejorEsperado = esperado;
                mejor = m;
            }
        }
        return mejor;
    }
}
