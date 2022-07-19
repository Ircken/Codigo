import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ValorMaximo {

    static boolean existeDirectorio(String carpeta) {

        boolean flag = true;

        File f = new File(carpeta);
        if(!f.exists() && !f.isDirectory()) {
            System.out.println("No existe la carpeta 'procesar' con los datos");
            flag = false;
        } else {
            System.out.println("Carpeta 'procesar' encontrada.");
        }

        return flag;
    }

    static boolean existeDocumento(String archivoFecha){


        boolean flag = true;

        if(!Paths.get(archivoFecha).toFile().isFile()) {
            System.out.println("No existe el archivo 'fecha.txt'");
            System.out.println("La informacion dentro de archivo '.txt' debe tener el formato -> 31/05/22 20:30:00");
            flag = false;
        } else {
            System.out.println("Archivo 'fecha.txt' encontrado");
        }

        return flag;

    }

    static String obtenerEntrada(String archivoFecha) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(archivoFecha));
        String linea = bufferedReader.readLine();
        bufferedReader.close();

        return linea;
    }

    static boolean entradaCorrecta(String cadena) {

        int longitud = cadena.length();                              // debe ser 17
        int cantidadBarrasCadena = cadena.length() - cadena.replace("/", "").length();    // debe ser 2
        int cantidadDosPuntosCadena = cadena.length() - cadena.replace(":", "").length();  // debe ser 2
        int cantidadEspaciosCadena = cadena.length() - cadena.replace(" ", "").length();   // debe ser 1
        boolean flag = false;

        if (longitud == 17) {
            if (cantidadBarrasCadena == 2) {
                if (cantidadDosPuntosCadena == 2) {
                    if (cantidadEspaciosCadena == 1) {
                        flag = true;
                    }
                }
            }
        }
        if (flag) {
            System.out.println("Fecha correcta.");
        } else {
            System.out.println("Fecha incorrecta.");
        }

        return flag;
    }

    static List<String> ubicacionArchivos(String carpeta) {

        File folder = new File(carpeta);
        File[] listOfFiles = folder.listFiles();
        List<String> nombresArchivos = new ArrayList<>();

        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {

            nombresArchivos.add(carpeta+"/"+listOfFiles[i].getName());

        }

        System.out.println("Ubicacion de los archivos obtenida.");

        return nombresArchivos;
    }

    static boolean extensionCorrecta(List<String> archivos) {

        boolean flag = true;
        for(String archivo:archivos) {

            if (!archivo.endsWith(".csv")) {
                flag = false;
                System.out.println("Archivos con extension incorrecta. Debe ser '.csv'");
                break;
            }
        }

        System.out.println("Archivos con extension correcta.");

        return flag;
    }

    static List<Integer> conseguirNumeros(List<String> archivos) {

        List<Integer> numeroOrden = new ArrayList<>();

        for(String cursor:archivos) {

            cursor = cursor.replace("procesar/", "");
            cursor = cursor.replace("ReporteRegistros", "");
            cursor = cursor.replace(".csv", "");

            cursor = cursor.replace("(", "");
            cursor = cursor.replace(")", "");
            cursor = cursor.replace(" ", "");

            if (cursor.equals("")){
                numeroOrden.add(0);
            } else {
                numeroOrden.add(Integer.parseInt(cursor));
            }

        }

        System.out.println("Orden de los archivos obtenidos.");

        return numeroOrden;
    }

    static List<String> archivosOrdenados(List<String> archivos, List<Integer> numerosArchivos) {


        List<String> vectorOrdenado = new ArrayList<>();
        while (archivos.size() > 0) {

            int elemento = Collections.min(numerosArchivos);
            int indice = numerosArchivos.indexOf(elemento);

            vectorOrdenado.add(archivos.get(indice));
            numerosArchivos.remove(indice);
            archivos.remove(indice);

        }

        System.out.println("Archivos ordenados.");

        return vectorOrdenado;
    }

    static String lineaArchivo(String archivo, String buscarFechaHora) throws IOException {
        // abrir archivo
        boolean flag = false;
        String lineaGuardar = "";

        File f = new File(archivo);
        FileReader fr=new FileReader(f);   //leer el archivo
        BufferedReader br=new BufferedReader(fr);  //bufer para el lector
        String linea;
        String[] vectorLinea;

        while((linea=br.readLine())!=null) {

            if (!linea.contains(";1/1;")) {

                if (linea.contains("Fecha hora") || linea.contains("Fecha Hora")){
                    flag = true;
                }

                if (flag && linea.startsWith(buscarFechaHora)) {

                    linea = linea.replace(";;;", ";");
                    linea = linea.replace(";;", ";");

                    if (linea.endsWith(";")) {
                        linea = linea.substring(0, linea.length() - 1);
                    }

                    linea = linea.replace(",", ".");
                    linea = linea.replace(";", ",");

                    vectorLinea = linea.split(",");

                    String agregar = vectorLinea[vectorLinea.length-1];

                    // si la ultima linea es alguna de estas
                    if (agregar.endsWith("T0") ||
                            agregar.endsWith("T1") ||
                            agregar.endsWith("T2") ||
                            agregar.endsWith("T3")) {

                        lineaGuardar = archivo + "," + vectorLinea[0] + "," + vectorLinea[vectorLinea.length-2];

                    } else {

                        lineaGuardar = archivo + "," + vectorLinea[0] + "," + vectorLinea[vectorLinea.length-1];

                    }

                }

            }

        }
        fr.close();    //cerrar lector

        return lineaGuardar;
    }

    static void escribir(String nombre, String textoDF) throws IOException {

        File f = new File(nombre);
        FileWriter escritorArchivo = new FileWriter(f);
        escritorArchivo.write(textoDF);
        escritorArchivo.close();

    }

    public static void progressPercentage(int remain, int total) {
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 10; // 10unit for 100%
        int remainProcent = ((100 * remain) / total) / maxBareSize;
        char defaultChar = ' ';
        String icon = "=";
        String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remainProcent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainProcent, bare.length());
        System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
        if (remain == total) {
            System.out.print("\n");
        }
    }

    public static void main(String[] args) throws IOException {


        Scanner scanner = new Scanner(System.in); 

        System.out.println();
        System.out.println("Comenzando..");
        System.out.println();

        String carpeta = "procesar";
        String archivoFecha = "fecha.txt";
        String archivoSalida = "salida.txt";

        if (existeDirectorio(carpeta) && existeDocumento(archivoFecha)) {
            String buscarFechaHora = obtenerEntrada(archivoFecha);

            if (entradaCorrecta(buscarFechaHora)) {
                List<String> archivos = ubicacionArchivos(carpeta);

                if (extensionCorrecta(archivos)) {

                    List<Integer> numerosArchivos = conseguirNumeros(archivos);                    
                    archivos = archivosOrdenados(archivos, numerosArchivos);

                    String textoAcumulado = "";

                    System.out.println();
                    System.out.println("Escribiendo..");
                    progressPercentage(0, archivos.size());
                    int contadorBarra = 0; 

                    for (String archivo:archivos) {
                        textoAcumulado += lineaArchivo(archivo, buscarFechaHora) + "\n";

                        contadorBarra += 1;
                        progressPercentage(contadorBarra, archivos.size());

                    }

                    escribir(archivoSalida, textoAcumulado.replace(",", "\t")+"Escritura finalizada.");

                }

            }

        }

        System.out.println();
        System.out.println("Tarea finalizada.");
        System.out.println();
        System.out.println("Pesiona ENTER para finalizar.");
        scanner.nextLine();
        scanner.close();
    }


}

// javac file.java
// jar -cfvm ValorMaximo.jar Manifest.mf ValorMaximo.class

// C:\Owl\Util\Programed\javaMaximoValor\src\javaMaximoValor.jar

// java -jar javaMaximoValor.jar