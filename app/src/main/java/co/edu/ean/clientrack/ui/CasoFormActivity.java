package co.edu.ean.clientrack.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.model.Caso;
import co.edu.ean.clientrack.model.Cliente;
import co.edu.ean.clientrack.util.Formatos;
import co.edu.ean.clientrack.util.SessionManager;
import co.edu.ean.clientrack.util.UiUtils;
import co.edu.ean.clientrack.util.Validador;

// Formulario para crear o editar un caso (si llega EXTRA_CASO_ID es edición)
public class CasoFormActivity extends AppCompatActivity {

    public static final String EXTRA_CASO_ID = "caso_id";

    private static final String TAG_CALENDARIO = "calendario_fecha_limite";
    private static final String KEY_CLIENTE = "cliente";
    private static final String KEY_FECHA = "fecha";
    private static final String KEY_PRIORIDAD = "prioridad";
    private static final String KEY_ESTADO = "estado";

    private DatabaseHelper db;
    private SessionManager sesion;

    private MaterialAutoCompleteTextView actCliente;
    private MaterialButtonToggleGroup tgPrioridad, tgEstado;
    private TextInputLayout tilAsunto, tilFecha;
    private TextInputEditText etAsunto, etDescripcion, etFecha;

    private List<Cliente> clientes;
    private List<String> nombresClientes;
    private int clienteSeleccionado = 0;
    private long casoId = -1;
    private String fechaLimiteIso = null; // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caso_form);

        db = DatabaseHelper.getInstance(this);
        sesion = new SessionManager(this);
        casoId = getIntent().getLongExtra(EXTRA_CASO_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(casoId == -1 ? R.string.new_case : R.string.edit_case);
        toolbar.setNavigationOnClickListener(v -> finish());

        actCliente = findViewById(R.id.actCliente);
        tgPrioridad = findViewById(R.id.tgPrioridad);
        tgEstado = findViewById(R.id.tgEstado);
        tilAsunto = findViewById(R.id.tilAsunto);
        tilFecha = findViewById(R.id.tilFecha);
        etAsunto = findViewById(R.id.etAsunto);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);

        // Un caso siempre pertenece a un cliente
        clientes = db.obtenerClientes(sesion.getUsuarioId());
        if (clientes.isEmpty()) {
            UiUtils.terminarConMensaje(this, R.string.msg_primero_cliente);
            return;
        }
        nombresClientes = new ArrayList<>();
        for (Cliente c : clientes) {
            nombresClientes.add(c.getNombreParaMostrar());
        }
        actCliente.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown, nombresClientes));
        actCliente.setOnItemClickListener((parent, view, position, id) -> clienteSeleccionado = position);

        ((MaterialButton) findViewById(R.id.btnGuardar)).setText(casoId == -1 ? R.string.save_case : R.string.save_changes);

        if (savedInstanceState != null) {
            // al rotar se recupera lo que ya se había elegido
            clienteSeleccionado = savedInstanceState.getInt(KEY_CLIENTE, 0);
            fechaLimiteIso = savedInstanceState.getString(KEY_FECHA);
            tgPrioridad.check(idPrioridad(savedInstanceState.getString(KEY_PRIORIDAD)));
            tgEstado.check(idEstado(savedInstanceState.getString(KEY_ESTADO)));
        } else if (casoId != -1) {
            cargarCaso();
        }
        mostrarCliente();
        mostrarFecha();

        etFecha.setOnClickListener(v -> mostrarCalendario());
        tilFecha.setEndIconOnClickListener(v -> mostrarCalendario());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());

        // Si el calendario estaba abierto al rotar, se vuelve a conectar su botón "Aceptar"
        Fragment abierto = getSupportFragmentManager().findFragmentByTag(TAG_CALENDARIO);
        if (abierto instanceof MaterialDatePicker) {
            @SuppressWarnings("unchecked")
            MaterialDatePicker<Long> calendario = (MaterialDatePicker<Long>) abierto;
            conectarCalendario(calendario);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CLIENTE, clienteSeleccionado);
        outState.putString(KEY_FECHA, fechaLimiteIso);
        if (tgPrioridad != null) {
            outState.putString(KEY_PRIORIDAD, prioridadSeleccionada());
            outState.putString(KEY_ESTADO, estadoSeleccionado());
        }
    }

    // modo edición
    private void cargarCaso() {
        Caso caso = db.obtenerCaso(casoId, sesion.getUsuarioId());
        if (caso == null) {
            UiUtils.terminarConMensaje(this, R.string.msg_caso_no_existe);
            return;
        }
        etAsunto.setText(caso.getAsunto());
        etDescripcion.setText(caso.getDescripcion());

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == caso.getClienteId()) {
                clienteSeleccionado = i;
                break;
            }
        }
        tgPrioridad.check(idPrioridad(caso.getPrioridad()));
        tgEstado.check(idEstado(caso.getEstado()));
        fechaLimiteIso = caso.getFechaLimite();
    }

    private void mostrarCliente() {
        if (clienteSeleccionado < 0 || clienteSeleccionado >= nombresClientes.size()) {
            clienteSeleccionado = 0;
        }
        // false = no filtrar la lista: así el menú sigue mostrando todos los clientes
        actCliente.setText(nombresClientes.get(clienteSeleccionado), false);
    }

    private void mostrarFecha() {
        etFecha.setText(fechaLimiteIso == null ? "" : Formatos.fechaLarga(fechaLimiteIso));
    }

    private void mostrarCalendario() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_CALENDARIO) != null) {
            return; // Evita abrirlo dos veces con un doble toque
        }
        LocalDate hoy = LocalDate.now();
        LocalDate inicial = fechaLimiteIso != null ? LocalDate.parse(fechaLimiteIso) : hoy;

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.date_picker_title)
                .setPositiveButtonText(R.string.date_picker_ok)
                .setNegativeButtonText(R.string.cancel)
                .setSelection(Formatos.aMilisUtc(inicial));

        // Para casos nuevos no se permite elegir una fecha que ya pasó
        if (casoId == -1) {
            long milisHoy = Formatos.aMilisUtc(hoy);
            builder.setCalendarConstraints(new CalendarConstraints.Builder()
                    .setStart(milisHoy)
                    .setValidator(DateValidatorPointForward.from(milisHoy))
                    .build());
        }

        MaterialDatePicker<Long> calendario = builder.build();
        conectarCalendario(calendario);
        calendario.show(getSupportFragmentManager(), TAG_CALENDARIO);
    }

    private void conectarCalendario(MaterialDatePicker<Long> calendario) {
        calendario.addOnPositiveButtonClickListener(seleccion -> {
            if (seleccion == null) {
                return;
            }
            fechaLimiteIso = Formatos.desdeMilisUtc(seleccion);
            mostrarFecha();
            tilFecha.setError(null);
        });
    }

    private void guardar() {
        tilAsunto.setError(null);
        tilFecha.setError(null);

        String asunto = etAsunto.getText() == null ? "" : etAsunto.getText().toString().trim();
        String descripcion = etDescripcion.getText() == null ? "" : etDescripcion.getText().toString().trim();
        boolean valido = true;

        if (asunto.isEmpty()) {
            tilAsunto.setError(getString(R.string.err_asunto_vacio));
            valido = false;
        } else if (!Validador.esAsuntoValido(asunto)) {
            tilAsunto.setError(getString(R.string.err_asunto_corto));
            valido = false;
        }
        if (fechaLimiteIso == null) {
            tilFecha.setError(getString(R.string.err_fecha));
            valido = false;
        }
        if (!valido) {
            return;
        }

        Caso caso = new Caso();
        caso.setId(casoId);
        caso.setUsuarioId(sesion.getUsuarioId());
        caso.setClienteId(clientes.get(clienteSeleccionado).getId());
        caso.setAsunto(asunto);
        caso.setDescripcion(descripcion);
        caso.setPrioridad(prioridadSeleccionada());
        caso.setEstado(estadoSeleccionado());
        caso.setFechaLimite(fechaLimiteIso);

        boolean exito = casoId == -1
                ? db.insertarCaso(caso) != -1
                : db.actualizarCaso(caso) > 0;

        if (exito) {
            UiUtils.terminarConMensaje(this, casoId == -1 ? R.string.msg_caso_creado : R.string.msg_caso_actualizado);
        } else {
            UiUtils.mostrarMensaje(findViewById(R.id.raiz), R.string.msg_caso_error, findViewById(R.id.btnGuardar));
        }
    }

    // Conversión entre botones y valores guardados

    private String prioridadSeleccionada() {
        int id = tgPrioridad.getCheckedButtonId();
        if (id == R.id.btnPrioAlta) return Caso.PRIORIDAD_ALTA;
        if (id == R.id.btnPrioBaja) return Caso.PRIORIDAD_BAJA;
        return Caso.PRIORIDAD_MEDIA;
    }

    private String estadoSeleccionado() {
        int id = tgEstado.getCheckedButtonId();
        if (id == R.id.btnEstProceso) return Caso.ESTADO_EN_PROCESO;
        if (id == R.id.btnEstCerrado) return Caso.ESTADO_CERRADO;
        return Caso.ESTADO_ABIERTO;
    }

    @IdRes
    private static int idPrioridad(String prioridad) {
        if (Caso.PRIORIDAD_ALTA.equals(prioridad)) return R.id.btnPrioAlta;
        if (Caso.PRIORIDAD_BAJA.equals(prioridad)) return R.id.btnPrioBaja;
        return R.id.btnPrioMedia;
    }

    @IdRes
    private static int idEstado(String estado) {
        if (Caso.ESTADO_EN_PROCESO.equals(estado)) return R.id.btnEstProceso;
        if (Caso.ESTADO_CERRADO.equals(estado)) return R.id.btnEstCerrado;
        return R.id.btnEstAbierto;
    }
}
