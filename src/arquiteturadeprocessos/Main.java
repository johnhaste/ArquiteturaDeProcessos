/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John Haste
 */
public class Main {
        
    public static void main(String[] args) {
     
        //Pré definição para debug
        Scanner teclado = new Scanner(System.in);
        System.out.println("Selecione um usuário");
        System.out.println("1-Lula\n2-Aecio\n3-Dilma");
        switch(teclado.nextInt()){
            case 1:
                ProcessoUsuario a = new ProcessoUsuario("Lula",3333);
                a.SendOlah("MULTICAST",3333);
                iniciaConexao(a);
                //Fecha a conexão
                a.conexao_multicast.interrupt();
                break;
            case 2:
                ProcessoUsuario b = new ProcessoUsuario("Aecio",4444);
                b.SendOlah("MULTICAST",4444);
                iniciaConexao(b);
                break;    
            case 3:
                ProcessoUsuario c = new ProcessoUsuario("Dilma",5555);
                c.SendOlah("MULTICAST",5555);
                iniciaConexao(c);
                break;
        }
        
        /*Debug: Digitar manualmente os dados do usuário
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
        */
    }
    
    //Inicia as buscas por arquivos
    private static void iniciaConexao(ProcessoUsuario processo){
        Scanner teclado = new Scanner(System.in);
        //Começa a rodar a interação com usuário
        while(true){
            String nomeArq;
            teclado = new Scanner(System.in);
            System.out.println("Digite o nome do arquivo que deseja ou Q para sair:");
            nomeArq = teclado.nextLine();
            if(nomeArq.equals("Q"))
                break;
            else if(processo.listaDeArquivos.contains(nomeArq)){
                System.out.println("VOCÊ JÁ POSSUI ESSE ARQUIVO. TENTE NOVAMENTE");
                continue;
            }
            if(processo.TemMaisUsuarios())
                processo.PedeArquivo(nomeArq);
            else
                System.out.println("VOCÊ ESTÁ SOZINHO NESSE ROLÊ.");
        }
    }
}
