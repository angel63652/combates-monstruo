package com.juego.combates.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

import com.juego.combates.batalla.Batalla;
import com.juego.combates.modelo.Criatura;

import java.util.List;

/**
 * Escenario del combate dibujado a mano con Canvas: cielo, hierba,
 * plataformas, las dos criaturas, paneles de vida y animación de golpe.
 */
public class VistaBatalla extends View {

    private static final long DURACION_GOLPE_MS = 450;

    private Batalla batalla;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();

    private Shader cielo;
    private long inicioGolpe = -1;
    private int objetivoGolpe = Batalla.Evento.NADA;

    public VistaBatalla(Context context) {
        super(context);
        paintTexto.setColor(0xFF20232A);
    }

    public void setBatalla(Batalla batalla) {
        this.batalla = batalla;
        invalidate();
    }

    /** Lanza la animación de sacudida sobre la criatura golpeada. */
    public void animarGolpe(int objetivo) {
        objetivoGolpe = objetivo;
        inicioGolpe = SystemClock.uptimeMillis();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cielo = new LinearGradient(0, 0, 0, h * 0.55f,
                0xFF8ED0F5, 0xFFD9F0FB, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // Fondo: cielo y hierba
        paint.setShader(cielo);
        canvas.drawRect(0, 0, w, h * 0.55f, paint);
        paint.setShader(null);
        paint.setColor(0xFF7BBF6A);
        canvas.drawRect(0, h * 0.55f, w, h, paint);
        paint.setColor(0xFF6AAE59);
        canvas.drawRect(0, h * 0.55f, w, h * 0.60f, paint);

        if (batalla == null) return;

        long transcurrido = (inicioGolpe < 0) ? Long.MAX_VALUE
                : SystemClock.uptimeMillis() - inicioGolpe;
        boolean animando = transcurrido < DURACION_GOLPE_MS;

        float sacudidaRival = 0f;
        float sacudidaJugador = 0f;
        if (animando) {
            float amplitud = w * 0.02f * (1f - (float) transcurrido / DURACION_GOLPE_MS);
            float onda = (float) Math.sin(transcurrido / 28.0) * amplitud;
            if (objetivoGolpe == Batalla.Evento.GOLPE_RIVAL) sacudidaRival = onda;
            if (objetivoGolpe == Batalla.Evento.GOLPE_JUGADOR) sacudidaJugador = onda;
        }

        Criatura rival = batalla.activaRival;
        Criatura jugador = batalla.activaJugador;

        // Plataformas
        paint.setColor(0x445B4A32);
        rect.set(w * 0.55f, h * 0.40f, w * 0.92f, h * 0.48f);
        canvas.drawOval(rect, paint);
        rect.set(w * 0.06f, h * 0.78f, w * 0.52f, h * 0.90f);
        canvas.drawOval(rect, paint);

        // Criaturas
        if (rival != null && !rival.estaDebilitada()) {
            dibujarCriatura(canvas, rival,
                    w * 0.735f + sacudidaRival, h * 0.32f, Math.min(w, h) * 0.115f,
                    animando && objetivoGolpe == Batalla.Evento.GOLPE_RIVAL);
        }
        if (jugador != null && !jugador.estaDebilitada()) {
            dibujarCriatura(canvas, jugador,
                    w * 0.29f + sacudidaJugador, h * 0.66f, Math.min(w, h) * 0.15f,
                    animando && objetivoGolpe == Batalla.Evento.GOLPE_JUGADOR);
        }

        // Paneles de información
        if (rival != null) {
            dibujarPanel(canvas, rival, batalla.equipoRival,
                    w * 0.04f, h * 0.05f, w * 0.5f, false);
        }
        if (jugador != null) {
            dibujarPanel(canvas, jugador, batalla.equipoJugador,
                    w * 0.46f, h * 0.72f, w * 0.5f, true);
        }

        if (animando) {
            postInvalidateOnAnimation();
        }
    }

    private void dibujarCriatura(Canvas canvas, Criatura c, float cx, float cy,
                                 float r, boolean golpeada) {
        int color = c.tipo.color;

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

    private void dibujarPanel(Canvas canvas, Criatura c, List<Criatura> equipo,
                              float x, float y, float ancho, boolean mostrarNumeros) {
        float alto = ancho * 0.42f;
        float radio = alto * 0.18f;

        paint.setColor(0xF2FFFFFF);
        rect.set(x, y, x + ancho, y + alto);
        canvas.drawRoundRect(rect, radio, radio, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(alto * 0.04f);
        paint.setColor(0xFF3A4149);
        canvas.drawRoundRect(rect, radio, radio, paint);
        paint.setStyle(Paint.Style.FILL);

        // Nombre y nivel
        paintTexto.setTextSize(alto * 0.28f);
        paintTexto.setFakeBoldText(true);
        canvas.drawText(c.nombre, x + ancho * 0.06f, y + alto * 0.32f, paintTexto);
        paintTexto.setFakeBoldText(false);
        paintTexto.setTextSize(alto * 0.22f);
        String nivel = "Nv " + c.nivel;
        float anchoNivel = paintTexto.measureText(nivel);
        canvas.drawText(nivel, x + ancho * 0.94f - anchoNivel, y + alto * 0.32f, paintTexto);

        // Barra de vida
        float bx = x + ancho * 0.06f;
        float by = y + alto * 0.45f;
        float bAncho = ancho * 0.88f;
        float bAlto = alto * 0.16f;
        paint.setColor(0xFFCBD2D9);
        rect.set(bx, by, bx + bAncho, by + bAlto);
        canvas.drawRoundRect(rect, bAlto / 2, bAlto / 2, paint);

        float f = c.fraccionVida();
        if (f > 0f) {
            paint.setColor(f > 0.5f ? 0xFF34C759 : (f > 0.2f ? 0xFFF0A500 : 0xFFE0442E));
            rect.set(bx, by, bx + bAncho * f, by + bAlto);
            canvas.drawRoundRect(rect, bAlto / 2, bAlto / 2, paint);
        }

        // PS numéricos (solo panel del jugador)
        float baseInferior = y + alto * 0.88f;
        if (mostrarNumeros) {
            paintTexto.setTextSize(alto * 0.22f);
            String ps = c.hp + " / " + c.hpMax + " PS";
            float anchoPs = paintTexto.measureText(ps);
            canvas.drawText(ps, x + ancho * 0.94f - anchoPs, baseInferior, paintTexto);
        }

        // Puntos del equipo (criaturas restantes)
        float pr = alto * 0.07f;
        float px = x + ancho * 0.06f + pr;
        float py = baseInferior - pr;
        for (Criatura miembro : equipo) {
            if (miembro.estaDebilitada()) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(pr * 0.5f);
                paint.setColor(0xFF9AA0A6);
                canvas.drawCircle(px, py, pr, paint);
                paint.setStyle(Paint.Style.FILL);
            } else {
                paint.setColor(0xFFE0442E);
                canvas.drawCircle(px, py, pr, paint);
            }
            px += pr * 3.2f;
        }
    }

    private static int oscurecer(int color) {
        return Color.rgb(
                (int) (Color.red(color) * 0.62f),
                (int) (Color.green(color) * 0.62f),
                (int) (Color.blue(color) * 0.62f));
    }

    private static int aclarar(int color) {
        return Color.rgb(
                Math.min(255, (int) (Color.red(color) * 0.5f) + 128),
                Math.min(255, (int) (Color.green(color) * 0.5f) + 128),
                Math.min(255, (int) (Color.blue(color) * 0.5f) + 128));
    }
}
