/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.util.Scanner;

/**
 *
 * @author John Haste
 */
public class Main {
    public static void main(String[] args) {
        
        Scanner teclado = new Scanner(System.in);
        int porta = teclado.nextInt();
        Usuario a = new Usuario("Joaozinho Da Padaria",porta);
        a.SendOlah();
    }
}
