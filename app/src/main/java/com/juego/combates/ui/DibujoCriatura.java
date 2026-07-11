package com.juego.combates.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Dibujo de una criatura (cuerpo redondo con orejas, barriga y ojos) reutilizable
 * por la vista de combate y por las tarjetas de selección de equipo.
 */
public final class DibujoCriatura {

    private DibujoCriatura() {
    }

    /**
     * Dibuja una criatura del color dado, centrada en (cx, cy) y de radio r.
     * Reutiliza el Paint y el Path que se le pasan para no crear objetos por frame.
     */
    public static void dibujar(Canvas canvas, Paint paint, Path path,
                               float cx, float cy, float r, int color, boolean golpeada) {
        // Orejas
        paint.setColor(oscurecer(color));
        path.reset();
        path.moveTo(cx - r * 0.7f, cy - r * 0.5f);
        path.lineTo(cx - r * 0.45f, cy - r * 1.35f);
        path.lineTo(cx - r * 0.15f, cy - r * 0.7f);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(cx + r * 0.7f, cy - r * 0.5f);
        path.lineTo(cx + r * 0.45f, cy - r * 1.35f);
        path.lineTo(cx + r * 0.15f, cy - r * 0.7f);
        path.close();
        canvas.drawPath(path, paint);

        // Cuerpo y barriga
        paint.setColor(color);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setColor(aclarar(color));
        canvas.drawCircle(cx, cy + r * 0.25f, r * 0.55f, paint);

        // Contorno
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(r * 0.08f);
        paint.setColor(oscurecer(color));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);

        // Ojos
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx - r * 0.32f, cy - r * 0.2f, r * 0.18f, paint);
        canvas.drawCircle(cx + r * 0.32f, cy - r * 0.2f, r * 0.18f, paint);
        paint.setColor(0xFF20232A);
        canvas.drawCircle(cx - r * 0.32f, cy - r * 0.2f, r * 0.08f, paint);
        canvas.drawCircle(cx + r * 0.32f, cy - r * 0.2f, r * 0.08f, paint);

        // Destello rojo al recibir daño
        if (golpeada) {
            paint.setColor(0x66FF2A2A);
            canvas.drawCircle(cx, cy, r * 1.1f, paint);
        }
    }

    public static int oscurecer(int color) {
        return Color.rgb(
                (int) (Color.red(color) * 0.62f),
                (int) (Color.green(color) * 0.62f),
                (int) (Color.blue(color) * 0.62f));
    }

    public static int aclarar(int color) {
        return Color.rgb(
                Math.min(255, (int) (Color.red(color) * 0.5f) + 128),
                Math.min(255, (int) (Color.green(color) * 0.5f) + 128),
                Math.min(255, (int) (Color.blue(color) * 0.5f) + 128));
    }
}
