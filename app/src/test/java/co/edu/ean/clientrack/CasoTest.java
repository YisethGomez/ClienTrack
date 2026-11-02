package co.edu.ean.clientrack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

import co.edu.ean.clientrack.model.Caso;

/** Pruebas de la regla "un caso está vencido si no está cerrado y su fecha límite ya pasó". */
public class CasoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 1);

    private Caso caso(String estado, String fechaLimite) {
        Caso c = new Caso();
        c.setEstado(estado);
        c.setFechaLimite(fechaLimite);
        return c;
    }

    @Test
    public void abiertoConFechaPasada_estaVencido() {
        assertTrue(caso(Caso.ESTADO_ABIERTO, "2026-10-31").isVencido(HOY));
        assertTrue(caso(Caso.ESTADO_EN_PROCESO, "2025-01-15").isVencido(HOY));
    }

    @Test
    public void venceHoyOMasAdelante_noEstaVencido() {
        assertFalse(caso(Caso.ESTADO_ABIERTO, "2026-11-01").isVencido(HOY));
        assertFalse(caso(Caso.ESTADO_ABIERTO, "2026-12-24").isVencido(HOY));
    }

    @Test
    public void cerrado_nuncaEstaVencido() {
        assertFalse(caso(Caso.ESTADO_CERRADO, "2020-01-01").isVencido(HOY));
    }

    @Test
    public void sinFecha_noEstaVencido() {
        assertFalse(caso(Caso.ESTADO_ABIERTO, null).isVencido(HOY));
        assertFalse(caso(Caso.ESTADO_ABIERTO, "").isVencido(HOY));
    }

    @Test
    public void listasDeValores_coincidenConLasConstantes() {
        assertTrue(java.util.Arrays.asList(Caso.PRIORIDADES)
                .containsAll(java.util.Arrays.asList(Caso.PRIORIDAD_ALTA, Caso.PRIORIDAD_MEDIA, Caso.PRIORIDAD_BAJA)));
        assertTrue(java.util.Arrays.asList(Caso.ESTADOS)
                .containsAll(java.util.Arrays.asList(Caso.ESTADO_ABIERTO, Caso.ESTADO_EN_PROCESO, Caso.ESTADO_CERRADO)));
    }
}
