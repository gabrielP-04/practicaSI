===========================================
TRANSCRIPTOR DE VIDEO DESDE YOUTUBE - README
===========================================

REQUISITOS
----------
- Sistema operativo: Windows (requerido)
- VLC Media Player instalado (necesario para la reproducción de vídeo)
  Puedes descargarlo desde: https://www.videolan.org/vlc/
- WinRAR (necesario para descomprimir los archivos)

ENTREGA DIVIDIDA EN PARTES
---------------------------
Debido al tamaño total del proyecto, la entrega está dividida en 4 partes comprimidas con WinRAR:

1. PracticaSI : Código fuente (carpeta principal con los archivos ".java")
2. models (carpeta con los modelos Vosk necesarios para transcribir audio)
3. tools (carpeta con dos ejecutables esenciales: "ffmpeg.exe" y "yt-dlp.exe")
4. lib (carpeta con las librerías necesarias)

INSTRUCCIONES DE USO
---------------------
1. Descomprime las tres partes usando WinRAR (asegúrate de extraer todos los archivos juntos en la misma ubicación).
2. Una vez extraído:
   - Asegúrate de que las carpetas "models" y "tools" están en el mismo directorio raíz que el código fuente.
3. Abre el proyecto en Eclipse (u otro entorno compatible con Java y JADE), haz click derecho sobre el proyecto -> "Build path" y añade todas las librerías que están dentro de lib al classpath. 
4. Ejecuta el proyecto: 
   - Para ejecutar, ir a la sección "Run configurations" -> "Arguments" y en "Program Arguments" escribir el siguiente comando : 
"-gui agDesc:es.upm.transcriptor.AgenteDescarga;agPer:es.upm.transcriptor.AgentePercepcion;agUI:es.upm.transcriptor.AgenteInterfaz".
5. Introduce un enlace de YouTube en la interfaz gráfica.
6. Pulsa el botón "Transcribir y reproducir".
7. El vídeo se descargará, se transcribirá y se mostrará con subtítulos sincronizados.

NOTAS ADICIONALES
------------------
- Asegúrate de tener conexión a Internet durante la prueba (para que yt-dlp pueda descargar los vídeos).
- El sistema detectará automáticamente el idioma y usará el modelo de voz correspondiente.
- Los idiomas disponibles son español, inglés y francés.
- Se recomienda utilizar vídeos que no contengan música de fondo.
- Se recomienda cerrar y volver a ejecutar el programa antes de transcribir un nuevo vídeo, para asegurar un funcionamiento correcto.


EJEMPLO DE VÍDEOS PARA PROBAR EL TRANSCRIPTOR
---------------------------------------------
- Español : https://youtu.be/Dji9oiC8c3k?si=IUgf5Xr8Tglzjoc1
- Inglés : https://youtu.be/ry9SYnV3svc?si=sz5cMeYq75ldcVDa
- Francés : https://youtu.be/PLmrHvsEkV4?si=QpWHkjz-XeVDX1Ur

