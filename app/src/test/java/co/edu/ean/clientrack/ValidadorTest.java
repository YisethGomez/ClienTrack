package co.edu.ean.clientrack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.edu.ean.clientrack.util.Validador;

/** Pruebas de las reglas de validación de los formularios. */
public class ValidadorTest {

    @Test
    public void correo_validos() {
        assertTrue(Validador.esCorreoValido("carlos@correo.com"));
        assertTrue(Validador.esCorreoValido("ana.maria+crm@empresa.com.co"));
        assertTrue(Validador.esCorreoValido("  con.espacios@correo.co  "));
    }

    @Test
    public void correo_invalidos() {
        assertFalse(Validador.esCorreoValido(null));
        assertFalse(Validador.esCorreoValido(""));
        assertFalse(Validador.esCorreoValido("   "));
        assertFalse(Validador.esCorreoValido("sinarroba.com"));
        assertFalse(Validador.esCorreoValido("falta@dominio"));
        assertFalse(Validador.esCorreoValido("dos@@arrobas.com"));
        assertFalse(Validador.esCorreoValido("con espacio@correo.com"));
    }

    @Test
    public void telefono_entre7y10Digitos() {
        assertTrue(Validador.esTelefonoValido("2801234"));
        assertTrue(Validador.esTelefonoValido("3004521187"));
        assertFalse(Validador.esTelefonoValido("123456"));
        assertFalse(Validador.esTelefonoValido("30045211870"));
        assertFalse(Validador.esTelefonoValido("300-452-11"));
        assertFalse(Validador.esTelefonoValido("+573004521"));
        assertFalse(Validador.esTelefonoValido(null));
    }

    @Test
    public void nombre_soloLetrasMinimo3() {
        assertTrue(Validador.esNombreValido("Ana"));
        assertTrue(Validador.esNombreValido("María Fernanda López"));
        assertTrue(Validador.esNombreValido("Andrés Peña Núñez"));
        assertFalse(Validador.esNombreValido("Al"));
        assertFalse(Validador.esNombreValido("   "));
        assertFalse(Validador.esNombreValido("Juan2"));
        assertFalse(Validador.esNombreValido("O'Neil"));
        assertFalse(Validador.esNombreValido(null));
    }

    @Test
    public void asunto_minimo3Caracteres() {
        assertTrue(Validador.esAsuntoValido("abc"));
        assertTrue(Validador.esAsuntoValido("  Garantía  "));
        assertFalse(Validador.esAsuntoValido("ab"));
        assertFalse(Validador.esAsuntoValido("  a  "));
        assertFalse(Validador.esAsuntoValido(null));
    }

    @Test
    public void password_minimo6() {
        assertTrue(Validador.esPasswordValida("123456"));
        assertFalse(Validador.esPasswordValida("12345"));
        assertFalse(Validador.esPasswordValida(null));
    }

    @Test
    public void hash_esSha256Conocido() {
        // SHA-256 de "123456", valor de referencia público
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                Validador.hashPassword("123456"));
    }

    @Test
    public void hash_esDeterministaYDistinguePasswords() {
        assertEquals(Validador.hashPassword("clave1"), Validador.hashPassword("clave1"));
        assertNotEquals(Validador.hashPassword("clave1"), Validador.hashPassword("clave2"));
        assertEquals(64, Validador.hashPassword("ñandú").length());
    }
}
