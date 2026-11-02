package co.edu.ean.clientrack.model;

import java.time.LocalDate;

// Caso de un cliente: solicitud, queja o pendiente (tabla casos)
public class Caso {

    // Valores permitidos (también se validan en la base de datos con CHECK)
    public static final String[] PRIORIDADES = {"Alta", "Media", "Baja"};
    public static final String[] ESTADOS = {"Abierto", "En proceso", "Cerrado"};

    public static final String ESTADO_ABIERTO = "Abierto";
    public static final String ESTADO_EN_PROCESO = "En proceso";
    public static final String ESTADO_CERRADO = "Cerrado";

    public static final String PRIORIDAD_ALTA = "Alta";
    public static final String PRIORIDAD_MEDIA = "Media";
    public static final String PRIORIDAD_BAJA = "Baja";

    private long id;
    private long usuarioId;
    private long clienteId;
    private String asunto;
    private String descripcion;
    private String prioridad;
    private String estado;
    private String fechaLimite;   // Formato ISO: yyyy-MM-dd
    private String fechaCreacion; // Formato ISO: yyyy-MM-dd

    // Datos del cliente (se obtienen con un JOIN, no son columnas de "casos")
    private String clienteNombre;
    private String clienteEmpresa;
    private String clienteCorreo;
    private String clienteTelefono;

    public Caso() { }

    // Vencido = no está cerrado y la fecha límite ya pasó (yyyy-MM-dd se puede comparar como texto)
    public boolean isVencido() {
        return isVencido(LocalDate.now());
    }

    // Recibe "hoy" como parámetro para poder probarlo
    public boolean isVencido(LocalDate hoy) {
        if (ESTADO_CERRADO.equals(estado) || fechaLimite == null || fechaLimite.isEmpty()) {
            return false;
        }
        return fechaLimite.compareTo(hoy.toString()) < 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long usuarioId) { this.usuarioId = usuarioId; }

    public long getClienteId() { return clienteId; }
    public void setClienteId(long clienteId) { this.clienteId = clienteId; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(String fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteEmpresa() { return clienteEmpresa; }
    public void setClienteEmpresa(String clienteEmpresa) { this.clienteEmpresa = clienteEmpresa; }

    public String getClienteCorreo() { return clienteCorreo; }
    public void setClienteCorreo(String clienteCorreo) { this.clienteCorreo = clienteCorreo; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
}
