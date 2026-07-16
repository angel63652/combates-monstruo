package com.juego.combates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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
 * Pantalla de combate. La disposición está en res/layout/activity_main.xml
 * (el escenario es la vista VistaBatalla, dibujada por código con Canvas).
 * Aquí va el flujo del combate: turnos, mensajes y diálogos.
 */
public class MainActivity extends Activity {

    /** Clave del extra con las especies del equipo del jugador (String[]). */
    public static final String EXTRA_EQUIPO = "com.juego.combates.EQUIPO";

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
    private AlertDialog dialogoActual;
    private String[] nombresEquipoJugador; // null = equipo al azar (combate rápido)

    // Cerrojo: mientras hay una acción o animación en curso se ignora
    // cualquier pulsación nueva (evita que dos toques a la vez lancen dos
    // acciones en el mismo turno).
    private boolean ocupado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nombresEquipoJugador = getIntent().getStringArrayExtra(EXTRA_EQUIPO);
        setContentView(R.layout.activity_main);
        enlazarVistas();
        iniciarBatalla();
    }

    /** Localiza las vistas del layout y engancha los botones. */
    private void enlazarVistas() {
        vistaBatalla = findViewById(R.id.vista_batalla);
        scrollLog = findViewById(R.id.scroll_log);
        textoLog = findViewById(R.id.texto_log);

        int[] idsMov = {R.id.boton_mov_0, R.id.boton_mov_1, R.id.boton_mov_2, R.id.boton_mov_3};
        for (int i = 0; i < idsMov.length; i++) {
            Button b = findViewById(idsMov[i]);
            final int indice = i;
            b.setOnClickListener(v -> alPulsarMovimiento(indice));
            botonesMovimiento.add(b);
        }

        botonEquipo = findViewById(R.id.boton_equipo);
        botonEquipo.setOnClickListener(v -> alPulsarEquipo());
        botonNueva = findViewById(R.id.boton_nueva);
        botonNueva.setOnClickListener(v -> alPulsarNueva());
    }

    private void tintar(Button b, int color) {
        b.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    // ------------------------------------------------------------------
    // Cerrojo de acciones (una acción a la vez)
    // ------------------------------------------------------------------

    /** Intenta tomar el turno; devuelve false si ya hay algo en curso. */
    private boolean tomarAccion() {
        if (ocupado) return false;
        ocupado = true;
        habilitarControles(false);
        return true;
    }

    /** Libera el cerrojo y devuelve el control al jugador. */
    private void liberarAccion() {
        ocupado = false;
        habilitarControles(true);
    }

    // ------------------------------------------------------------------
    // Flujo del combate
    // ------------------------------------------------------------------

    private void iniciarBatalla() {
        ocupado = true;
        List<Criatura> equipoJugador =
                (nombresEquipoJugador != null && nombresEquipoJugador.length > 0)
                        ? equipoDesdeNombres(nombresEquipoJugador)
                        : Fabrica.equipoAleatorio(rng);
        batalla = new Batalla(equipoJugador, Fabrica.equipoAleatorio(rng), rng);
        vistaBatalla.setBatalla(batalla);
        textoLog.setText("");
        actualizarBotones();
        habilitarControles(false);
        mostrarEventos(batalla.eventosIntro(), this::finDeAccion);
    }

    private List<Criatura> equipoDesdeNombres(String[] nombres) {
        List<Criatura> equipo = new ArrayList<>();
        for (String nombre : nombres) {
            equipo.add(Fabrica.crear(nombre));
        }
        return equipo;
    }

    private void alPulsarMovimiento(int indice) {
        if (!tomarAccion()) return;
        mostrarEventos(batalla.ejecutarTurno(indice), this::finDeAccion);
    }

    private void alPulsarEquipo() {
        if (!tomarAccion()) return;
        abrirDialogoEquipo(false);
    }

    private void alPulsarNueva() {
        if (!tomarAccion()) return;
        confirmarNuevaBatalla();
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
        vistaBatalla.mostrarEvento(e);
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
        liberarAccion();
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
                tintar(b, m.tienePP() ? m.tipo.color : 0xFF555B63);
                b.setTag(m.tienePP() || sinPP); // pulsable si tiene PP, o Forcejeo si no queda nada
            } else {
                b.setVisibility(View.INVISIBLE);
                b.setTag(false);
            }
        }
        if (sinPP && !activa.movimientos.isEmpty()) {
            Button b = botonesMovimiento.get(0);
            b.setText("Forcejeo\nNormal · sin PP");
            tintar(b, 0xFF9AA0A6);
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
            agregarLog(getString(R.string.dlg_sin_reservas));
            if (!esTrasKO) liberarAccion();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(esTrasKO
                        ? getString(R.string.dlg_equipo_ko_titulo)
                        : getString(R.string.dlg_equipo_titulo))
                .setItems(etiquetas.toArray(new String[0]), (dialogo, pos) -> {
                    int indiceEquipo = indices.get(pos);
                    List<Batalla.Evento> eventos = esTrasKO
                            ? batalla.cambioTrasKO(indiceEquipo)
                            : batalla.cambioVoluntario(indiceEquipo);
                    mostrarEventos(eventos, this::finDeAccion);
                });
        if (esTrasKO) {
            builder.setCancelable(false);
        } else {
            builder.setNegativeButton(getString(R.string.dlg_cancelar), (d, x) -> liberarAccion());
            builder.setOnCancelListener(d -> liberarAccion());
        }
        dialogoActual = builder.show();
    }

    private void mostrarFin(boolean victoria) {
        dialogoActual = new AlertDialog.Builder(this)
                .setTitle(victoria
                        ? getString(R.string.dlg_victoria_titulo)
                        : getString(R.string.dlg_derrota_titulo))
                .setMessage(victoria
                        ? getString(R.string.dlg_victoria_msg)
                        : getString(R.string.dlg_derrota_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dlg_otra_vez), (d, x) -> iniciarBatalla())
                .setNeutralButton(getString(R.string.dlg_cambiar_equipo), (d, x) -> irASeleccion())
                .setNegativeButton(getString(R.string.dlg_menu), (d, x) -> irAlMenu())
                .show();
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MenuActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void irASeleccion() {
        startActivity(new Intent(this, SeleccionActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void confirmarNuevaBatalla() {
        dialogoActual = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dlg_nueva_titulo))
                .setMessage(getString(R.string.dlg_nueva_msg))
                .setPositiveButton(getString(R.string.dlg_si), (d, x) -> iniciarBatalla())
                .setNegativeButton(getString(R.string.dlg_no), (d, x) -> liberarAccion())
                .setOnCancelListener(d -> liberarAccion())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        // Cierra cualquier diálogo abierto para no filtrar la ventana si la
        // Activity se destruye con él visible.
        if (dialogoActual != null && dialogoActual.isShowing()) {
            dialogoActual.dismiss();
        }
        dialogoActual = null;
    }
}
