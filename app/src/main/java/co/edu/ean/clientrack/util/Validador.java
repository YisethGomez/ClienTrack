package co.edu.ean.clientrack.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

// Validaciones de los formularios y hash de contraseñas
public final class Validador {

    public static final int MIN_PASSWORD = 6;

    // Mismo patrón de Patterns.EMAIL_ADDRESS (así se puede probar sin Android)
    private static final Pattern CORREO = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}"
                    + "@"
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
                    + "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");

    private static final Pattern TELEFONO = Pattern.compile("\\d{7,10}");

    /** Solo letras (con tildes y ñ) y espacios, entre 3 y 60 caracteres. */
    private static final Pattern NOMBRE = Pattern.compile("[\\p{L} ]{3,60}");

    private Validador() { }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean esCorreoValido(String correo) {
        return !estaVacio(correo) && CORREO.matcher(correo.trim()).matches();
    }

    /** Teléfono colombiano: solo números, entre 7 (fijo) y 10 (celular) dígitos. */
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono.trim()).matches();
    }

    /** El nombre debe tener al menos 3 letras y solo letras y espacios. */
    public static boolean esNombreValido(String nombre) {
        return nombre != null && NOMBRE.matcher(nombre.trim()).matches();
    }

    public static boolean esAsuntoValido(String asunto) {
        return asunto != null && asunto.trim().length() >= 3;
    }

    public static boolean esPasswordValida(String password) {
        return password != null && password.length() >= MIN_PASSWORD;
    }

    // SHA-256 para no guardar la contraseña en texto plano
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
