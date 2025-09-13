
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


public class GUI extends JFrame {
    
    // mostrar imagenes
    private JLabel labelImagenOriginal;    
    private JLabel labelImagenProcesada;   
    
    private BufferedImage imagenOriginal;  
    private BufferedImage imagenProcesada; 
    private File archivoSeleccionado;      
     

    //selectores de configuracion
    private JComboBox<String> comboAlgoritmo;  
    private JComboBox<String> comboModo;      
    private JComboBox<String> comboElemento;   
    private JTextField txtHilos;               



    public GUI() {
        setTitle("Procesamiento Morfología Matemática");  
        setSize(1000, 600);                             
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        setLayout(new BorderLayout());                   

        // ver los botones y controles para configurarlo
        JPanel panelSuperior = new JPanel(new FlowLayout());  
        
        // botones principales
        JButton btnCargar = new JButton("Cargar Imagen");    
        JButton btnProcesar = new JButton("Procesar");        
        JButton btnGuardar = new JButton("Guardar Resultado"); 
        
        //controles para seleccion
        comboAlgoritmo = new JComboBox<>(new String[]{"Erosión", "Dilatación"});     
        comboModo = new JComboBox<>(new String[]{"Secuencial", "Paralelo"});         
        comboElemento = new JComboBox<>(new String[]{"Cruz", "Cuadrado 3x3", "X", "Horizontal", "Vertical"}); // Elemento estructurante
        txtHilos = new JTextField("4", 5);  //nº de hilos (por defecto son  4)


        //etiquetas descriptivas
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

        // Panel creado sale en dos columnas para mostrar imagen original y procesada
        JPanel panelCentral = new JPanel(new GridLayout(1, 2));  
        
        labelImagenOriginal = new JLabel("Imagen Original", SwingConstants.CENTER);
        labelImagenProcesada = new JLabel("Imagen Procesada", SwingConstants.CENTER);
        
        panelCentral.add(new JScrollPane(labelImagenOriginal));
        panelCentral.add(new JScrollPane(labelImagenProcesada));
        add(panelCentral, BorderLayout.CENTER);          

        
        //carga de imagen
        btnCargar.addActionListener(e -> {
            // abre selector de archivos
            JFileChooser fileChooser = new JFileChooser();
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoSeleccionado = fileChooser.getSelectedFile();  
                try {
                    // Cargar la imagen con la clase Morfologia
                    imagenOriginal = Morfologia.leerImagen(archivoSeleccionado.getAbsolutePath());
                    
                    // Mostrar la imagen en la interfaz (reescalada)
                    labelImagenOriginal.setIcon(new ImageIcon(imagenOriginal.getScaledInstance(400, -1, Image.SCALE_SMOOTH)));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al cargar imagen");
                }
            }
        });

        // procesar imagen
        // Se ejecuta cuando el usuario hace clic en "Procesar"
        btnProcesar.addActionListener(e -> {
            // Verificar que hay una imagen cargada
            if (imagenOriginal == null) {
                JOptionPane.showMessageDialog(this, "Primero cargue una imagen.");
                return;
            }
            
            // se hace con las configuraciones escogidas previamente
            int[][] elemento = obtenerElementoEstructurante(comboElemento.getSelectedIndex());  
            String algoritmo = (String) comboAlgoritmo.getSelectedItem(); 
            String modo = (String) comboModo.getSelectedItem();           
            
            try {
                long inicio = System.currentTimeMillis();  
                
                // ejecucion del algoritmo
                if (modo.equals("Secuencial")) {
                    // modo secuencial
                    if (algoritmo.equals("Erosión")) {
                        imagenProcesada = Morfologia.erosionSecuencial(imagenOriginal, elemento);
                    } else {  // dilatacion
                        imagenProcesada = Morfologia.dilatacionSecuencial(imagenOriginal, elemento);
                    }
                } else {
                    // MODO paralelo
                    int hilos = Integer.parseInt(txtHilos.getText());  // Obtener número de hilos escogidos
                    if (algoritmo.equals("Erosión")) {
                        imagenProcesada = Morfologia.erosionParalela(imagenOriginal, elemento, hilos);
                    } else {  // dilatacion
                        imagenProcesada = Morfologia.dilatacionParalela(imagenOriginal, elemento, hilos);
                    }
                }
                
                long fin = System.currentTimeMillis();  // tiempo cuando termina
                
                // tiempo del procesamiento
                JOptionPane.showMessageDialog(this, "Procesamiento terminado en " + (fin - inicio) + " ms");
                
                // imagen procesada en la interfaz
                labelImagenProcesada.setIcon(new ImageIcon(imagenProcesada.getScaledInstance(400, -1, Image.SCALE_SMOOTH)));
            } catch (Exception ex) {
                // Mostrar mensaje de error 
                JOptionPane.showMessageDialog(this, "Error al procesar imagen: " + ex.getMessage());
            }
        });

        // guardar resultado
        btnGuardar.addActionListener(e -> {
            if (imagenProcesada == null) {
                JOptionPane.showMessageDialog(this, "Primero procese una imagen.");
                return;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            
            // dialogo de guardar archivo
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

    /**
     * Método que devuelve el elemento estructurante según la opción seleccionada

     * @param opcion Índice del elemento estructurante seleccionado (0-4)
     * @return Matriz 2D que representa el elemento estructurante
     */
    private int[][] obtenerElementoEstructurante(int opcion) {
        switch (opcion) {
            case 0:  // CRUZ - Forma de cruz (+ shape)
                return new int[][]{{0,1,0},
                                  {1,1,1},
                                  {0,1,0}};
            case 1:  // CUADRADO 3x3 - Cuadrado completo
                return new int[][]{{1,1,1},
                                  {1,1,1},
                                  {1,1,1}};
            case 2:  // X - Forma de X diagonal
                return new int[][]{{1,0,1},
                                  {0,1,0},
                                  {1,0,1}};
            case 3:  // HORIZONTAL - Línea horizontal
                return new int[][]{{1,1,1}};
            case 4:  // VERTICAL - Línea vertical
                return new int[][]{{1},
                                  {1},
                                  {1}};
            default: // Elemento por defecto - Un solo píxel
                return new int[][]{{1}};
        }
    }

    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(() -> {
            GUI ventana = new GUI();     
            ventana.setVisible(true);   
        });
    }
} ¡
