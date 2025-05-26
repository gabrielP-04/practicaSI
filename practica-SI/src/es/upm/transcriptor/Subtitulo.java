package es.upm.transcriptor;

public class Subtitulo {
    public final long inicioMs;
    public final long finMs;
    public final String texto;

    public Subtitulo(long inicioMs, long finMs, String texto) {
        this.inicioMs = inicioMs;
        this.finMs = finMs;
        this.texto = texto;
    }
}
