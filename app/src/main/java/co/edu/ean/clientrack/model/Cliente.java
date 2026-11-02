package co.edu.ean.clientrack.model;

// Cliente de un usuario (tabla clientes)
public class Cliente {

    private long id;
    private long usuarioId;
    private String nombre;
    private String empresa;
    private String correo;
    private String telefono;
    private int totalCasos; // Dato calculado con una consulta (no es columna de la tabla)

    public Cliente() { }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getTotalCasos() { return totalCasos; }
    public void setTotalCasos(int totalCasos) { this.totalCasos = totalCasos; }

    /** Texto que se muestra en el selector de clientes del formulario de casos. */
    public String getNombreParaMostrar() {
        if (empresa != null && !empresa.isEmpty()) {
            return nombre + " (" + empresa + ")";
        }
        return nombre;
    }
}
