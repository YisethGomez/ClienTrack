package co.edu.ean.clientrack.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.model.Caso;

// Funciones de apoyo para la interfaz: colores de etiquetas y mensajes
public final class UiUtils {

    /** Mensaje que una pantalla le devuelve a la anterior para mostrarlo en un Snackbar. */
    public static final String EXTRA_MENSAJE = "mensaje";

    private UiUtils() { }

    // Colores

    @ColorInt
    public static int colorPrioridad(Context context, String prioridad) {
        int color;
        if (Caso.PRIORIDAD_ALTA.equals(prioridad)) {
            color = R.color.prioridad_alta;
        } else if (Caso.PRIORIDAD_BAJA.equals(prioridad)) {
            color = R.color.prioridad_baja;
        } else {
            color = R.color.prioridad_media;
        }
        return ContextCompat.getColor(context, color);
    }

    @ColorInt
    public static int colorTextoEstado(Context context, String estado) {
        int color;
        if (Caso.ESTADO_CERRADO.equals(estado)) {
            color = R.color.estado_cerrado;
        } else if (Caso.ESTADO_EN_PROCESO.equals(estado)) {
            color = R.color.estado_proceso;
        } else {
            color = R.color.estado_abierto;
        }
        return ContextCompat.getColor(context, color);
    }

    @ColorInt
    public static int colorFondoEstado(Context context, String estado) {
        int color;
        if (Caso.ESTADO_CERRADO.equals(estado)) {
            color = R.color.estado_cerrado_bg;
        } else if (Caso.ESTADO_EN_PROCESO.equals(estado)) {
            color = R.color.estado_proceso_bg;
        } else {
            color = R.color.estado_abierto_bg;
        }
        return ContextCompat.getColor(context, color);
    }

    /** Pinta una etiqueta tonal: fondo claro y texto oscuro del mismo color. */
    public static void pintarEtiqueta(TextView etiqueta, String texto, @ColorInt int colorTexto,
                                      @ColorInt int colorFondo) {
        etiqueta.setText(texto);
        etiqueta.setTextColor(colorTexto);
        etiqueta.setBackgroundTintList(ColorStateList.valueOf(colorFondo));
    }

    /** Pinta la etiqueta de estado de un caso. */
    public static void pintarEstado(TextView etiqueta, String estado) {
        Context context = etiqueta.getContext();
        pintarEtiqueta(etiqueta, estado, colorTextoEstado(context, estado), colorFondoEstado(context, estado));
    }

    /** Pinta la etiqueta "Vencido" (rojo sobre rojo muy claro). */
    public static void pintarVencido(TextView etiqueta) {
        Context context = etiqueta.getContext();
        etiqueta.setTextColor(ContextCompat.getColor(context, R.color.error));
        etiqueta.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error_container)));
    }

    /** Colorea el punto de prioridad. */
    public static void pintarPunto(View punto, @ColorInt int color) {
        punto.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    // Textos

    /** Muestra "No registrado" cuando un dato opcional está vacío. */
    public static String valorOpcional(Context context, String valor) {
        return Formatos.estaVacio(valor) ? context.getString(R.string.no_registrado) : valor;
    }

    // Mensajes

    public static void mostrarMensaje(@NonNull View raiz, @Nullable String mensaje, @Nullable View ancla) {
        if (Formatos.estaVacio(mensaje)) {
            return;
        }
        Snackbar snackbar = Snackbar.make(raiz, mensaje, Snackbar.LENGTH_SHORT);
        if (ancla != null && ancla.getVisibility() == View.VISIBLE) {
            snackbar.setAnchorView(ancla);
        }
        snackbar.show();
    }

    public static void mostrarMensaje(@NonNull View raiz, @StringRes int mensaje, @Nullable View ancla) {
        mostrarMensaje(raiz, raiz.getContext().getString(mensaje), ancla);
    }

    /** Cierra la pantalla y le deja un mensaje a la anterior. */
    public static void terminarConMensaje(@NonNull Activity activity, @StringRes int mensaje) {
        Intent datos = new Intent();
        datos.putExtra(EXTRA_MENSAJE, activity.getString(mensaje));
        activity.setResult(Activity.RESULT_OK, datos);
        activity.finish();
    }
}
