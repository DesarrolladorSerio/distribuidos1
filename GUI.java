import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUI extends JFrame {

    private JLabel labelImagenOriginal;
    private JLabel labelImagenProcesada;
    private BufferedImage imagenOriginal;
    private BufferedImage imagenProcesada;
    private File archivoSeleccionado;

    private JComboBox<String> comboAlgoritmo;
    private JComboBox<String> comboModo;
    private JComboBox<String> comboElemento;
    private JTextField txtHilos;

    public GUI() {
        setTitle("Procesamiento Morfología Matemática");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel(new FlowLayout());
        JButton btnCargar = new JButton("Cargar Imagen");
        comboAlgoritmo = new JComboBox<>(new String[]{"Erosión", "Dilatación"});
        comboModo = new JComboBox<>(new String[]{"Secuencial", "Paralelo"});
        comboElemento = new JComboBox<>(new String[]{"Cruz", "Cuadrado 3x3", "X", "Horizontal", "Vertical"});
        txtHilos = new JTextField("4", 5);
        JButton btnProcesar = new JButton("Procesar");
        JButton btnGuardar = new JButton("Guardar Resultado");

        panelSuperior.add(btnCargar);
        panelSuperior.add(new JLabel("Algoritmo:"));
        panelSuperior.add(comboAlgoritmo);
        panelSuperior.add(new JLabel("Modo:"));
        panelSuperior.add(comboModo);
        panelSuperior.add(new JLabel("Elemento:"));
        panelSuperior.add(comboElemento);
        panelSuperior.add(new JLabel("Hilos:"));
        panelSuperior.add(txtHilos);
        panelSuperior.add(btnProcesar);
        panelSuperior.add(btnGuardar);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new GridLayout(1, 2));
        labelImagenOriginal = new JLabel("Imagen Original", SwingConstants.CENTER);
        labelImagenProcesada = new JLabel("Imagen Procesada", SwingConstants.CENTER);
        panelCentral.add(new JScrollPane(labelImagenOriginal));
        panelCentral.add(new JScrollPane(labelImagenProcesada));
        add(panelCentral, BorderLayout.CENTER);

        btnCargar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoSeleccionado = fileChooser.getSelectedFile();
                try {
                    imagenOriginal = Morfologia.leerImagen(archivoSeleccionado.getAbsolutePath());
                    labelImagenOriginal.setIcon(new ImageIcon(imagenOriginal.getScaledInstance(400, -1, Image.SCALE_SMOOTH)));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al cargar imagen");
                }
            }
        });

        btnProcesar.addActionListener(e -> {
            if (imagenOriginal == null) {
                JOptionPane.showMessageDialog(this, "Primero cargue una imagen.");
                return;
            }
            int[][] elemento = obtenerElementoEstructurante(comboElemento.getSelectedIndex());
            String algoritmo = (String) comboAlgoritmo.getSelectedItem();
            String modo = (String) comboModo.getSelectedItem();
            try {
                long inicio = System.currentTimeMillis();
                if (modo.equals("Secuencial")) {
                    if (algoritmo.equals("Erosión")) {
                        imagenProcesada = Morfologia.erosionSecuencial(imagenOriginal, elemento);
                    } else {
                        imagenProcesada = Morfologia.dilatacionSecuencial(imagenOriginal, elemento);
                    }
                } else {
                    int hilos = Integer.parseInt(txtHilos.getText());
                    if (algoritmo.equals("Erosión")) {
                        imagenProcesada = Morfologia.erosionParalela(imagenOriginal, elemento, hilos);
                    } else {
                        imagenProcesada = Morfologia.dilatacionParalela(imagenOriginal, elemento, hilos);
                    }
                }
                long fin = System.currentTimeMillis();
                JOptionPane.showMessageDialog(this, "Procesamiento terminado en " + (fin - inicio) + " ms");
                labelImagenProcesada.setIcon(new ImageIcon(imagenProcesada.getScaledInstance(400, -1, Image.SCALE_SMOOTH)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar imagen: " + ex.getMessage());
            }
        });

        btnGuardar.addActionListener(e -> {
            if (imagenProcesada == null) {
                JOptionPane.showMessageDialog(this, "Primero procese una imagen.");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Morfologia.guardarImagen(imagenProcesada, fileChooser.getSelectedFile().getAbsolutePath() + ".png");
                    JOptionPane.showMessageDialog(this, "Imagen guardada correctamente.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al guardar imagen.");
                }
            }
        });
    }

    private int[][] obtenerElementoEstructurante(int opcion) {
        switch (opcion) {
            case 0:
                return new int[][]{{0,1,0},{1,1,1},{0,1,0}};
            case 1:
                return new int[][]{{1,1,1},{1,1,1},{1,1,1}};
            case 2:
                return new int[][]{{1,0,1},{0,1,0},{1,0,1}};
            case 3:
                return new int[][]{{1,1,1}};
            case 4:
                return new int[][]{{1},{1},{1}};
            default:
                return new int[][]{{1}};
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI ventana = new GUI();
            ventana.setVisible(true);
        });
    }
}
