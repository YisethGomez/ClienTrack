package co.edu.ean.clientrack.util;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// Formatos de fechas y textos que se muestran en pantalla
public final class Formatos {

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    /** "28 oct" (listas) */
    private static final DateTimeFormatter CORTA = DateTimeFormatter.ofPattern("d MMM", ES);
    /** "28 oct 2026" (detalle) */
    private static final DateTimeFormatter MEDIA = DateTimeFormatter.ofPattern("d MMM yyyy", ES);
    /** "28 de octubre de 2026" (formulario) */
    private static final DateTimeFormatter LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES);

    private Formatos() { }

    public static String fechaCorta(String fechaIso) {
        return formatear(fechaIso, CORTA);
    }

    public static String fechaMedia(String fechaIso) {
        return formatear(fechaIso, MEDIA);
    }

    public static String fechaLarga(String fechaIso) {
        return formatear(fechaIso, LARGA);
    }

    // yyyy-MM-dd -> formato de pantalla. Si la fecha viene mal se devuelve igual
    private static String formatear(String fechaIso, DateTimeFormatter formato) {
        if (fechaIso == null || fechaIso.trim().isEmpty()) {
            return "";
        }
        try {
            return LocalDate.parse(fechaIso.trim()).format(formato).replace(".", "");
        } catch (DateTimeParseException e) {
            return fechaIso;
        }
    }

    // El DatePicker de Material usa milisegundos en UTC. Se convierte en UTC para que no se corra un día
    public static long aMilisUtc(LocalDate fecha) {
        return fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static String desdeMilisUtc(long milis) {
        return Instant.ofEpochMilli(milis).atZone(ZoneOffset.UTC).toLocalDate().toString();
    }

    /** "María Fernanda López" -> "MF". Máximo dos letras. */
    public static String iniciales(String nombre) {
        if (nombre == null) {
            return "";
        }
        StringBuilder iniciales = new StringBuilder();
        for (String parte : nombre.trim().split("\\s+")) {
            if (!parte.isEmpty() && iniciales.length() < 2) {
                iniciales.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return iniciales.toString();
    }

    /** Pasa a minúsculas y quita las tildes, para buscar "garantia" y encontrar "Garantía". */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }

    // escapa % y _ para el LIKE (se usa con ESCAPE '\')
    public static String escaparLike(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
