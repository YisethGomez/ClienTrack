package co.edu.ean.clientrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import co.edu.ean.clientrack.model.Caso;
import co.edu.ean.clientrack.model.Cliente;
import co.edu.ean.clientrack.model.Usuario;
import co.edu.ean.clientrack.util.Formatos;

// Base de datos SQLite: usuarios -> clientes -> casos.
// Todas las consultas filtran por usuario_id para que cada uno vea solo lo suyo.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NOMBRE = "clientrack.db";
    private static final int DB_VERSION = 1;

    // Tabla usuarios
    private static final String SQL_CREAR_USUARIOS =
            "CREATE TABLE usuarios (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  nombre TEXT NOT NULL," +
            "  correo TEXT NOT NULL UNIQUE," +
            "  password_hash TEXT NOT NULL," +
            "  fecha_registro TEXT NOT NULL" +
            ")";

    // Tabla clientes
    private static final String SQL_CREAR_CLIENTES =
            "CREATE TABLE clientes (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  usuario_id INTEGER NOT NULL," +
            "  nombre TEXT NOT NULL," +
            "  empresa TEXT," +
            "  correo TEXT," +
            "  telefono TEXT," +
            "  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE" +
            ")";

    // Tabla casos
    private static final String SQL_CREAR_CASOS =
            "CREATE TABLE casos (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  usuario_id INTEGER NOT NULL," +
            "  cliente_id INTEGER NOT NULL," +
            "  asunto TEXT NOT NULL," +
            "  descripcion TEXT," +
            "  prioridad TEXT NOT NULL CHECK (prioridad IN ('Alta','Media','Baja'))," +
            "  estado TEXT NOT NULL CHECK (estado IN ('Abierto','En proceso','Cerrado'))," +
            "  fecha_limite TEXT NOT NULL," +
            "  fecha_creacion TEXT NOT NULL," +
            "  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE," +
            "  FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE" +
            ")";

    private static DatabaseHelper instancia;

    // singleton, una sola instancia para toda la app
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instancia == null) {
            instancia = new DatabaseHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NOMBRE, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // sin esto no funciona el ON DELETE CASCADE
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREAR_USUARIOS);
        db.execSQL(SQL_CREAR_CLIENTES);
        db.execSQL(SQL_CREAR_CASOS);
        db.execSQL("CREATE INDEX idx_clientes_usuario ON clientes(usuario_id)");
        db.execSQL("CREATE INDEX idx_casos_usuario ON casos(usuario_id)");
        db.execSQL("CREATE INDEX idx_casos_cliente ON casos(cliente_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        db.execSQL("DROP TABLE IF EXISTS casos");
        db.execSQL("DROP TABLE IF EXISTS clientes");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    // Usuarios

    /** Registra un usuario nuevo. Devuelve el id creado o -1 si falla (por ejemplo, correo repetido). */
    public long registrarUsuario(String nombre, String correo, String passwordHash) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("correo", correo);
        valores.put("password_hash", passwordHash);
        valores.put("fecha_registro", LocalDate.now().toString());
        return getWritableDatabase().insert("usuarios", null, valores);
    }

    /** Indica si ya existe un usuario con ese correo. */
    public boolean existeCorreo(String correo) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM usuarios WHERE correo = ?", new String[]{correo})) {
            return c.moveToFirst();
        }
    }

    /** Valida las credenciales. Devuelve el usuario si son correctas o null si no. */
    public Usuario login(String correo, String passwordHash) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre, correo FROM usuarios WHERE correo = ? AND password_hash = ?",
                new String[]{correo, passwordHash})) {
            if (c.moveToFirst()) {
                return new Usuario(c.getLong(0), c.getString(1), c.getString(2));
            }
            return null;
        }
    }

    // Clientes

    private ContentValues valoresCliente(Cliente cliente) {
        ContentValues valores = new ContentValues();
        valores.put("usuario_id", cliente.getUsuarioId());
        valores.put("nombre", cliente.getNombre());
        valores.put("empresa", cliente.getEmpresa());
        valores.put("correo", cliente.getCorreo());
        valores.put("telefono", cliente.getTelefono());
        return valores;
    }

    public long insertarCliente(Cliente cliente) {
        return getWritableDatabase().insert("clientes", null, valoresCliente(cliente));
    }

    public int actualizarCliente(Cliente cliente) {
        return getWritableDatabase().update("clientes", valoresCliente(cliente),
                "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(cliente.getId()), String.valueOf(cliente.getUsuarioId())});
    }

    /** Elimina un cliente. Por el ON DELETE CASCADE también se eliminan sus casos. */
    public int eliminarCliente(long clienteId, long usuarioId) {
        return getWritableDatabase().delete("clientes", "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(clienteId), String.valueOf(usuarioId)});
    }

    /** Lista los clientes del usuario con la cantidad de casos que tiene cada uno. */
    public List<Cliente> obtenerClientes(long usuarioId) {
        List<Cliente> lista = new ArrayList<>();
        String sql =
                "SELECT cl.id, cl.nombre, cl.empresa, cl.correo, cl.telefono, COUNT(ca.id) AS total " +
                "FROM clientes cl " +
                "LEFT JOIN casos ca ON ca.cliente_id = cl.id " +
                "WHERE cl.usuario_id = ? " +
                "GROUP BY cl.id " +
                "ORDER BY cl.nombre COLLATE NOCASE";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(usuarioId)})) {
            while (c.moveToNext()) {
                Cliente cliente = new Cliente();
                cliente.setId(c.getLong(0));
                cliente.setUsuarioId(usuarioId);
                cliente.setNombre(c.getString(1));
                cliente.setEmpresa(c.getString(2));
                cliente.setCorreo(c.getString(3));
                cliente.setTelefono(c.getString(4));
                cliente.setTotalCasos(c.getInt(5));
                lista.add(cliente);
            }
        }
        return lista;
    }

    // Casos

    // Consulta base: une cada caso con los datos de su cliente (JOIN)
    private static final String SQL_SELECT_CASO =
            "SELECT ca.id, ca.cliente_id, ca.asunto, ca.descripcion, ca.prioridad, ca.estado, " +
            "       ca.fecha_limite, ca.fecha_creacion, " +
            "       cl.nombre, cl.empresa, cl.correo, cl.telefono " +
            "FROM casos ca " +
            "INNER JOIN clientes cl ON cl.id = ca.cliente_id ";

    private Caso leerCaso(Cursor c, long usuarioId) {
        Caso caso = new Caso();
        caso.setId(c.getLong(0));
        caso.setUsuarioId(usuarioId);
        caso.setClienteId(c.getLong(1));
        caso.setAsunto(c.getString(2));
        caso.setDescripcion(c.getString(3));
        caso.setPrioridad(c.getString(4));
        caso.setEstado(c.getString(5));
        caso.setFechaLimite(c.getString(6));
        caso.setFechaCreacion(c.getString(7));
        caso.setClienteNombre(c.getString(8));
        caso.setClienteEmpresa(c.getString(9));
        caso.setClienteCorreo(c.getString(10));
        caso.setClienteTelefono(c.getString(11));
        return caso;
    }

    public long insertarCaso(Caso caso) {
        ContentValues valores = new ContentValues();
        valores.put("usuario_id", caso.getUsuarioId());
        valores.put("cliente_id", caso.getClienteId());
        valores.put("asunto", caso.getAsunto());
        valores.put("descripcion", caso.getDescripcion());
        valores.put("prioridad", caso.getPrioridad());
        valores.put("estado", caso.getEstado());
        valores.put("fecha_limite", caso.getFechaLimite());
        valores.put("fecha_creacion", LocalDate.now().toString());
        return getWritableDatabase().insert("casos", null, valores);
    }

    public int actualizarCaso(Caso caso) {
        ContentValues valores = new ContentValues();
        valores.put("cliente_id", caso.getClienteId());
        valores.put("asunto", caso.getAsunto());
        valores.put("descripcion", caso.getDescripcion());
        valores.put("prioridad", caso.getPrioridad());
        valores.put("estado", caso.getEstado());
        valores.put("fecha_limite", caso.getFechaLimite());
        return getWritableDatabase().update("casos", valores, "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(caso.getId()), String.valueOf(caso.getUsuarioId())});
    }

    /** Cambia solo el estado de un caso (por ejemplo, "Marcar como cerrado"). */
    public int actualizarEstado(long casoId, long usuarioId, String estado) {
        ContentValues valores = new ContentValues();
        valores.put("estado", estado);
        return getWritableDatabase().update("casos", valores, "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(casoId), String.valueOf(usuarioId)});
    }

    public int eliminarCaso(long casoId, long usuarioId) {
        return getWritableDatabase().delete("casos", "id = ? AND usuario_id = ?",
                new String[]{String.valueOf(casoId), String.valueOf(usuarioId)});
    }

    /** Obtiene un caso por su id (o null si no existe o no es del usuario). */
    public Caso obtenerCaso(long casoId, long usuarioId) {
        String sql = SQL_SELECT_CASO + "WHERE ca.id = ? AND ca.usuario_id = ?";
        try (Cursor c = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(casoId), String.valueOf(usuarioId)})) {
            return c.moveToFirst() ? leerCaso(c, usuarioId) : null;
        }
    }

    // estado null = todos. Ordena por prioridad y luego por fecha límite
    public List<Caso> buscarCasos(long usuarioId, String texto, String estado) {
        List<Caso> lista = new ArrayList<>();
        List<String> args = new ArrayList<>();

        StringBuilder sql = new StringBuilder(SQL_SELECT_CASO);
        sql.append("WHERE ca.usuario_id = ? ");
        args.add(String.valueOf(usuarioId));

        if (texto != null && !texto.trim().isEmpty()) {
            // Se escapan % y _ para que se busquen como texto normal y no como comodines
            String patron = "%" + Formatos.escaparLike(texto.trim()) + "%";
            sql.append("AND (ca.asunto LIKE ? ESCAPE '\\' OR cl.nombre LIKE ? ESCAPE '\\' ")
               .append("OR cl.empresa LIKE ? ESCAPE '\\') ");
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        if (estado != null) {
            sql.append("AND ca.estado = ? ");
            args.add(estado);
        }
        sql.append("ORDER BY CASE ca.prioridad WHEN 'Alta' THEN 1 WHEN 'Media' THEN 2 ELSE 3 END, ")
           .append("ca.fecha_limite ASC");

        try (Cursor c = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]))) {
            while (c.moveToNext()) {
                lista.add(leerCaso(c, usuarioId));
            }
        }
        return lista;
    }

    // [abiertos, en proceso, cerrados, vencidos]
    public int[] obtenerResumen(long usuarioId) {
        String hoy = LocalDate.now().toString();
        String sql =
                "SELECT " +
                "  SUM(CASE WHEN estado = 'Abierto' THEN 1 ELSE 0 END), " +
                "  SUM(CASE WHEN estado = 'En proceso' THEN 1 ELSE 0 END), " +
                "  SUM(CASE WHEN estado = 'Cerrado' THEN 1 ELSE 0 END), " +
                "  SUM(CASE WHEN estado <> 'Cerrado' AND fecha_limite < ? THEN 1 ELSE 0 END) " +
                "FROM casos WHERE usuario_id = ?";
        int[] resumen = new int[4];
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{hoy, String.valueOf(usuarioId)})) {
            if (c.moveToFirst()) {
                for (int i = 0; i < 4; i++) {
                    resumen[i] = c.isNull(i) ? 0 : c.getInt(i);
                }
            }
        }
        return resumen;
    }
}
