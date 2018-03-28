/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Entidade que representa um usuário na rede
*/
public class Usuario implements Serializable{
    private String nome_usuario;
    private int porta_usuario;
    private PrivateKey chave_privada;
    private PublicKey chave_publica;
    //Chave dos outros (Nome de Usuário, Chave Pública)
    private HashMap<String, String> listaDeChavesUsuarios;
    //Lista de arquivos que possui
    private ArrayList<String> listaDeArquivos;
    //Reputação dos outros
    private HashMap<String, String> listaDeReputacao;
    
    public MulticastPeer conexao_multicast;
    
    public Usuario(String nome_usuario, int porta_usuario) {
        try {
            this.nome_usuario = nome_usuario;
            this.porta_usuario = porta_usuario;
            this.listaDeChavesUsuarios = new HashMap();
            CriaParDeChaves();
            conexao_multicast = new MulticastPeer(this);
        } catch (IOException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void CriaParDeChaves(){
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            random.setSeed(System.currentTimeMillis());
            keyGen.initialize(1024, random);
            KeyPair pares = keyGen.generateKeyPair();
            this.chave_privada = pares.getPrivate();
            this.chave_publica = pares.getPublic();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void ImprimeUsuario(){
        System.out.println("Nome: " + this.nome_usuario +" Porta: " + this.porta_usuario);
        System.out.println("Chave Publica: " + this.chave_publica.toString());
        System.out.println("Chave Privada: " + this.chave_privada.toString());
    }
    public void SendOlah(){
  
        //String mensagem = "{ChavePublica:" + this.chave_publica.toString() + ",NomeDeUsuario:"+this.nome_usuario+"}";
        String mensagem = "Eu sou o " + this.nome_usuario;
        
        conexao_multicast.enviaMensagem(mensagem.getBytes());
    
    }
    
    public void AdicionaNovoUsuario(String NomeDeUsuario, String ChavePublica){
        
        
        System.out.println(NomeDeUsuario+","+ChavePublica);
        
        //Chave dos outros (Nome de Usuário, Chave Pública)
        this.listaDeChavesUsuarios.put(NomeDeUsuario, ChavePublica);
    
    }
    
    public void ExibeUsuariosDaRede(){
        
        this.listaDeChavesUsuarios.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
        
    }
    
    
    
}
