package com.juego.combates;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.juego.combates.batalla.Batalla;
import com.juego.combates.modelo.Criatura;
import com.juego.combates.modelo.Fabrica;
import com.juego.combates.modelo.Movimiento;
import com.juego.combates.ui.VistaBatalla;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pantalla única del juego. Toda la interfaz se construye en Java puro:
 * arriba el escenario (Canvas), en medio el registro de combate y abajo
 * los botones de movimientos y de equipo.
 */
public class MainActivity extends Activity {

    private static final long RETARDO_MENSAJE_MS = 700;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rng = new Random();

    private Batalla batalla;
    private VistaBatalla vistaBatalla;
    private TextView textoLog;
    private ScrollView scrollLog;
    private final List<Button> botonesMovimiento = new ArrayList<>();
    private Button botonEquipo;
    private Button botonNueva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        construirInterfaz();
        iniciarBatalla();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz (sin XML)
    // ------------------------------------------------------------------

    private void construirInterfaz() {
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(0xFF20232A);

        // Escenario de batalla
        vistaBatalla = new VistaBatalla(this);
        raiz.addView(vistaBatalla, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Registro de combate
        scrollLog = new ScrollView(this);
        scrollLog.setBackgroundColor(0xFF2B3038);
        scrollLog.setPadding(dp(12), dp(8), dp(12), dp(8));
        textoLog = new TextView(this);
        textoLog.setTextColor(0xFFEDEFF2);
        textoLog.setTextSize(15f);
        textoLog.setTypeface(Typeface.MONOSPACE);
        scrollLog.addView(textoLog);
        raiz.addView(scrollLog, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(110)));

        // Botones de movimientos (2 x 2)
        LinearLayout fila1 = filaHorizontal();
        LinearLayout fila2 = filaHorizontal();
        for (int i = 0; i < 4; i++) {
            Button b = new Button(this);
            b.setTextColor(Color.WHITE);
            b.setAllCaps(false);
            b.setTextSize(14f);
            b.setLines(2);
            final int indice = i;
            b.setOnClickListener(v -> alPulsarMovimiento(indice));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            (i < 2 ? fila1 : fila2).addView(b, lp);
            botonesMovimiento.add(b);
        }
        raiz.addView(fila1, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));
        raiz.addView(fila2, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));

        // Fila inferior: cambio de criatura y nueva batalla
        LinearLayout fila3 = filaHorizontal();
        botonEquipo = new Button(this);
        botonEquipo.setText("Equipo");
        botonEquipo.setAllCaps(false);
        botonEquipo.setTextColor(Color.WHITE);
        estilizarBoton(botonEquipo, 0xFF5865F2);
        botonEquipo.setOnClickListener(v -> abrirDialogoEquipo(false));

        botonNueva = new Button(this);
        botonNueva.setText("Nueva batalla");
        botonNueva.setAllCaps(false);
        botonNueva.setTextColor(Color.WHITE);
        estilizarBoton(botonNueva, 0xFF6E7681);
        botonNueva.setOnClickListener(v -> confirmarNuevaBatalla());

        LinearLayout.LayoutParams lpMitad = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lpMitad.setMargins(dp(4), dp(4), dp(4), dp(8));
        fila3.addView(botonEquipo, lpMitad);
        LinearLayout.LayoutParams lpMitad2 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lpMitad2.setMargins(dp(4), dp(4), dp(4), dp(8));
        fila3.addView(botonNueva, lpMitad2);
        raiz.addView(fila3, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56)));

        setContentView(raiz);
    }

    private LinearLayout filaHorizontal() {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(dp(4), 0, dp(4), 0);
        return fila;
    }

    private void estilizarBoton(Button b, int color) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(dp(10));
        b.setBackground(fondo);
    }

    private int dp(float valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ------------------------------------------------------------------
    // Flujo del combate
    // ------------------------------------------------------------------

    private void iniciarBatalla() {
        batalla = new Batalla(Fabrica.equipoAleatorio(rng), Fabrica.equipoAleatorio(rng), rng);
        vistaBatalla.setBatalla(batalla);
        textoLog.setText("");
        actualizarBotones();
        habilitarControles(false);

        List<Batalla.Evento> intro = new ArrayList<>();
        intro.add(new Batalla.Evento("¡Un entrenador rival te desafía!", Batalla.Evento.NADA));
        intro.add(new Batalla.Evento("El rival envía a " + batalla.activaRival.nombre + ".",
                Batalla.Evento.REFRESCO));
        intro.add(new Batalla.Evento("¡Adelante, " + batalla.activaJugador.nombre + "!",
                Batalla.Evento.REFRESCO));
        mostrarEventos(intro, this::finDeAccion);
    }

    private void alPulsarMovimiento(int indice) {
        habilitarControles(false);
        List<Batalla.Evento> eventos = batalla.ejecutarTurno(indice);
        mostrarEventos(eventos, this::finDeAccion);
    }

    /** Muestra los eventos uno a uno con retardo, disparando animaciones. */
    private void mostrarEventos(List<Batalla.Evento> eventos, Runnable alTerminar) {
        mostrarEventoEn(eventos, 0, alTerminar);
    }

    private void mostrarEventoEn(List<Batalla.Evento> eventos, int i, Runnable alTerminar) {
        if (i >= eventos.size()) {
            alTerminar.run();
            return;
        }
        Batalla.Evento e = eventos.get(i);
        agregarLog(e.texto);
        if (e.efecto == Batalla.Evento.GOLPE_RIVAL || e.efecto == Batalla.Evento.GOLPE_JUGADOR) {
            vistaBatalla.animarGolpe(e.efecto);
        } else {
            vistaBatalla.invalidate();
        }
        handler.postDelayed(() -> mostrarEventoEn(eventos, i + 1, alTerminar), RETARDO_MENSAJE_MS);
    }

    /** Comprobaciones tras cada tanda de eventos: fin de combate, KO propio, o seguir. */
    private void finDeAccion() {
        vistaBatalla.invalidate();
        actualizarBotones();

        if (batalla.rivalDerrotado()) {
            mostrarFin(true);
            return;
        }
        if (batalla.jugadorDerrotado()) {
            mostrarFin(false);
            return;
        }
        if (batalla.activaJugador.estaDebilitada()) {
            abrirDialogoEquipo(true);
            return;
        }
        habilitarControles(true);
    }

    private void agregarLog(String texto) {
        if (textoLog.length() > 0) {
            textoLog.append("\n");
        }
        textoLog.append(texto);
        scrollLog.post(() -> scrollLog.fullScroll(View.FOCUS_DOWN));
    }

    private void actualizarBotones() {
        Criatura activa = batalla.activaJugador;
        boolean sinPP = !activa.tieneAlgunPP();
        for (int i = 0; i < botonesMovimiento.size(); i++) {
            Button b = botonesMovimiento.get(i);
            if (i < activa.movimientos.size()) {
                Movimiento m = activa.movimientos.get(i);
                b.setVisibility(View.VISIBLE);
                b.setText(m.nombre + "\n" + m.tipo.nombre + " · PP " + m.pp + "/" + m.ppMax);
                estilizarBoton(b, m.tienePP() ? m.tipo.color : 0xFF555B63);
                b.setTag(m.tienePP() || sinPP); // pulsable si tiene PP, o Forcejeo si no queda nada
            } else {
                b.setVisibility(View.INVISIBLE);
                b.setTag(false);
            }
        }
        if (sinPP && !activa.movimientos.isEmpty()) {
            Button b = botonesMovimiento.get(0);
            b.setText("Forcejeo\nNormal · sin PP");
            estilizarBoton(b, 0xFF9AA0A6);
        }
    }

    private void habilitarControles(boolean habilitar) {
        for (Button b : botonesMovimiento) {
            boolean pulsable = habilitar && Boolean.TRUE.equals(b.getTag());
            b.setEnabled(pulsable);
            b.setAlpha(pulsable ? 1f : 0.45f);
        }
        botonEquipo.setEnabled(habilitar);
        botonEquipo.setAlpha(habilitar ? 1f : 0.45f);
        botonNueva.setEnabled(habilitar);
        botonNueva.setAlpha(habilitar ? 1f : 0.45f);
    }

    // ------------------------------------------------------------------
    // Diálogos
    // ------------------------------------------------------------------

    /** Diálogo de cambio. Si esTrasKO, es obligatorio y el cambio es gratis. */
    private void abrirDialogoEquipo(boolean esTrasKO) {
        final List<Integer> indices = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        for (int i = 0; i < batalla.equipoJugador.size(); i++) {
            Criatura c = batalla.equipoJugador.get(i);
            if (c.estaDebilitada() || c == batalla.activaJugador) continue;
            indices.add(i);
            etiquetas.add(c.nombre + "  (" + c.tipo.nombre + ")  " + c.hp + "/" + c.hpMax + " PS");
        }
        if (indices.isEmpty()) {
            agregarLog("No tienes más criaturas disponibles.");
            if (!esTrasKO) habilitarControles(true);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(esTrasKO ? "¡Elige tu siguiente criatura!" : "¿A quién envías?")
                .setItems(etiquetas.toArray(new String[0]), (dialogo, pos) -> {
                    int indiceEquipo = indices.get(pos);
                    habilitarControles(false);
                    List<Batalla.Evento> eventos = esTrasKO
                            ? batalla.cambioTrasKO(indiceEquipo)
                            : batalla.cambioVoluntario(indiceEquipo);
                    mostrarEventos(eventos, this::finDeAccion);
                });
        if (esTrasKO) {
            builder.setCancelable(false);
        } else {
            builder.setNegativeButton("Cancelar", (d, x) -> habilitarControles(true));
            builder.setOnCancelListener(d -> habilitarControles(true));
        }
        builder.show();
    }

    private void mostrarFin(boolean victoria) {
        habilitarControles(false);
        new AlertDialog.Builder(this)
                .setTitle(victoria ? "¡Victoria!" : "Derrota...")
                .setMessage(victoria
                        ? "¡Has derrotado al entrenador rival!"
                        : "Todas tus criaturas se han debilitado.")
                .setCancelable(false)
                .setPositiveButton("Nueva batalla", (d, x) -> iniciarBatalla())
                .show();
    }

    private void confirmarNuevaBatalla() {
        new AlertDialog.Builder(this)
                .setTitle("Nueva batalla")
                .setMessage("¿Abandonar este combate y empezar otro con equipos nuevos?")
                .setPositiveButton("Sí", (d, x) -> iniciarBatalla())
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
