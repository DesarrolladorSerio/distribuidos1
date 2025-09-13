import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;         

/**
 * MORFOLOGÍA MATEMÁTICA:
 * - Erosión: Elimina colores claros, busca el MÍNIMO valor entre los píxeles del elemento estructurante
 * - Dilatación: Expande colores claros, busca el MÁXIMO valor entre los píxeles del elemento estructurante
 */
public class Morfologia {

    public static BufferedImage leerImagen(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void guardarImagen(BufferedImage img, String path) throws IOException {
        ImageIO.write(img, "png", new File(path));
    }

     //Aplica erosión de forma secuencial (un solo hilo)

    public static BufferedImage erosionSecuencial(BufferedImage img, int[][] elem) {
        return aplicarOperacion(img, elem, true);  // true = erosión
    }

    // Aplica dilatación de forma secuencial (un solo hilo)
    
    public static BufferedImage dilatacionSecuencial(BufferedImage img, int[][] elem) {
        return aplicarOperacion(img, elem, false); // false = dilatación
    }

    // Método núcleo que implementa la lógica de morfología matemática secuencial
   
    private static BufferedImage aplicarOperacion(BufferedImage img, int[][] elem, boolean erosion) {
        int width = img.getWidth();   // Ancho de la imagen
        int height = img.getHeight(); // Alto de la imagen
        // Crear imagen de salida con los parametros de la original
        BufferedImage salida = new BufferedImage(width, height, img.getType());

        // Dimensiones del elemento estructurante
        int eRows = elem.length;      // N filas
        int eCols = elem[0].length;   // N columnas
        // centro del elemento
        int eCenterRow = eRows / 2;   // Fila central
        int eCenterCol = eCols / 2;   // Columna central


        for (int y = 0; y < height; y++) {         
            for (int x = 0; x < width; x++) {      
                
            
                int[] resultado = { erosion ? 255 : 0, erosion ? 255 : 0, erosion ? 255 : 0 };

                //recorrer posicion
                for (int i = 0; i < eRows; i++) {       
                    for (int j = 0; j < eCols; j++) {   
                        
                        if (elem[i][j] == 1) {
                           
                            int ny = y + i - eCenterRow;  // Coordenada Y en la imagen
                            int nx = x + j - eCenterCol;  // Coordenada X en la imagen

                            
                            if (ny >= 0 && ny < height && nx >= 0 && nx < width) {
                                // Obtener valor RGB del píxel
                                int rgb = img.getRGB(nx, ny);
                                
                               
                                int r = (rgb >> 16) & 0xFF;  // Canal rojo (bits 16-23)
                                int g = (rgb >> 8) & 0xFF;   // Canal verde (bits 8-15)
                                int b = rgb & 0xFF;          // Canal azul (bits 0-7)

                                
                                if (erosion) {
                                
                                    resultado[0] = Math.min(resultado[0], r);  // Rojo
                                    resultado[1] = Math.min(resultado[1], g);  // Verde
                                    resultado[2] = Math.min(resultado[2], b);  // Azul
                                } else {
                        
                                    resultado[0] = Math.max(resultado[0], r);  // Rojo
                                    resultado[1] = Math.max(resultado[1], g);  // Verde
                                    resultado[2] = Math.max(resultado[2], b);  // Azul
                                }
                            }
                        }
                    }
                }
                
                int nuevoRGB = (resultado[0] << 16) | (resultado[1] << 8) | resultado[2];
                
                // Asignar el nuevo valor al píxel para la imagen de slaida
                salida.setRGB(x, y, nuevoRGB);
            }
        }
        return salida;  // Retornar imagen procesada
    }


    
    /**
     * Aplica erosión usando múltiples hilos para mejorar rendimiento
     */
    public static BufferedImage erosionParalela(BufferedImage img, int[][] elem, int hilos) throws InterruptedException {
        return aplicarOperacionParalela(img, elem, hilos, true);  
    }

     //Aplica dilatación usando múltiples hilos para mejorar rendimiento
    
    public static BufferedImage dilatacionParalela(BufferedImage img, int[][] elem, int hilos) throws InterruptedException {
        return aplicarOperacionParalela(img, elem, hilos, false); 
    }

    /**
     * Implementación paralela de morfología matemática
     * ESTRATEGIA DE PARALELIZACIÓN: División horizontal de la imagen
     * Cada hilo procesa un bloque de filas de la imagen
     */
    private static BufferedImage aplicarOperacionParalela(BufferedImage img, int[][] elem, int hilos, boolean erosion) throws InterruptedException {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage salida = new BufferedImage(width, height, img.getType());

    
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        
        int bloque = height / hilos;

        
        for (int t = 0; t < hilos; t++) {
            int inicio = t * bloque;                         
            int fin = (t == hilos - 1) ? height : (t + 1) * bloque;  
            executor.execute(() -> {

                for (int y = inicio; y < fin; y++) {           
                    for (int x = 0; x < width; x++) {      
                        
                        // Inicializar resultado para erosión/dilatación
                        int[] resultado = { erosion ? 255 : 0, erosion ? 255 : 0, erosion ? 255 : 0 };

                        for (int i = 0; i < elem.length; i++) {        // Filas del elemento
                            for (int j = 0; j < elem[0].length; j++) {  // Columnas del elemento
                                
                                if (elem[i][j] == 1) {  
                                    
                                    int ny = y + i - elem.length / 2;    
                                    int nx = x + j - elem[0].length / 2; 

                                    if (ny >= 0 && ny < height && nx >= 0 && nx < width) {
                                        int rgb = img.getRGB(nx, ny);
                                        int r = (rgb >> 16) & 0xFF;  // Canal rojo
                                        int g = (rgb >> 8) & 0xFF;   // Canal verde
                                        int b = rgb & 0xFF;          // Canal azul

                                        if (erosion) {
                                            // EROSIÓN: Buscar mínimo
                                            resultado[0] = Math.min(resultado[0], r);
                                            resultado[1] = Math.min(resultado[1], g);
                                            resultado[2] = Math.min(resultado[2], b);
                                        } else {
                                            // DILATACIÓN: Buscar máximo
                                            resultado[0] = Math.max(resultado[0], r);
                                            resultado[1] = Math.max(resultado[1], g);
                                            resultado[2] = Math.max(resultado[2], b);
                                        }
                                    }
                                }
                            }
                        }
                        
                        
                        int nuevoRGB = (resultado[0] << 16) | (resultado[1] << 8) | resultado[2];
                        salida.setRGB(x, y, nuevoRGB);
                    }
                }
            });
        }


        executor.shutdown();  
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        
        return salida;  
} 

}
