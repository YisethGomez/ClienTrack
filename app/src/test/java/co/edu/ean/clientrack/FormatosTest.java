package co.edu.ean.clientrack;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

import co.edu.ean.clientrack.util.Formatos;

/** Pruebas de los formatos de fecha y texto que se muestran en pantalla. */
public class FormatosTest {

    @Test
    public void fechas_formatosEnEspanol() {
        assertEquals("28 oct", Formatos.fechaCorta("2026-10-28"));
        assertEquals("28 oct 2026", Formatos.fechaMedia("2026-10-28"));
        assertEquals("28 de octubre de 2026", Formatos.fechaLarga("2026-10-28"));
        assertEquals("1 de enero de 2027", Formatos.fechaLarga("2027-01-01"));
    }

    @Test
    public void fechas_valoresVaciosOInvalidosNoRompen() {
        assertEquals("", Formatos.fechaCorta(null));
        assertEquals("", Formatos.fechaCorta(""));
        assertEquals("", Formatos.fechaMedia("   "));
        assertEquals("no-es-fecha", Formatos.fechaLarga("no-es-fecha"));
        assertEquals("2026-13-40", Formatos.fechaCorta("2026-13-40"));
    }

    @Test
    public void calendario_conversionUtcIdaYVuelta() {
        assertEquals(1767225600000L, Formatos.aMilisUtc(LocalDate.of(2026, 1, 1)));
        assertEquals("2026-01-01", Formatos.desdeMilisUtc(1767225600000L));
        String[] fechas = {"2026-02-28", "2028-02-29", "2026-11-01", "2026-12-31", "2030-06-15"};
        for (String f : fechas) {
            assertEquals(f, Formatos.desdeMilisUtc(Formatos.aMilisUtc(LocalDate.parse(f))));
        }
    }

    @Test
    public void iniciales_maximoDosLetras() {
        assertEquals("MF", Formatos.iniciales("María Fernanda López"));
        assertEquals("AP", Formatos.iniciales("  andrés   peña "));
        assertEquals("C", Formatos.iniciales("Clínica"));
        assertEquals("", Formatos.iniciales(""));
        assertEquals("", Formatos.iniciales(null));
    }

    @Test
    public void normalizar_quitaTildesYMayusculas() {
        assertEquals("garantia nandu", Formatos.normalizar("  Garantía ÑANDÚ "));
        assertEquals("clinica san rafael", Formatos.normalizar("Clínica San Rafael"));
        assertEquals("", Formatos.normalizar(null));
    }

    @Test
    public void escaparLike_comodinesDeSqlite() {
        assertEquals("50\\%", Formatos.escaparLike("50%"));
        assertEquals("a\\_b", Formatos.escaparLike("a_b"));
        assertEquals("c\\\\d", Formatos.escaparLike("c\\d"));
        assertEquals("normal", Formatos.escaparLike("normal"));
        assertEquals("", Formatos.escaparLike(null));
    }
}
