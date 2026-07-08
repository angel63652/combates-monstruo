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

    /**
     * Un paso narrado del combate. Guarda una "foto" del estado (qué criatura
     * hay en cada lado y su vida en ese instante) para que la vista dibuje lo
     * que corresponde a este mensaje y no el estado final del turno, que ya
     * está calculado por adelantado.
     */
    public static class Evento {
        public static final int NADA = 0;
        public static final int GOLPE_RIVAL = 1;    // el rival recibe un golpe
        public static final int GOLPE_JUGADOR = 2;  // el jugador recibe un golpe
        public static final int REFRESCO = 3;       // cambió una criatura / curación

        public final String texto;
        public final int efecto;
        public final Criatura jugador;
        public final int hpJugador;
        public final Criatura rival;
        public final int hpRival;

        public Evento(String texto, int efecto, Criatura jugador, int hpJugador,
                      Criatura rival, int hpRival) {
            this.texto = texto;
            this.efecto = efecto;
            this.jugador = jugador;
            this.hpJugador = hpJugador;
            this.rival = rival;
            this.hpRival = hpRival;
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

    /** Presentación al empezar el combate. */
    public List<Evento> eventosIntro() {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(ev("¡Un entrenador rival te desafía!", Evento.NADA));
        eventos.add(ev("El rival envía a " + activaRival.nombre + ".", Evento.REFRESCO));
        eventos.add(ev("¡Adelante, " + activaJugador.nombre + "!", Evento.REFRESCO));
        return eventos;
    }

    /** Turno completo: el jugador usa el movimiento de ese índice y el rival responde. */
    public List<Evento> ejecutarTurno(int indiceMovimiento) {
        List<Evento> eventos = new ArrayList<>();
        Movimiento movJugador = movimientoOForcejeo(activaJugador, indiceMovimiento);
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
        eventos.add(ev("¡Vuelve, " + activaJugador.nombre + "!", Evento.NADA));
        activaJugador = entrante;
        eventos.add(ev("¡Adelante, " + entrante.nombre + "!", Evento.REFRESCO));

        Movimiento movRival = elegirMovimientoIA();
        atacar(activaRival, activaJugador, movRival, false, eventos);
        reemplazoRivalSiHaceFalta(eventos);
        return eventos;
    }

    /** Cambio gratuito tras un debilitamiento: el rival no ataca. */
    public List<Evento> cambioTrasKO(int indiceEquipo) {
        List<Evento> eventos = new ArrayList<>();
        activaJugador = equipoJugador.get(indiceEquipo);
        eventos.add(ev("¡Adelante, " + activaJugador.nombre + "!", Evento.REFRESCO));
        return eventos;
    }

    // ------------------------------------------------------------------
    // Lógica interna
    // ------------------------------------------------------------------

    /** Crea un evento con la foto del estado actual (criaturas activas y su vida). */
    private Evento ev(String texto, int efecto) {
        return new Evento(texto, efecto,
                activaJugador, activaJugador.hp, activaRival, activaRival.hp);
    }

    private Movimiento movimientoOForcejeo(Criatura c, int indice) {
        if (indice >= 0 && indice < c.movimientos.size()) {
            Movimiento m = c.movimientos.get(indice);
            if (m.tienePP()) return m;
        }
        return forcejeo;
    }

    private void atacar(Criatura atacante, Criatura defensor, Movimiento mov,
                        boolean esJugador, List<Evento> eventos) {
        // El sufijo "enemigo" distingue los bandos cuando ambos usan la misma especie.
        String quien = esJugador ? atacante.nombre : atacante.nombre + " enemigo";
        String quienDefensor = esJugador ? defensor.nombre + " enemigo" : defensor.nombre;

        if (mov == forcejeo) {
            eventos.add(ev("¡A " + quien + " no le quedan PP! ¡Usa Forcejeo!", Evento.NADA));
        } else {
            mov.pp--;
            eventos.add(ev("¡" + quien + " usa " + mov.nombre + "!", Evento.NADA));
        }

        if (mov.esCuracion) {
            int curado = atacante.curar(atacante.hpMax / 2);
            if (curado > 0) {
                eventos.add(ev(quien + " recupera " + curado + " PS.", Evento.REFRESCO));
            } else {
                eventos.add(ev("Pero no tiene efecto...", Evento.NADA));
            }
            return;
        }

        if (rng.nextInt(100) >= mov.precision) {
            eventos.add(ev("¡Pero el ataque falló!", Evento.NADA));
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
        eventos.add(ev(quienDefensor + " pierde " + dano + " PS.",
                esJugador ? Evento.GOLPE_RIVAL : Evento.GOLPE_JUGADOR));

        if (critico) {
            eventos.add(ev("¡Golpe crítico!", Evento.NADA));
        }
        if (efectividad > 1.0) {
            eventos.add(ev("¡Es muy eficaz!", Evento.NADA));
        } else if (efectividad < 1.0) {
            eventos.add(ev("No es muy eficaz...", Evento.NADA));
        }

        if (defensor.estaDebilitada()) {
            eventos.add(ev("¡" + quienDefensor + " se debilitó!", Evento.REFRESCO));
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
            eventos.add(ev("¡El rival envía a " + mejor.nombre + "!", Evento.REFRESCO));
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
