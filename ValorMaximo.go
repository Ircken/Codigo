package main

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"sort"
	"strconv"
	"strings"

	"github.com/schollz/progressbar/v3"
)

func ubicacionArchivos(carpeta string) (archivosDirectorio []string) {

	//vector archivos
	archivos, err := ioutil.ReadDir(carpeta)
	if err != nil {
		log.Fatal(err)
	}

	// reocrro cada elemento de archivos
	for _, archivo := range archivos {
		archivosDirectorio = append(archivosDirectorio, carpeta+"/"+archivo.Name())
	}

	return
}

func lineaArchivo(archivo, buscarFechaHora string) string {
	// abrir archivo
	var flag = false
	var lineaGuardar = ""

	f, err := os.Open(archivo)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {

		linea := scanner.Text()

		if !strings.Contains(linea, ";1/1;") {

			if strings.Contains(linea, "Fecha hora") || strings.Contains(linea, "Fecha Hora") {
				flag = true
			}

			if flag && strings.HasPrefix(linea, buscarFechaHora) {

				linea = strings.ReplaceAll(linea, ";;;", ";")
				linea = strings.ReplaceAll(linea, ";;", ";")

				linea = strings.TrimSuffix(linea, ";")

				linea = strings.ReplaceAll(linea, ",", ".")
				linea = strings.ReplaceAll(linea, ";", ",")

				vectorLinea := strings.Split(linea, ",")

				agregar := vectorLinea[len(vectorLinea)-1]

				// si la ultima linea es alguna de estas
				if agregar == "T0" ||
					agregar == "T1" ||
					agregar == "T2" ||
					agregar == "T3" {
					lineaGuardar = archivo + "," + vectorLinea[0] + "," + vectorLinea[len(vectorLinea)-2]
				} else {
					lineaGuardar = archivo + "," + vectorLinea[0] + "," + vectorLinea[len(vectorLinea)-1]
				}

			}

		}

	}

	return lineaGuardar
}

func escribir(nombre, textoDF string) {

	f, err := os.Create(nombre)

	if err != nil {
		log.Fatal(err)
	}

	defer f.Close()

	_, err2 := f.WriteString(textoDF)

	if err2 != nil {
		log.Fatal(err2)
	}
}

func existeDirectorio(carpeta string) (flag bool) {

	_, err := os.Stat(carpeta)
	if os.IsNotExist(err) {
		fmt.Println("No existe la carpeta 'procesar' con los datos.")
		flag = false
	} else {
		fmt.Println("Carpeta 'procesar' encontrada.")
		flag = true
	}
	return
}

func existeArchivo(fecha string) (flag bool) {

	_, err := os.Stat(fecha)
	if os.IsNotExist(err) {

		fmt.Println("No existe el archivo 'fecha.txt'.")
		fmt.Println("La informacion dentro de archivo '.txt' debe tener el formato -> 31/05/22 20:30:00")
		flag = false

	} else {
		fmt.Println("Archivo 'fecha.txt' encontrado.")
		flag = true
	}

	return
}

func entradaCorrecta(cadena string) (flag bool) {

	longitud := len(cadena)                               // debe ser 17
	cantidadBarrasCadena := strings.Count(cadena, "/")    // debe ser 2
	cantidadDosPuntosCadena := strings.Count(cadena, ":") // debe ser 2
	cantidadEspaciosCadena := strings.Count(cadena, " ")  // debe ser 1

	if longitud == 17 {
		if cantidadBarrasCadena == 2 {
			if cantidadDosPuntosCadena == 2 {
				if cantidadEspaciosCadena == 1 {
					flag = true
				} else {
					flag = false
				}
			} else {
				flag = false
			}
		} else {
			flag = false
		}
	} else {
		flag = false
	}

	if flag {
		fmt.Println("Fecha correcta.")
	} else {
		fmt.Println("Fecha incorrecta.")
	}
	return
}

func obtenerEntrada(archivoFecha string) (cadenaTexto string) {

	f, err := ioutil.ReadFile(archivoFecha) // nombre del archivo
	if err != nil {
		fmt.Print(err)
	}
	cadenaTexto = string(f) // convertir contenido a cadena

	return
}

func conseguirNumeros(archivos []string) (numeroOrden []int) {

	for _, cursor := range archivos {

		cursor = strings.ReplaceAll(cursor, "(", "")
		cursor = strings.ReplaceAll(cursor, ")", "")
		cursor = strings.ReplaceAll(cursor, " ", "")

		cursor = strings.ReplaceAll(cursor, "procesar/", "")
		cursor = strings.ReplaceAll(cursor, "ReporteRegistros", "")
		cursor = strings.ReplaceAll(cursor, ".csv", "")

		intVar, _ := strconv.ParseInt(cursor, 0, 64)

		numeroOrden = append(numeroOrden, int(intVar))

	}
	return
}

func archivosOrdenados(archivos []string, numerosArchivos []int) (vectorOrdenado []string) {

	m := make(map[string]int) // map
	for indice, archivo := range archivos {
		m[archivo] = numerosArchivos[indice]
	}

	keys := make([]string, 0, len(m)) // array
	for key := range m {
		keys = append(keys, key)
	}

	sort.SliceStable(keys,
		func(i, j int) bool { return m[keys[i]] < m[keys[j]] })

	vectorOrdenado = append(vectorOrdenado, keys...)

	return
}

func extensionCorrecta(archivos []string) (flag bool) {

	flag = true
	for _, archivo := range archivos {
		if !strings.HasSuffix(archivo, ".csv") {
			flag = false
			fmt.Println("Archivos con extension incorrecta.")
			break
		}
	}

	return
}

func main() {

	fmt.Println()
	fmt.Println("Comenzando..")
	fmt.Println()

	carpeta := "procesar"
	archivoFecha := "fecha.txt"
	archivoSalida := "salida.txt"

	if existeDirectorio(carpeta) && existeArchivo(archivoFecha) {

		buscarFechaHora := obtenerEntrada(archivoFecha)
		if entradaCorrecta(buscarFechaHora) {

			archivos := ubicacionArchivos(carpeta)
			if extensionCorrecta(archivos) {

				numerosArchivos := conseguirNumeros(archivos)
				archivos = archivosOrdenados(archivos, numerosArchivos)

				textoAcumulado := ""

				fmt.Println()
				longitud := int64(len(archivos))
				bar := progressbar.Default(longitud)
				for _, archivo := range archivos {
					textoAcumulado += lineaArchivo(archivo, buscarFechaHora) + "\n"
					bar.Add(1)
				}

				finLinea := "Escritura finalizada."
				escribir(archivoSalida, strings.ReplaceAll(textoAcumulado, ",", "\t")+finLinea)
			}

		}

	}
	fmt.Println()
	fmt.Println("Tarea finalizada.")
	fmt.Println()
	fmt.Println("Presione ENTER para finalizar.")
	fmt.Scanln()
	fmt.Println()

}
