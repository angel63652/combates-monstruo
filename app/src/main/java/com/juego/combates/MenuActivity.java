package com.juego.combates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.juego.combates.modelo.Fabrica;
import com.juego.combates.ui.VistaCriatura;

/**
 * Pantalla de inicio. La disposición está en res/layout/activity_menu.xml
 * (editable en el diseñador). Aquí solo se colorean las criaturas de adorno
 * y se enganchan los botones.
 */
public class MenuActivity extends Activity {

    private static final int[] IDS_CRIATURA = {
            R.id.criatura_0, R.id.criatura_1, R.id.criatura_2,
            R.id.criatura_3, R.id.criatura_4, R.id.criatura_5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Pinta cada criatura de adorno con el color de su tipo.
        String[] especies = Fabrica.nombresEspecies();
        for (int i = 0; i < IDS_CRIATURA.length; i++) {
            VistaCriatura vc = findViewById(IDS_CRIATURA[i]);
            if (vc != null && i < especies.length) {
                vc.setColor(Fabrica.crear(especies[i]).tipo.color);
            }
        }

        findViewById(R.id.boton_elegir).setOnClickListener(v ->
                startActivity(new Intent(this, SeleccionActivity.class)));
        findViewById(R.id.boton_rapido).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
    }
}
