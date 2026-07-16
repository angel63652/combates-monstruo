package com.juego.combates.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Muestra una sola criatura del color indicado, centrada y ajustada al tamaño
 * de la vista. Se usa en el menú y en las tarjetas de selección de equipo.
 */
public class VistaCriatura extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int color = 0xFF9AA0A6;

    public VistaCriatura(Context context) {
        super(context);
    }

    /** Constructor usado al inflar la vista desde un layout XML. */
    public VistaCriatura(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;
        float cx = w * 0.5f;
        // Un poco por debajo del centro para dejar hueco a las orejas arriba.
        float cy = h * 0.56f;
        float r = Math.min(w, h) * 0.34f;
        DibujoCriatura.dibujar(canvas, paint, path, cx, cy, r, color, false);
    }
}
