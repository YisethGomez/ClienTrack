package co.edu.ean.clientrack.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.adapter.ClienteAdapter;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.model.Cliente;
import co.edu.ean.clientrack.util.Formatos;
import co.edu.ean.clientrack.util.SessionManager;
import co.edu.ean.clientrack.util.UiUtils;
import co.edu.ean.clientrack.util.Validador;

// Lista de clientes. Agregar, editar y eliminar se hace desde una hoja inferior
public class ClientesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager sesion;
    private ClienteAdapter adapter;

    private View raiz, layoutVacio;
    private TextView tvVacioTitulo, tvVacioTexto, tvTotalClientes;
    private TextInputEditText etBuscarCliente;
    private ExtendedFloatingActionButton fabNuevoCliente;

    private List<Cliente> todos = new ArrayList<>();
    @Nullable
    private BottomSheetDialog hojaAbierta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);

        db = DatabaseHelper.getInstance(this);
        sesion = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        raiz = findViewById(R.id.raiz);
        layoutVacio = findViewById(R.id.layoutVacio);
        tvVacioTitulo = layoutVacio.findViewById(R.id.tvVacioTitulo);
        tvVacioTexto = layoutVacio.findViewById(R.id.tvVacioTexto);
        tvTotalClientes = findViewById(R.id.tvTotalClientes);
        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        fabNuevoCliente = findViewById(R.id.fabNuevoCliente);

        RecyclerView rv = findViewById(R.id.rvClientes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteAdapter(this::mostrarFormulario);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 6 && fabNuevoCliente.isExtended()) {
                    fabNuevoCliente.shrink();
                } else if (dy < -6 && !fabNuevoCliente.isExtended()) {
                    fabNuevoCliente.extend();
                }
            }
        });

        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltro();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        fabNuevoCliente.setOnClickListener(v -> mostrarFormulario(null));

        cargarClientes();
    }

    @Override
    protected void onDestroy() {
        // Evita que la hoja quede "colgada" si la pantalla se destruye (por ejemplo, al rotar)
        if (hojaAbierta != null) {
            hojaAbierta.dismiss();
            hojaAbierta = null;
        }
        super.onDestroy();
    }

    private void cargarClientes() {
        todos = db.obtenerClientes(sesion.getUsuarioId());
        aplicarFiltro();
    }

    // filtro en memoria por nombre o empresa, ignora tildes y mayúsculas
    private void aplicarFiltro() {
        String busqueda = Formatos.normalizar(etBuscarCliente.getText() == null ? "" : etBuscarCliente.getText().toString());
        List<Cliente> visibles = new ArrayList<>();
        for (Cliente c : todos) {
            if (busqueda.isEmpty()
                    || Formatos.normalizar(c.getNombre()).contains(busqueda)
                    || Formatos.normalizar(c.getEmpresa()).contains(busqueda)) {
                visibles.add(c);
            }
        }
        adapter.submitList(visibles);

        tvTotalClientes.setText(getResources().getQuantityString(R.plurals.total_clientes, visibles.size(), visibles.size()));
        tvTotalClientes.setVisibility(todos.isEmpty() ? View.GONE : View.VISIBLE);

        boolean buscando = !busqueda.isEmpty();
        tvVacioTitulo.setText(buscando ? R.string.empty_search_title : R.string.empty_clients_title);
        tvVacioTexto.setText(buscando ? R.string.empty_search_text : R.string.empty_clients_text);
        layoutVacio.setVisibility(visibles.isEmpty() ? View.VISIBLE : View.GONE);

        if (visibles.isEmpty() && !fabNuevoCliente.isExtended()) {
            fabNuevoCliente.extend();
        }
    }

    // existente == null -> cliente nuevo
    private void mostrarFormulario(@Nullable Cliente existente) {
        if (hojaAbierta != null && hojaAbierta.isShowing()) {
            return;
        }
        View vista = LayoutInflater.from(this).inflate(R.layout.sheet_cliente, null, false);
        TextView tvTitulo = vista.findViewById(R.id.tvTituloSheet);
        TextInputLayout tilNombre = vista.findViewById(R.id.tilNombre);
        TextInputLayout tilCorreo = vista.findViewById(R.id.tilCorreo);
        TextInputLayout tilTelefono = vista.findViewById(R.id.tilTelefono);
        TextInputEditText etNombre = vista.findViewById(R.id.etNombre);
        TextInputEditText etEmpresa = vista.findViewById(R.id.etEmpresa);
        TextInputEditText etCorreo = vista.findViewById(R.id.etCorreo);
        TextInputEditText etTelefono = vista.findViewById(R.id.etTelefono);
        MaterialButton btnGuardar = vista.findViewById(R.id.btnGuardarCliente);
        MaterialButton btnEliminar = vista.findViewById(R.id.btnEliminarCliente);

        tvTitulo.setText(existente == null ? R.string.new_client : R.string.edit_client);
        btnGuardar.setText(existente == null ? R.string.save : R.string.save_changes);

        if (existente != null) {
            etNombre.setText(existente.getNombre());
            etEmpresa.setText(existente.getEmpresa());
            etCorreo.setText(existente.getCorreo());
            etTelefono.setText(existente.getTelefono());
            btnEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setText(existente.getTotalCasos() > 0
                    ? R.string.delete_client_con_casos : R.string.delete_client);
        }

        BottomSheetDialog hoja = new BottomSheetDialog(this);
        hoja.setContentView(vista);
        if (hoja.getWindow() != null) {
            // El teclado empuja el formulario hacia arriba en vez de taparlo
            hoja.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        BottomSheetBehavior<?> comportamiento = hoja.getBehavior();
        comportamiento.setSkipCollapsed(true);
        comportamiento.setState(BottomSheetBehavior.STATE_EXPANDED);
        hoja.setOnDismissListener(d -> hojaAbierta = null);

        vista.findViewById(R.id.btnCerrarSheet).setOnClickListener(v -> hoja.dismiss());

        btnGuardar.setOnClickListener(v -> {
            tilNombre.setError(null);
            tilCorreo.setError(null);
            tilTelefono.setError(null);

            String nombre = texto(etNombre);
            String empresa = texto(etEmpresa);
            String correo = texto(etCorreo).toLowerCase(Locale.ROOT);
            String telefono = texto(etTelefono);
            boolean valido = true;

            if (!Validador.esNombreValido(nombre)) {
                tilNombre.setError(getString(R.string.err_cliente_nombre));
                valido = false;
            }
            if (!correo.isEmpty() && !Validador.esCorreoValido(correo)) {
                tilCorreo.setError(getString(R.string.err_email_formato));
                valido = false;
            }
            if (!telefono.isEmpty() && !Validador.esTelefonoValido(telefono)) {
                tilTelefono.setError(getString(R.string.err_telefono));
                valido = false;
            }
            if (!valido) {
                return;
            }

            Cliente cliente = existente != null ? existente : new Cliente();
            cliente.setUsuarioId(sesion.getUsuarioId());
            cliente.setNombre(nombre);
            cliente.setEmpresa(empresa);
            cliente.setCorreo(correo);
            cliente.setTelefono(telefono);

            boolean exito = existente == null
                    ? db.insertarCliente(cliente) != -1
                    : db.actualizarCliente(cliente) > 0;

            if (exito) {
                hoja.dismiss();
                cargarClientes();
                UiUtils.mostrarMensaje(raiz,
                        existente == null ? R.string.msg_cliente_agregado : R.string.msg_cliente_actualizado,
                        fabNuevoCliente);
            } else {
                tilNombre.setError(getString(R.string.msg_cliente_error));
            }
        });

        if (existente != null) {
            btnEliminar.setOnClickListener(v -> confirmarEliminar(existente, hoja));
        }

        hojaAbierta = hoja;
        hoja.show();
    }

    private void confirmarEliminar(Cliente cliente, BottomSheetDialog hoja) {
        int total = cliente.getTotalCasos();
        String mensaje = total > 0
                ? getResources().getQuantityString(R.plurals.dlg_eliminar_cliente_casos, total, total)
                : getString(R.string.dlg_no_deshacer);

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dlg_eliminar_cliente_titulo, cliente.getNombre()))
                .setMessage(mensaje)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (db.eliminarCliente(cliente.getId(), sesion.getUsuarioId()) > 0) {
                        hoja.dismiss();
                        cargarClientes();
                        UiUtils.mostrarMensaje(raiz, R.string.msg_cliente_eliminado, fabNuevoCliente);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
