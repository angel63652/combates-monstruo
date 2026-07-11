package com.juego.combates;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.juego.combates.modelo.Criatura;
import com.juego.combates.modelo.Fabrica;
import com.juego.combates.modelo.Movimiento;
import com.juego.combates.ui.VistaCriatura;

import java.util.ArrayList;
import java.util.List;

/**
 * Elección del equipo del jugador: hay que escoger exactamente 3 criaturas
 * de las disponibles. Cada tarjeta muestra el dibujo, el tipo y las
 * estadísticas. Al completar 3, el botón lanza el combate.
 */
public class SeleccionActivity extends Activity {

    private static final int EQUIPO = Fabrica.TAMANO_EQUIPO;

    private final List<String> seleccionadas = new ArrayList<>();
    private final List<LinearLayout> tarjetas = new ArrayList<>();
    private String[] especies;
    private TextView contador;
    private Button botonCombatir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        especies = Fabrica.nombresEspecies();

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(0xFF20232A);

        // Título
        TextView titulo = new TextView(this);
        titulo.setText("Elige tu equipo");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(24f);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setPadding(dp(20), dp(20), dp(20), dp(2));
        raiz.addView(titulo);

        contador = new TextView(this);
        contador.setTextColor(0xFFB6C0CC);
        contador.setTextSize(15f);
        contador.setPadding(dp(20), 0, dp(20), dp(12));
        raiz.addView(contador);

        // Lista de tarjetas (scroll)
        ScrollView scroll = new ScrollView(this);
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(dp(12), 0, dp(12), dp(12));
        for (int i = 0; i < especies.length; i++) {
            LinearLayout tarjeta = crearTarjeta(especies[i]);
            tarjetas.add(tarjeta);
            final int indice = i;
            tarjeta.setOnClickListener(v -> alternar(indice));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp(6), 0, dp(6));
            lista.addView(tarjeta, lp);
        }
        scroll.addView(lista);
        raiz.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Botón combatir
        botonCombatir = new Button(this);
        botonCombatir.setText("¡Combatir!");
        botonCombatir.setAllCaps(false);
        botonCombatir.setTextColor(Color.WHITE);
        botonCombatir.setTextSize(18f);
        botonCombatir.setOnClickListener(v -> combatir());
        LinearLayout.LayoutParams lpBoton = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        lpBoton.setMargins(dp(12), dp(6), dp(12), dp(12));
        raiz.addView(botonCombatir, lpBoton);

        setContentView(raiz);
        refrescar();
    }

    private LinearLayout crearTarjeta(String especie) {
        Criatura c = Fabrica.crear(especie);

        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.HORIZONTAL);
        tarjeta.setGravity(Gravity.CENTER_VERTICAL);
        tarjeta.setPadding(dp(10), dp(10), dp(12), dp(10));

        // Dibujo de la criatura
        VistaCriatura vc = new VistaCriatura(this);
        vc.setColor(c.tipo.color);
        tarjeta.addView(vc, new LinearLayout.LayoutParams(dp(64), dp(64)));

        // Columna de texto
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpCol = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpCol.setMargins(dp(12), 0, 0, 0);

        TextView nombre = new TextView(this);
        nombre.setText(c.nombre);
        nombre.setTextColor(Color.WHITE);
        nombre.setTextSize(19f);
        nombre.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(nombre);

        TextView tipo = new TextView(this);
        tipo.setText("Tipo " + c.tipo.nombre);
        tipo.setTextColor(c.tipo.color);
        tipo.setTextSize(14f);
        tipo.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(tipo);

        TextView stats = new TextView(this);
        stats.setText("PS " + c.hpMax + " · ATQ " + c.ataque
                + " · DEF " + c.defensa + " · VEL " + c.velocidad);
        stats.setTextColor(0xFFCBD2D9);
        stats.setTextSize(13f);
        col.addView(stats);

        TextView movs = new TextView(this);
        movs.setText(listaMovimientos(c));
        movs.setTextColor(0xFF8A93A0);
        movs.setTextSize(12f);
        col.addView(movs);

        tarjeta.addView(col, lpCol);
        return tarjeta;
    }

    private String listaMovimientos(Criatura c) {
        StringBuilder sb = new StringBuilder();
        for (Movimiento m : c.movimientos) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(m.nombre);
        }
        return sb.toString();
    }

    private void alternar(int indice) {
        String especie = especies[indice];
        if (seleccionadas.contains(especie)) {
            seleccionadas.remove(especie);
        } else if (seleccionadas.size() < EQUIPO) {
            seleccionadas.add(especie);
        } else {
            Toast.makeText(this, "Ya tienes " + EQUIPO + " elegidas. Quita una primero.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        refrescar();
    }

    private void refrescar() {
        for (int i = 0; i < tarjetas.size(); i++) {
            boolean sel = seleccionadas.contains(especies[i]);
            GradientDrawable fondo = new GradientDrawable();
            fondo.setCornerRadius(dp(14));
            if (sel) {
                fondo.setColor(0xFF2E3B31);
                fondo.setStroke(dp(3), 0xFF34C759);
            } else {
                fondo.setColor(0xFF2B3038);
                fondo.setStroke(dp(1), 0xFF3A4149);
            }
            tarjetas.get(i).setBackground(fondo);
        }
        contador.setText("Seleccionadas: " + seleccionadas.size() + " / " + EQUIPO);
        boolean listo = seleccionadas.size() == EQUIPO;
        botonCombatir.setEnabled(listo);
        botonCombatir.setAlpha(listo ? 1f : 0.5f);
        GradientDrawable fb = new GradientDrawable();
        fb.setColor(listo ? 0xFF34A853 : 0xFF3A4149);
        fb.setCornerRadius(dp(14));
        botonCombatir.setBackground(fb);
    }

    private void combatir() {
        if (seleccionadas.size() != EQUIPO) return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_EQUIPO,
                seleccionadas.toArray(new String[0]));
        startActivity(intent);
    }

    private int dp(float valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}
