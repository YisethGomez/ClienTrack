package co.edu.ean.clientrack.util;

import android.content.Context;
import android.content.SharedPreferences;

// Guarda la sesión del usuario en SharedPreferences
public class SessionManager {

    private static final String PREFS = "clientrack_sesion";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_USUARIO_NOMBRE = "usuario_nombre";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void iniciarSesion(long usuarioId, String nombre) {
        prefs.edit()
                .putLong(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_USUARIO_NOMBRE, nombre)
                .apply();
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public boolean haySesion() {
        return getUsuarioId() != -1;
    }

    public long getUsuarioId() {
        return prefs.getLong(KEY_USUARIO_ID, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_USUARIO_NOMBRE, "");
    }

    /** Devuelve solo el primer nombre, para el saludo. */
    public String getPrimerNombre() {
        String nombre = getNombre().trim();
        int espacio = nombre.indexOf(' ');
        return espacio > 0 ? nombre.substring(0, espacio) : nombre;
    }
}
