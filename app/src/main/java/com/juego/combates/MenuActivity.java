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
import android.widget.TextView;

import com.juego.combates.modelo.Fabrica;
import com.juego.combates.modelo.Criatura;
import com.juego.combates.ui.VistaCriatura;

/**
 * Pantalla de inicio: título, unas criaturas de adorno y los botones para
 * elegir equipo o lanzar un combate rápido con equipo al azar.
 */
public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setGravity(Gravity.CENTER_HORIZONTAL);
        raiz.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF2B3A67, 0xFF20232A}));
        int pad = dp(24);
        raiz.setPadding(pad, dp(48), pad, dp(32));

        // Título
        TextView titulo = new TextView(this);
        titulo.setText("Combates Monstruo");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(34f);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(Gravity.CENTER);
        raiz.addView(titulo);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Combates por turnos");
        subtitulo.setTextColor(0xFFB6C0CC);
        subtitulo.setTextSize(16f);
        subtitulo.setGravity(Gravity.CENTER);
        subtitulo.setPadding(0, dp(6), 0, dp(28));
        raiz.addView(subtitulo);

        // Fila de criaturas de adorno
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER);
        for (String especie : Fabrica.nombresEspecies()) {
            Criatura c = Fabrica.crear(especie);
            VistaCriatura vc = new VistaCriatura(this);
            vc.setColor(c.tipo.color);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(52), dp(52));
            lp.setMargins(dp(3), 0, dp(3), 0);
            fila.addView(vc, lp);
        }
        LinearLayout.LayoutParams lpFila = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        lpFila.setMargins(0, 0, 0, dp(40));
        raiz.addView(fila, lpFila);

        // Botón: elegir equipo
        Button botonElegir = botonGrande("Elegir equipo y combatir", 0xFF34A853);
        botonElegir.setOnClickListener(v ->
                startActivity(new Intent(this, SeleccionActivity.class)));
        raiz.addView(botonElegir, paramsBoton());

        // Botón: combate rápido (equipo al azar)
        Button botonRapido = botonGrande("Combate rápido (equipo al azar)", 0xFF5865F2);
        botonRapido.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        raiz.addView(botonRapido, paramsBoton());

        // Empujar el pie hacia abajo
        View espacio = new View(this);
        raiz.addView(espacio, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView pie = new TextView(this);
        pie.setText("Java puro · sin dependencias");
        pie.setTextColor(0xFF7A8290);
        pie.setTextSize(13f);
        pie.setGravity(Gravity.CENTER);
        raiz.addView(pie);

        setContentView(raiz);
    }

    private Button botonGrande(String texto, int color) {
        Button b = new Button(this);
        b.setText(texto);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(17f);
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(dp(14));
        b.setBackground(fondo);
        return b;
    }

    private LinearLayout.LayoutParams paramsBoton() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        lp.setMargins(dp(8), dp(8), dp(8), dp(8));
        return lp;
    }

    private int dp(float valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}
