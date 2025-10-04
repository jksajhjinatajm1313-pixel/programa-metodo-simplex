import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase implementa el Método Simplex para resolver problemas de programación lineal.
 * El código está diseñado para ser lo más claro y didáctico posible, explicando cada
 * paso del algoritmo.
 *
 * @author Angeline Cazarez Ortega :)
 */
public class Simplex {

    private double[][] tableau; // Nuestra tabla principal de operaciones
    private int numRestricciones;
    private int numVariables;

    // Listas para guardar las variables que están en la base y las que no.
    private List<Integer> basicas;
    private List<Integer> noBasicas;

    /**
     * Constructor para inicializar el problema Simplex.
     * @param funcionObjetivo Coeficientes de la función a maximizar (ej: Z = 3x1 + 5x2)
     * @param restricciones Matriz con los coeficientes de las restricciones.
     * @param ladoDerecho Los valores del lado derecho de las restricciones (los recursos).
     */
    public Simplex(double[] funcionObjetivo, double[][] restricciones, double[] ladoDerecho) {
        numRestricciones = restricciones.length;
        numVariables = funcionObjetivo.length;
        basicas = new ArrayList<>();
        noBasicas = new ArrayList<>();

        // Creamos el tableau con espacio para la función objetivo, las restricciones,
        // las variables de holgura y la columna de soluciones (RHS).
        tableau = new double[numRestricciones + 1][numVariables + numRestricciones + 1];

        // 1. Llenamos la fila de la función objetivo (fila 0)
        // Se colocan los coeficientes de la función objetivo como negativos
        // porque Z - 3x1 - 5x2 = 0
        tableau[0][0] = 1; // Coeficiente para Z
        for (int j = 0; j < numVariables; j++) {
            tableau[0][j + 1] = -funcionObjetivo[j];
        }

        // 2. Llenamos las filas de las restricciones
        for (int i = 0; i < numRestricciones; i++) {
            for (int j = 0; j < numVariables; j++) {
                tableau[i + 1][j + 1] = restricciones[i][j];
            }
            // Agregamos las variables de holgura (una por cada restricción)
            tableau[i + 1][numVariables + i + 1] = 1;
            // Agregamos el lado derecho (RHS)
            tableau[i + 1][numVariables + numRestricciones] = ladoDerecho[i];
        }

        // 3. Inicializamos las variables básicas y no básicas
        for (int i = 0; i < numVariables; i++) {
            noBasicas.add(i + 1); // Las variables originales (x1, x2, ...) son no básicas
        }
        for (int i = 0; i < numRestricciones; i++) {
            basicas.add(numVariables + i + 1); // Las de holgura (s1, s2, ...) son básicas
        }
    }

    /**
     * El corazón del algoritmo. Itera hasta encontrar la solución óptima.
     */
    public void resolver() {
        int iteracion = 1;
        while (!esSolucionOptima()) {
            System.out.println("\n--- Iteración #" + iteracion++ + " ---");
            imprimirTableau();

            int columnaPivote = encontrarColumnaPivote();
            int filaPivote = encontrarFilaPivote(columnaPivote);

            System.out.println("Columna Pivote (entra variable X" + noBasicas.get(columnaPivote-1) + ")");
            System.out.println("Fila Pivote (sale variable X" + basicas.get(filaPivote-1) + ")");
            
            // Realizamos la operación de pivote para actualizar el tableau
            pivotear(filaPivote, columnaPivote);
        }

        System.out.println("\n--- Solución Óptima Encontrada ---");
        imprimirTableau();
        mostrarResultados();
    }

    /**
     * Imprime el tableau actual en la consola de forma ordenada.
     */
    private void imprimirTableau() {
        System.out.print("Base\t");
        for (int j = 0; j < tableau[0].length -1; j++) {
             if (j==0) System.out.print("Z\t");
             else if (j <= numVariables) System.out.print("x" + j + "\t");
             else System.out.print("s" + (j - numVariables) + "\t");
        }
        System.out.println("RHS");
        
        for (int i = 0; i < tableau.length; i++) {
             if (i==0) System.out.print("Z\t");
             else System.out.print("s" + (basicas.get(i-1) - numVariables) + "\t");
            for (int j = 0; j < tableau[0].length; j++) {
                System.out.printf("%.2f\t", tableau[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Verifica si hemos llegado a la solución óptima.
     * Esto ocurre cuando no hay coeficientes negativos en la fila de la función objetivo.
     * @return true si es óptima, false en caso contrario.
     */
    private boolean esSolucionOptima() {
        for (int j = 1; j < tableau[0].length - 1; j++) {
            if (tableau[0][j] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Encuentra la columna del elemento pivote.
     * Es la columna con el valor más negativo en la primera fila (función objetivo).
     * @return El índice de la columna pivote.
     */
    private int encontrarColumnaPivote() {
        int columnaPivote = 1;
        double valorMinimo = tableau[0][1];
        for (int j = 2; j < tableau[0].length - 1; j++) {
            if (tableau[0][j] < valorMinimo) {
                valorMinimo = tableau[0][j];
                columnaPivote = j;
            }
        }
        return columnaPivote;
    }

    /**
     * Encuentra la fila del elemento pivote.
     * Se calcula el cociente entre el lado derecho (RHS) y el valor de la columna pivote.
     * La fila elegida es la que tiene el menor cociente positivo.
     * @param columnaPivote El índice de la columna pivote.
     * @return El índice de la fila pivote.
     */
    private int encontrarFilaPivote(int columnaPivote) {
        int filaPivote = -1;
        double ratioMinimo = Double.MAX_VALUE;

        for (int i = 1; i < tableau.length; i++) {
            // Solo consideramos filas con valores positivos en la columna pivote
            if (tableau[i][columnaPivote] > 0) {
                double ratio = tableau[i][tableau[0].length - 1] / tableau[i][columnaPivote];
                if (ratio < ratioMinimo) {
                    ratioMinimo = ratio;
                    filaPivote = i;
                }
            }
        }
        return filaPivote;
    }

    /**
     * Realiza la operación de pivoteo para actualizar el tableau.
     * @param filaPivote Índice de la fila pivote.
     * @param columnaPivote Índice de la columna pivote.
     */
    private void pivotear(int filaPivote, int columnaPivote) {
        double elementoPivote = tableau[filaPivote][columnaPivote];

        // 1. Normalizamos la fila pivote (la dividimos por el elemento pivote)
        // para que el elemento pivote se convierta en 1.
        for (int j = 0; j < tableau[0].length; j++) {
            tableau[filaPivote][j] /= elementoPivote;
        }

        // 2. Hacemos cero los otros elementos en la columna pivote
        // usando operaciones de fila.
        for (int i = 0; i < tableau.length; i++) {
            if (i != filaPivote) {
                double factor = tableau[i][columnaPivote];
                for (int j = 0; j < tableau[0].length; j++) {
                    tableau[i][j] -= factor * tableau[filaPivote][j];
                }
            }
        }

        // Actualizamos las variables básicas y no básicas
        int varEntra = noBasicas.get(columnaPivote - 1);
        int varSale = basicas.get(filaPivote - 1);

        basicas.set(filaPivote - 1, varEntra);
        noBasicas.set(columnaPivote - 1, varSale);
    }
    
    /**
     * Muestra los resultados finales de una manera clara y legible.
     */
    private void mostrarResultados() {
        // El valor óptimo de Z está en la primera fila, última columna.
        System.out.printf("El valor máximo de Z es: %.2f\n", tableau[0][tableau[0].length - 1]);

        // Buscamos el valor de las variables originales
        for (int i = 1; i <= numVariables; i++) {
            boolean esBasica = false;
            for(int k=0; k < basicas.size(); k++){
                if(basicas.get(k) == i){
                     System.out.printf("El valor de x%d es: %.2f\n", i, tableau[k + 1][tableau[0].length - 1]);
                     esBasica = true;
                }
            }
            if(!esBasica){
                 System.out.printf("El valor de x%d es: 0.00\n", i);
            }
        }
    }


    /**
     * Método principal (main) para ejecutar el programa.
     * Aquí definimos un problema de ejemplo para resolver.
     */
    public static void main(String[] args) {
        // --- EJEMPLO DE UN PROBLEMA ---
        // Maximizar Z = 3x1 + 5x2
        // Sujeto a las restricciones:
        // 1.  x1 <= 4
        // 2. 2x2 <= 12
        // 3. 3x1 + 2x2 <= 18
        // x1, x2 >= 0

        System.out.println("Resolviendo un problema de Programación Lineal con el Método Simplex.");
        
        // Coeficientes de la función objetivo [3, 5]
        double[] funcionObjetivo = {3, 5};

        // Matriz de coeficientes de las restricciones
        double[][] restricciones = {
            {1, 0},  // Para x1 <= 4
            {0, 2},  // Para 2x2 <= 12
            {3, 2}   // Para 3x1 + 2x2 <= 18
        };

        // Lado derecho de las restricciones (RHS)
        double[] ladoDerecho = {4, 12, 18};

        // Creamos el objeto Simplex y lo resolvemos
        Simplex simplex = new Simplex(funcionObjetivo, restricciones, ladoDerecho);
        simplex.resolver();
    }
}