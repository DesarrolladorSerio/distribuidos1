# Proyecto de Morfología Matemática en Imágenes

Este proyecto implementa los algoritmos de **Erosión** y **Dilatación** en imágenes a color (RGB), tanto en versión **secuencial** como **paralela**.  
Además, incluye una **interfaz gráfica (GUI con Swing)** para facilitar la selección de imágenes y la configuración de parámetros.

## 📂 Contenido

- `Morfologia.java` → Contiene la implementación de los algoritmos de erosión y dilatación (secuencial y paralelo).
- `GUI.java` → Interfaz gráfica para cargar imágenes, seleccionar parámetros y mostrar resultados.
- `README.md` → Este archivo con instrucciones de uso.

## ⚙️ Requisitos

- **Java JDK 8 o superior** instalado.
- Sistema operativo Windows, Linux o macOS.

## ▶️ Instrucciones de Uso

1. **Compilar** los archivos desde la carpeta del proyecto:
   ```bash
   javac *.java
   ```

2. **Ejecutar** la aplicación con:
   ```bash
   java GUI
   ```

3. En la interfaz gráfica:
   - Pulsa **"Cargar Imagen"** para seleccionar un archivo PNG.
   - Configura:
     - Algoritmo: **Erosión** o **Dilatación**.
     - Modo: **Secuencial** o **Paralelo**.
     - Elemento estructurante: Cruz, Cuadrado 3x3, X, Horizontal o Vertical.
     - Número de hilos (si eliges paralelo).
   - Pulsa **"Procesar"** para ejecutar el algoritmo.
   - Guarda el resultado en PNG con el botón **"Guardar Resultado"**.

## 📊 Notas Importantes

- El programa soporta imágenes grandes (hasta 10.000 x 10.000 píxeles).
- El procesamiento en paralelo divide la imagen en bloques de filas distribuidas entre los hilos.
- Los elementos estructurantes están definidos como matrices binarias (0 y 1).


