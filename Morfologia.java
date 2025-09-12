import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

public class Morfologia {

    public static BufferedImage leerImagen(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void guardarImagen(BufferedImage img, String path) throws IOException {
        ImageIO.write(img, "png", new File(path));
    }

    public static BufferedImage erosionSecuencial(BufferedImage img, int[][] elem) {
        return aplicarOperacion(img, elem, true);
    }

    public static BufferedImage dilatacionSecuencial(BufferedImage img, int[][] elem) {
        return aplicarOperacion(img, elem, false);
    }

    private static BufferedImage aplicarOperacion(BufferedImage img, int[][] elem, boolean erosion) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage salida = new BufferedImage(width, height, img.getType());

        int eRows = elem.length;
        int eCols = elem[0].length;
        int eCenterRow = eRows / 2;
        int eCenterCol = eCols / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] resultado = { erosion ? 255 : 0, erosion ? 255 : 0, erosion ? 255 : 0 };

                for (int i = 0; i < eRows; i++) {
                    for (int j = 0; j < eCols; j++) {
                        if (elem[i][j] == 1) {
                            int ny = y + i - eCenterRow;
                            int nx = x + j - eCenterCol;

                            if (ny >= 0 && ny < height && nx >= 0 && nx < width) {
                                int rgb = img.getRGB(nx, ny);
                                int r = (rgb >> 16) & 0xFF;
                                int g = (rgb >> 8) & 0xFF;
                                int b = rgb & 0xFF;

                                if (erosion) {
                                    resultado[0] = Math.min(resultado[0], r);
                                    resultado[1] = Math.min(resultado[1], g);
                                    resultado[2] = Math.min(resultado[2], b);
                                } else {
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
        return salida;
    }

    public static BufferedImage erosionParalela(BufferedImage img, int[][] elem, int hilos) throws InterruptedException {
        return aplicarOperacionParalela(img, elem, hilos, true);
    }

    public static BufferedImage dilatacionParalela(BufferedImage img, int[][] elem, int hilos) throws InterruptedException {
        return aplicarOperacionParalela(img, elem, hilos, false);
    }

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
                        int[] resultado = { erosion ? 255 : 0, erosion ? 255 : 0, erosion ? 255 : 0 };

                        for (int i = 0; i < elem.length; i++) {
                            for (int j = 0; j < elem[0].length; j++) {
                                if (elem[i][j] == 1) {
                                    int ny = y + i - elem.length / 2;
                                    int nx = x + j - elem[0].length / 2;

                                    if (ny >= 0 && ny < height && nx >= 0 && nx < width) {
                                        int rgb = img.getRGB(nx, ny);
                                        int r = (rgb >> 16) & 0xFF;
                                        int g = (rgb >> 8) & 0xFF;
                                        int b = rgb & 0xFF;

                                        if (erosion) {
                                            resultado[0] = Math.min(resultado[0], r);
                                            resultado[1] = Math.min(resultado[1], g);
                                            resultado[2] = Math.min(resultado[2], b);
                                        } else {
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
