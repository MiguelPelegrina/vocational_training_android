package com.example.proyecto.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Clase que gestiona la conexión con la base de datos externa
 */
public class APIConnection {
    // Atributos de la clase
    // URL base que no se modificará
    private static final String URL_BASE_BB = "https://www.breakingbadapi.com/api/";
    private static final String URL_BASE_HP = "https://hp-api.onrender.com/api/characters";

    /**
     * Método de clase que realiza una consulta a la API para obtener la información deseada
     * @param endpoint Parámetro que modifica la consulta realizada
     * @return String que contiene toda la información solicitada
     */
    public static String getRequest(String endpoint, String api){
        //Declaracion e inicializacion de variables
        HttpURLConnection http = null;
        String content = null;
        URL url = null;
        try {
            // Nos formamos la URL más el endpoint introducido como parámetro
            switch (api){
                case "bb":
                    url = new URL(URL_BASE_BB + endpoint);
                    break;
                case "hp":
                    url = new URL(URL_BASE_HP + endpoint);
                    break;
            }
            // Establecemos la conexión
            http = (HttpURLConnection) url.openConnection();
            // Establecemos la codificación de los datos del flujo
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Accept", "application/json");

            // Cuando el servidor devuelve HTTP_OK (200) se ha devuelto de forma adecuada la
            // información pedida a través de la consulta
            if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
                // Codificamos como String el texto de la respuesta
                StringBuilder stringBuilder = new StringBuilder();
                // Abrimos el flujo de datos para el lector
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));
                // Variable auxiliar que guarda el contenido de cada linea
                String line;
                // Mientras que la línea no sea nula, añadimos su contenido al stringBuilder
                while ((line = reader.readLine()) != null){
                    stringBuilder.append(line);
                }
                // Convertimos el stringBuilder en String
                content = stringBuilder.toString();
                // Cerramos el flujo de datos
                reader.close();
            }else{
                // Esta comprobación se realiza en el caso en el cual se produce un error en el
                // servidor al que le mandamos la petición REST
                if(http.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE ||
                    http.getResponseCode() == HttpURLConnection.HTTP_GATEWAY_TIMEOUT){
                    content = null;
                }
            }
            // Tratamos las excepciones
        } catch (MalformedURLException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            // Cerramos la conexión por internet
            if (http != null){
                http.disconnect();
            }
        }
        return content;
    }
}
