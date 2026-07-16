package com.juego.combates;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.juego.combates.modelo.Criatura;
import com.juego.combates.modelo.Fabrica;
import com.juego.combates.modelo.Movimiento;
import com.juego.combates.ui.VistaCriatura;

import java.util.ArrayList;
import java.util.List;

/**
 * Elección del equipo del jugador (3 criaturas). La pantalla está en
 * res/layout/activity_seleccion.xml y cada tarjeta en item_criatura.xml.
 */
public class SeleccionActivity extends Activity {

    private static final int EQUIPO = Fabrica.TAMANO_EQUIPO;

    private final List<String> seleccionadas = new ArrayList<>();
    private final List<View> tarjetas = new ArrayList<>();
    private String[] especies;
    private TextView contador;
    private Button botonCombatir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion);
        especies = Fabrica.nombresEspecies();

        contador = findViewById(R.id.contador);
        botonCombatir = findViewById(R.id.boton_combatir);
        botonCombatir.setOnClickListener(v -> combatir());

        LinearLayout lista = findViewById(R.id.lista);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < especies.length; i++) {
            View tarjeta = inflater.inflate(R.layout.item_criatura, lista, false);
            rellenarTarjeta(tarjeta, Fabrica.crear(especies[i]));
            final int indice = i;
            tarjeta.setOnClickListener(v -> alternar(indice));
            lista.addView(tarjeta);
            tarjetas.add(tarjeta);
        }
        refrescar();
    }

    private void rellenarTarjeta(View tarjeta, Criatura c) {
        VistaCriatura vc = tarjeta.findViewById(R.id.vista_criatura);
        vc.setColor(c.tipo.color);

        TextView nombre = tarjeta.findViewById(R.id.nombre);
        nombre.setText(c.nombre);

        TextView tipo = tarjeta.findViewById(R.id.tipo);
        tipo.setText(getString(R.string.etiqueta_tipo, c.tipo.nombre));
        tipo.setTextColor(c.tipo.color);

        TextView stats = tarjeta.findViewById(R.id.stats);
        stats.setText(getString(R.string.etiqueta_stats,
                c.hpMax, c.ataque, c.defensa, c.velocidad));

        TextView movs = tarjeta.findViewById(R.id.movimientos);
        movs.setText(listaMovimientos(c));
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
            Toast.makeText(this, getString(R.string.seleccion_tope, EQUIPO),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        refrescar();
    }

    private void refrescar() {
        for (int i = 0; i < tarjetas.size(); i++) {
            boolean sel = seleccionadas.contains(especies[i]);
            tarjetas.get(i).setBackgroundResource(
                    sel ? R.drawable.tarjeta_seleccionada : R.drawable.tarjeta);
        }
        contador.setText(getString(R.string.seleccion_contador, seleccionadas.size(), EQUIPO));

        boolean listo = seleccionadas.size() == EQUIPO;
        botonCombatir.setEnabled(listo);
        botonCombatir.setAlpha(listo ? 1f : 0.5f);
        botonCombatir.setBackgroundTintList(ColorStateList.valueOf(
                getColor(listo ? R.color.boton_verde : R.color.boton_apagado)));
    }

    private void combatir() {
        if (seleccionadas.size() != EQUIPO) return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_EQUIPO, seleccionadas.toArray(new String[0]));
        startActivity(intent);
    }
}
