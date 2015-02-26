/**
 * Juego
 *
 * Juego en el que la changuita Barra debera de eliminar a los fantasmas 
 * y evitar a todos los juanitos!
 *
 * @author Diego Ponce and Viridiana 
 * @version 2.0
 * @date 18/02/2015
 */

package bbgame;

import javax.swing.JFrame;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.LinkedList;

public class bbGame extends JFrame implements Runnable, KeyListener {

    private final int iMAXANCHO = 10; // maximo numero de personajes por ancho
    private final int iMAXALTO = 8;  // maxuimo numero de personajes por alto
    private Base basBarra;         // Objeto de la clase base
    private LinkedList<Base> lklBarriles;    //Lista de objetos de la clase Base
    private int iVidas;     //numero de vidas
    private int iPuntos;    //numero de puntos acumulados
    private int iDireccion; //define la direccion del movimiento
    private int iContadorBarriles;  //cuenta la colision de juanitos con Barra
    private boolean bPausa; //pausa para el juego
    private boolean bGameStarted;  //Booleana para saber si el juego comenzó o no
    private boolean bEscape;    //escape termina el juego
    private static final int WIDTH = 550;    //Ancho del JFrame
    private static final int HEIGHT = 700;    //Alto del JFrame
    private Image imaImagenInicio;   // Imagen game over
    private Image imaImagenGameOver;   // Imagen game over
    private String nombreArchivo;    //Nombre del archivo.
    private String[] arr;    //Arreglo del archivo divido.
    private int iNumeroBarriles;    //numero de fantasmas
    
    
    /* objetos para manejar el buffer del Jframe y este no parpadee */
    private Image    imaImagenApplet;   // Imagen a proyectar en JFrame
    private Graphics graGraficaApplet;  // Objeto grafico de la Imagen
    private SoundClip adcSonidoBarril;   // Objeto sonido de Juanito
    
    /** 
     * ExamenAppJFrame
     * 
     * Constructor de la clase <code>ExamenAppJFrame</code>.<P>
     * En este metodo se crea una instancia de la clase 
     * <code>ExamenAppJFrame</code>
     * 
     */
    public bbGame() {        
        //inicializamos la variable que checa si ya empezó el juego en falso.
        bGameStarted = false;
        
        //Se define el nuero de vidas entre 3 y 5 de la changuita
        iVidas = (int) (Math.random() * 3) + 3;
        
        //Se inician los puntos en 0
        iPuntos = 0;
        
        //se inicia el movimiento en ninguna direccion (inexistente)
        iDireccion = -1;
        
        //se inicia contador en 0
        iContadorBarriles = 0;
        
        //se inicia sin pausa el juego
        bPausa = false;
        
        //se inicia si terminar el juego
        bEscape = false;
        
        //nombrar archivo 
        nombreArchivo = "SaveJuego.txt";
        
        //definir imagen de Inicio del juego
        URL urlImagenInicio = this.getClass().getResource("start.png");
        imaImagenInicio = Toolkit.getDefaultToolkit().getImage(urlImagenInicio);
        
        //definir imagen de game over
        URL urlImagenGameOver = this.getClass().getResource("gameOver.gif");
        imaImagenGameOver = Toolkit.getDefaultToolkit().getImage(urlImagenGameOver);
        
        // se posiciona Barra
    int iPosX = (int) (Math.random() *(WIDTH / 4)) + WIDTH / 2;    
        int iPosY = (int) (Math.random() *(HEIGHT / 4)) + HEIGHT / 2;
        
        //Definir imagenen para Barra
    URL urlImagenBarra = this.getClass().getResource("barra.png");
                
        //se crea el objeto para Barra
    basBarra = new Base(iPosX, iPosY, WIDTH / iMAXANCHO,
                  HEIGHT / iMAXALTO,
                    Toolkit.getDefaultToolkit().getImage(urlImagenBarra));
        
        //se reposiciona a Barra  en el piso del Applet y al centro
        basBarra.setX(WIDTH / 2 - basBarra.getAncho() / 2);
        basBarra.setY(HEIGHT / 1 - basBarra.getAlto() / 2);
        
        //defino la imagen de los Juanitos
        URL urlImagenBarril = this.getClass().getResource("barril.png");
        
        // se posiciona a Juanito 
        iPosX = (iMAXANCHO - 1) * WIDTH / iMAXANCHO;
        iPosY = (iMAXALTO - 1) * HEIGHT / iMAXALTO;    
        
        //se crea la lista de juanitos
        lklBarriles = new LinkedList();
        
        // genero un numero azar de 10 a 15
        iNumeroBarriles = (int) (Math.random() * 6) + 10;
       
        // genero cada juanito y lo añado a la lista
        for (int iI = 0; iI < iNumeroBarriles; iI ++) {
            // se crea el objeto para juanito
            Base basBarril = new Base(iPosX,iPosY, WIDTH / iMAXANCHO,
                        HEIGHT / iMAXALTO,
                        Toolkit.getDefaultToolkit().getImage(urlImagenBarril));
            basBarril.setX((int) (Math.random() * 
                                    (WIDTH - basBarril.getAncho())));   
            basBarril.setY(-basBarril.getAlto() + 
                                -((int) (Math.random() * HEIGHT)));
            lklBarriles.add(basBarril);
        }
        
        
        //Creo el sonido de colision entre juanito y Barra
        URL urlSonidoBarril = this.getClass().getResource("rebote.wav");
        adcSonidoBarril = new SoundClip("rebote.wav");
        
        /* se le añade la opcion al JFrame de ser escuchado por los eventos
           del teclado  */
        addKeyListener(this);
        
        // Declaras un hilo
        Thread th = new Thread (this);
        // Empieza el hilo
        th.start ();
    }

     /** 
     * run
     * 
     * Metodo sobrescrito de la clase <code>Thread</code>.<P>
     * En este metodo se ejecuta el hilo, que contendrá las instrucciones
     * de nuestro juego.
     * 
     */
    public void run () {
        /* mientras dure el juego, se actualizan posiciones de jugadores
           se checa si hubo colisiones para desaparecer jugadores o corregir
           movimientos y se vuelve a pintar todo
        */ 
        while (iVidas > 0 && !bEscape) {
            //si la pausa no esta activada
            if (!bPausa) {
                actualiza();
                checaColision();
            }
            repaint();
            try {
                // El thread se duerme.
                Thread.sleep (20);
            }
            catch (InterruptedException iexError) {
                System.out.println("Hubo un error en el juego " + 
                        iexError.toString());
            }
        }
    }
    
    /** 
     * actualiza
     * 
     * Metodo que actualiza la posicion de los objetos 
     * 
     */
    public void actualiza() {
        //Dependiendo de la iDireccion de Barra es hacia donde se mueve.
        switch (iDireccion) {
            case 1: { //se mueve hacia la izquierda
                basBarra.setX(basBarra.getX() - 5);
                break;   
            }
            case 2: { //se mueve hacia la derecha
                basBarra.setX(basBarra.getX() + 5);
                break;    
            }
        }
        
        // ciclo para mover cada juanito de la lista
        for (Base basBarril : lklBarriles) {
            //velocidad definida y se acelera dependiendo de las vidas
            int iVelocidad = 6 - iVidas;
            basBarril.setY(basBarril.getY() + iVelocidad);
        }
        
    }
    
    /**
     * checaColision
     * 
     * Metodo usado para checar la colision entre objetos
     * 
     */
    public void checaColision() {
        //Colision de Barra con el JFrame dependiendo a donde se mueve.
        //Barra colisiona con el lado izquierdo
        if (basBarra.getX() < 0) {
            basBarra.setX(0);    //Barra no sale
        }
        //Barra esta colisionando con el lado derecho
        if (basBarra.getX() + basBarra.getAncho() > WIDTH) {
            basBarra.setX(WIDTH - basBarra.getAncho()); /*Barra
                                                                no sale*/
        }
        //Barra esta colsionando con el lado superior
        if (basBarra.getY() < 25) { 
            basBarra.setY(25);     //Barra no sale
        }
        //Barra esta colisionando con el lado inferior
        if (basBarra.getY() + basBarra.getAlto() > HEIGHT) { 
            basBarra.setY(HEIGHT - basBarra.getAlto()); /*Barra
                                                               no sale*/
        }
        
        // ciclo para revisar colision de los juanitos
        for (Base basBarril : lklBarriles) {
            //checo la colision con Barra
            if (basBarra.intersecta(basBarril)) {
                //Reubicar a juanito
                basBarril.setX((int) (Math.random() * 
                                    (WIDTH - basBarril.getAncho())));   
                basBarril.setY(-basBarril.getAlto() + 
                                -((int) (Math.random() * HEIGHT)));  
                //se suma una colision al contador
                iContadorBarriles++;
                //si ya colisionaron 5
                if (iContadorBarriles >= 5) {
                    iVidas--;   //se resta una vida
                    iContadorBarriles = 0;
                }
                adcSonidoBarril.play(); //sonido al colisionar con Barra
            }
            
            //si colisiono con la parte de abajo del JFrame
            if ((basBarril.getY() + basBarril.getAlto()) > HEIGHT) {
                //Reubicar a juanito
                basBarril.setX((int) (Math.random() * 
                                    (WIDTH - basBarril.getAncho())));   
                basBarril.setY(-basBarril.getAlto() + 
                                -((int) (Math.random() * HEIGHT)));      
            }
        }
        
    }
    
    /**
     * paint
     * 
     * Metodo sobrescrito de la clase <code>JFrame</code>,
     * heredado de la clase Container.<P>
     * En este metodo lo que hace es actualizar el contenedor y 
     * define cuando usar ahora el paint
     * 
     * @param graGrafico es el <code>objeto grafico</code> usado para dibujar.
     * 
     * @param graDibujo es el objeto de <code>Graphics</code> usado para dibujar.
     * 
     */
    public void paint(Graphics graGrafico) {
        
        // Inicializan el DoubleBuffer
        if (imaImagenApplet == null){
            imaImagenApplet = createImage (this.getSize().width, 
                                this.getSize().height);
            graGraficaApplet = imaImagenApplet.getGraphics ();
        }
        
        // Actualiza la imagen de fondo.
        URL urlImagenFondo = this.getClass().getResource("fondo.png");
        Image imaImagenFondo = Toolkit.getDefaultToolkit().getImage(urlImagenFondo);
         graGraficaApplet.drawImage(imaImagenFondo, 0, 0, WIDTH, HEIGHT, this);
        
        // Actualiza el Foreground.
        graGraficaApplet.setColor (getForeground());
        paint1(graGraficaApplet);
        
        // Dibuja la imagen actualizada
        graGrafico.drawImage (imaImagenApplet, 0, 0, this);
    }
    
    /**
     * paint1
     * 
     * Metodo sobrescrito de la clase <code>JFrame</code>,
     * heredado de la clase Container.<P>
     * En este metodo se dibuja la imagen con la posicion actualizada,
     * ademas que cuando la imagen es cargada te despliega una advertencia.
     * 
     * @param graGrafico es el <code>objeto grafico</code> usado para dibujar.
     * 
     */
    public void paint1 (Graphics graDibujo) {
        //Si todavia hay vidas en el juego o no ESCAPE
        
        
        
        if (iVidas > 0 && !bEscape) {
            // si la imagen ya se cargo
            if (basBarra != null && lklBarriles != null) {
                //Dibuja la imagen de principal en el JFrame
                basBarra.paint(graDibujo, this);

                // pinto cada Juanito de la lista
                for (Base basBarril : lklBarriles) {
                    //Dibuja la imagen de juanito en el JFrame
                    basBarril.paint(graDibujo, this);
                }

                //Dibujar vidas restantes
                graDibujo.setFont(new Font("Arial", Font.BOLD, 20));
                graDibujo.setColor(Color.red);
                String sVidasDisplay = "Vidas: " + Integer.toString(iVidas);
                graDibujo.drawString(sVidasDisplay, 690, 50);

                //Dibujar puntos
                String sPuntosDisplay = "Puntos: " + Integer.toString(iPuntos);
                graDibujo.drawString(sPuntosDisplay, 690, 80);

            } // sino se ha cargado se dibuja un mensaje 
            else {
                //Da un mensaje mientras se carga el dibujo 
                graDibujo.drawString("No se cargo la imagen..", 20, 20);
            }
        }
        
        else {
            // Dibuja game over
            graDibujo.setColor(Color.black);
            graDibujo.fillRect(0, 0, WIDTH, HEIGHT);
            //Dibujar imagenen
            graDibujo.drawImage (imaImagenGameOver, 
                           ((WIDTH / 2) - imaImagenGameOver.getWidth(this) / 2), 
                           ((HEIGHT / 2) - imaImagenGameOver.getHeight(this) / 2) 
                           ,this);
            //dibujar puntos
            graDibujo.setColor(Color.white);
            String sPuntosDisplay = "Puntos: " + Integer.toString(iPuntos);
            graDibujo.drawString(sPuntosDisplay, ((WIDTH / 2) - 50), 
                                    ((HEIGHT / 2) + 100));
        }
        
    }

    /**
     * keyTyped
     * 
     * Metodo sobrescrito de la interface <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al presionar una 
     * tecla que no es de accion.
     * 
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al 
     * presionar.
     * 
     */
    public void keyTyped(KeyEvent keyEvent) {
        // no hay codigo pero se debe escribir el metodo
    }

    /**
     * keyPressed
     * 
     * Metodo sobrescrito de la interface <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al dejar presionada
     * alguna tecla.
     * Se cambia la direccion al presionar una tecla
     * 
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al 
     * presionar.
     * 
     */
    public void keyPressed(KeyEvent keyEvent) {
        // si presiono la tecla A
        if(keyEvent.getKeyCode() == KeyEvent.VK_A || keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {    
            iDireccion = 1;
        }
        // si presiono la tecla D
        else if(keyEvent.getKeyCode() == KeyEvent.VK_D || keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {    
            iDireccion = 2;
        }
    }

    /**
     * keyReleased
     * Metodo sobrescrito de la interface <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al soltar la tecla.
     * 
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al soltar.
     * 
     */
    public void keyReleased(KeyEvent keyEvent) {
        
        // si presiono la tecla A
        if(keyEvent.getKeyCode() == KeyEvent.VK_A || keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {    
            iDireccion = -1;
        }
        // si presiono la tecla D
        else if(keyEvent.getKeyCode() == KeyEvent.VK_D || keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {    
            iDireccion = -1;
        }
        // si presiono la tecla P
        if(keyEvent.getKeyCode() == KeyEvent.VK_P) {  
            if (!bPausa) {
                bPausa = true;
            }
            else {
                bPausa = false;
            }
        }
        // si presiono la tecla escape
        else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {  
            bEscape = true;
        }
        // si presiono la tecla G
        else if(keyEvent.getKeyCode() == KeyEvent.VK_G) {  
            try{
                guardaArchivo();    //Graba el vector en el archivo.
            }catch(IOException e){
                System.out.println("Error en " + e.toString());
            }
        }
        // si presiono la tecla C
        else if(keyEvent.getKeyCode() == KeyEvent.VK_C) {  
            try{
                leeArchivo();    //Graba el vector en el archivo.
            }catch(IOException e){
                System.out.println("Error en " + e.toString());
            }
        }
    }
    
    /*
     * Guardar
     * 
     * Metodo sobrescrito de la clase <code>JFrame</code>,
     * heredado de la clase Container.<P>
     * En este metodo se guarda en un archivo de texto las posiciones y valores
     * de todo el juego
     * 
     */
    public void guardaArchivo()  throws IOException{
       PrintWriter prwArchivo = new PrintWriter(new 
                                        FileWriter("savedgame.txt"));
        prwArchivo.println(iVidas); //Se imprimen las vidas
        prwArchivo.println(iPuntos); //Se imprime el score
        prwArchivo.println(iDireccion); //Se imprime la direccion de chimp
        prwArchivo.println(iVidas);//Se imprime la velocidad de juan
        prwArchivo.println(iContadorBarriles);//Se imprime la cantidad de colisiones
        //Se imprime la posición de Barra en la misma línea
        prwArchivo.println(basBarra.getX() + " " + basBarra.getY());
        //Se guarda la cantidad de caminadores y las posiciones
        prwArchivo.println(lklBarriles.size());
        for (Base basMalo : lklBarriles){
            prwArchivo.println(basMalo.getX() + " " + basMalo.getY());
        }
        
        prwArchivo.close(); //Se cierra el archivo 
    }
    /*
     * Cargar
     * 
     * Metodo sobrescrito de la clase <code>JFrame</code>,
     * heredado de la clase Container.<P>
     * En este metodo se carga de un archivo de texto, todas las posiciones
     * de todo el juego
     * 
     */
    public void leeArchivo() throws IOException{
        BufferedReader brwEntrada; //Archivo entrada
        try{
            brwEntrada = new BufferedReader(new FileReader("savedgame.txt"));
        } catch (FileNotFoundException e){
            guardaArchivo();
            brwEntrada = new BufferedReader(new FileReader("savedgame.txt"));
        }
        String sAux = ""; //Se inicializa un string auxiliar como vacío
        //Se lee y carga la línea que contiene las vidas
        iVidas = Integer.parseInt(brwEntrada.readLine()); 
        //Se lee y carga la línea que contiene el score
        iPuntos = Integer.parseInt(brwEntrada.readLine());
        //Se lee y carga la línea que contiene la dirección
        iDireccion = Integer.parseInt(brwEntrada.readLine());
        //Se lee y carga la velocidad del juanito
        iVidas = Integer.parseInt(brwEntrada.readLine());
        //Se lee y carga la cantidad de colisiones hasta el momento
        iContadorBarriles = Integer.parseInt(brwEntrada.readLine());
        //Se lee y carga si esta en pausa o no
        bPausa = true;//Boolean.parseBoolean(brwEntrada.readLine());
        //Se elimina el personaje Barra y se vuelve a crear
        sAux = brwEntrada.readLine(); //Se lee la línea en una variable auxiliar
        basBarra.setX(Integer.parseInt(sAux.substring(0,sAux.indexOf(" "))));
        basBarra.setY(Integer.parseInt(sAux.substring(sAux.indexOf(" ")+1)));
        //Se lee la cantidad de juanitos y se guarda en un auxiliar entero
        lklBarriles.clear(); //Se limpia la lista de Juanitos
        lklBarriles = new LinkedList(); //Se vuelve a crear la lista
        int iAux = Integer.parseInt(brwEntrada.readLine());
        for (int iI = 0; iI < iAux; iI ++){
            sAux = brwEntrada.readLine();
            URL urlImagenBarril = this.getClass().
                    getResource("juanito.gif");
            //Se crea el personaje ya con las posiciones
            Base basMalo = new Base(Integer.parseInt
            (sAux.substring(0,sAux.indexOf(" "))),Integer.parseInt
            (sAux.substring(sAux.indexOf(" ")+1)), getWidth() / iMAXANCHO,
                    getHeight() / iMAXALTO, Toolkit.getDefaultToolkit().
                                            getImage(urlImagenBarril));
            lklBarriles.add(basMalo); //Se añade personaje a la lista
        }
       
    }
    
    /**
     * main
     * Metodo......
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        bbGame juego = new bbGame();
        juego.setSize(WIDTH, HEIGHT);
        juego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        juego.setVisible(true);
    }
}
