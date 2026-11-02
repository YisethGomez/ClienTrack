package co.edu.ean.clientrack;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.ean.clientrack.ui.LoginActivity;
import co.edu.ean.clientrack.ui.RegisterActivity;
import co.edu.ean.clientrack.ui.WelcomeActivity;
import co.edu.ean.clientrack.util.SessionManager;

/**
 * Pruebas de interfaz de punta a punta. Se ejecutan en un emulador o celular con:
 *   gradlew connectedDebugAndroidTest   (o con probar.bat, opción 2)
 * Cada prueba crea un usuario nuevo con un correo único, así no depende de datos previos.
 */
@RunWith(AndroidJUnit4.class)
public class FlujosTest {

    private static final String PASSWORD = "secreto1";

    @Before
    @After
    public void cerrarSesion() {
        Context context = ApplicationProvider.getApplicationContext();
        new SessionManager(context).cerrarSesion();
    }

    private String correoUnico() {
        return "prueba" + System.nanoTime() + "@test.co";
    }

    /** Registra un usuario nuevo e inicia sesión. Termina en la pantalla "Mis casos". */
    private void crearCuentaEIngresar(String correo) {
        ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.etNombre)).perform(replaceText("Usuario Prueba"));
        onView(withId(R.id.etCorreo)).perform(replaceText(correo));
        onView(withId(R.id.etPassword)).perform(replaceText(PASSWORD));
        onView(withId(R.id.etConfirmar)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());

        // Vuelve al login con el correo ya escrito
        onView(withId(R.id.etCorreo)).check(matches(withText(correo)));
        onView(withId(R.id.etPassword)).perform(replaceText(PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.btnIngresar)).perform(scrollTo(), click());

        onView(withText(R.string.main_title)).check(matches(isDisplayed()));
    }

    private void agregarCliente(String nombre) {
        onView(withId(R.id.btnClientes)).perform(click());
        onView(withId(R.id.fabNuevoCliente)).perform(click());
        onView(withId(R.id.etNombre)).perform(replaceText(nombre));
        onView(withId(R.id.etEmpresa)).perform(replaceText("Frío Norte"), closeSoftKeyboard());
        onView(withId(R.id.btnGuardarCliente)).perform(click());
        onView(withText(nombre)).check(matches(isDisplayed()));
        pressBack(); // vuelve a "Mis casos"
    }

    @Test
    public void bienvenida_muestraAccionesPrincipales() {
        ActivityScenario.launch(WelcomeActivity.class);
        onView(withId(R.id.btnComenzar)).check(matches(isDisplayed()));
        onView(withId(R.id.btnComenzar)).check(matches(withText(R.string.welcome_start)));
        onView(withId(R.id.tvIrLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void login_camposVacios_muestraErrores() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.btnIngresar)).perform(click());
        onView(withText(R.string.err_email_vacio)).check(matches(isDisplayed()));
        onView(withText(R.string.err_password_vacio)).check(matches(isDisplayed()));
    }

    @Test
    public void login_credencialesIncorrectas_muestraError() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.etCorreo)).perform(replaceText(correoUnico()));
        onView(withId(R.id.etPassword)).perform(replaceText("noexiste"), closeSoftKeyboard());
        onView(withId(R.id.btnIngresar)).perform(click());
        onView(withText(R.string.err_credenciales)).check(matches(isDisplayed()));
    }

    @Test
    public void registro_contrasenasDistintas_muestraError() {
        ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.etNombre)).perform(replaceText("Usuario Prueba"));
        onView(withId(R.id.etCorreo)).perform(replaceText(correoUnico()));
        onView(withId(R.id.etPassword)).perform(replaceText(PASSWORD));
        onView(withId(R.id.etConfirmar)).perform(replaceText("otra123"), closeSoftKeyboard());
        onView(withId(R.id.btnRegistrar)).perform(scrollTo(), click());
        onView(withText(R.string.err_password_no_coincide)).check(matches(isDisplayed()));
    }

    @Test
    public void usuarioNuevo_veEstadoVacioYPideClienteAntesDeCrearCaso() {
        crearCuentaEIngresar(correoUnico());
        onView(withText(R.string.empty_cases_title)).check(matches(isDisplayed()));

        onView(withId(R.id.fabNuevoCaso)).perform(click());
        onView(withText(R.string.dlg_primer_cliente_titulo)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).inRoot(isDialog()).perform(click());
    }

    @Test
    public void formularioCaso_sinDatos_muestraErrores() {
        crearCuentaEIngresar(correoUnico());
        agregarCliente("Andrés Peña");

        onView(withId(R.id.fabNuevoCaso)).perform(click());
        onView(withId(R.id.btnGuardar)).perform(click());
        onView(withText(R.string.err_asunto_vacio)).check(matches(isDisplayed()));
        onView(withText(R.string.err_fecha)).check(matches(isDisplayed()));
    }

    @Test
    public void caso_crearCerrarYEliminar() {
        crearCuentaEIngresar(correoUnico());
        agregarCliente("Lucía Mendoza");

        // Crear
        onView(withId(R.id.fabNuevoCaso)).perform(click());
        onView(withId(R.id.etAsunto)).perform(replaceText("Cotización de luminarias"), closeSoftKeyboard());
        onView(withId(R.id.btnPrioAlta)).perform(click());
        onView(withId(R.id.etFecha)).perform(click());
        onView(withText(R.string.date_picker_ok)).inRoot(isDialog()).perform(click()); // hoy por defecto
        onView(withId(R.id.btnGuardar)).perform(click());

        // Vuelve a la lista con el caso nuevo
        onView(withText("Cotización de luminarias")).check(matches(isDisplayed()));

        // Detalle y cerrar
        onView(withText("Cotización de luminarias")).perform(click());
        onView(withId(R.id.tvAsunto)).check(matches(withText("Cotización de luminarias")));
        onView(withId(R.id.btnCerrarCaso)).perform(click());
        onView(withId(R.id.tvEstado)).check(matches(withText("Cerrado")));

        // Eliminar
        onView(withId(R.id.btnEliminar)).perform(click());
        onView(withText(R.string.delete)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.empty_cases_title)).check(matches(isDisplayed()));
    }
}
