package co.edu.ean.clientrack.model;

// Usuario que inicia sesión (tabla usuarios)
public class Usuario {

    private long id;
    private String nombre;
    private String correo;

    public Usuario(long id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
}
