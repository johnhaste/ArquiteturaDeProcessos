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
        
        //Lê o nome do usuário
        Scanner teclado = new Scanner(System.in);
        System.out.println("Digite um nome de usuário");
        
        //Lê a porta que será usada
        String nomeUser = teclado.nextLine();
        System.out.println("Digite uma porta para acessar o Sonin Bleinin Torrent");
        int porta = teclado.nextInt();
        
        //Cria um novo processo para o usuário
        ProcessoUsuario a = new ProcessoUsuario(nomeUser,porta);
        
        //Envia um "oi" para a rede, para os outros adicionarem ele
        a.SendOlah("MULTICAST",porta);
        
        for(int i = 0;i<1000000;i++){}
        
        a.TestaCriptografia();
        if(a.getNome_usuario().equals("Gui")){
            System.out.println("PEDINDO...");
            a.PedeArquivo("Senhor dos Aneis.mp4");
        }
        
    }
}
