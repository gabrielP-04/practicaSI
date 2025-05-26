package es.upm.transcriptor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SubtituloParser {
    public static List<Subtitulo> parsear(String jsonTexto) {
        List<Subtitulo> lista = new ArrayList<>();
        JSONArray array = new JSONArray(jsonTexto);

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.has("start") && obj.has("end") && obj.has("word")) {
                long inicio = (long) (obj.getDouble("start") * 1000);
                long fin = (long) (obj.getDouble("end") * 1000);
                String palabra = obj.getString("word");
                lista.add(new Subtitulo(inicio, fin, palabra));
            }
        }

        return lista;
    }

    public static String obtenerLinea(long tiempoMs, List<Subtitulo> subtitulos) {
        StringBuilder linea = new StringBuilder();
        for (Subtitulo s : subtitulos) {
            if (tiempoMs >= s.inicioMs && tiempoMs <= s.finMs) {
                linea.append(s.texto).append(" ");
            }
        }
        return linea.toString();
    }
}
