/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.File;
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

        try {
            Thread.sleep(5500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        while(true){
            String nomeArq;
            teclado = new Scanner(System.in);
            System.out.println("Digite o nome do arquivo que deseja ou Q para sair:");
            nomeArq = teclado.nextLine();
            if(nomeArq.equals("Q"))
                break;
            else if(a.listaDeArquivos.contains(nomeArq)){
                System.out.println("VOCÊ JÁ POSSUI ESSE ARQUIVO. TENTE NOVAMENTE");
                continue;
            }
            if(a.TemMaisUsuarios())
                a.PedeArquivo(nomeArq);
            else
                System.out.println("VOCÊ ESTÁ SOZINHO NESSE ROLÊ.");
        }
        a.conexao_multicast.interrupt();
    }
}
