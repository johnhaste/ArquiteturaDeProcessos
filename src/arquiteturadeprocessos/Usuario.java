/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 Entidade que representa um usuário na rede
*/
public class Usuario implements Serializable{
    //Informações do Usuário
    private String nome_usuario;
    private int porta_usuario;
    private PrivateKey chave_privada;
    private PublicKey chave_publica;
    
    //Chave dos outros (Nome de Usuário, Chave Pública)
    private HashMap<String, PublicKey> listaDeChavesUsuarios;
   
    //Lista de arquivos que possui
    private ArrayList<String> listaDeArquivos;
    
    //Reputação dos outros
    private HashMap<String, String> listaDeReputacao;
    
    //MulticastPeer
    public MulticastPeer conexao_multicast;
    
    public Usuario(String nome_usuario, int porta_usuario) {
        try {
            this.nome_usuario = nome_usuario;
            this.porta_usuario = porta_usuario;
            this.listaDeChavesUsuarios = new HashMap();
            CriaParDeChaves();
            
            listaDeChavesUsuarios = new HashMap<String, PublicKey>();
            listaDeChavesUsuarios.put(this.nome_usuario,this.chave_publica);
            listaDeChavesUsuarios.put("JohnJones",this.chave_publica);
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
        
        byte[] chavePub = this.chave_publica.getEncoded();
        String mensagem = "="+this.nome_usuario + ";" + Base64.getEncoder().encodeToString(chavePub);
        System.out.println(Base64.getEncoder().encodeToString(chavePub));
        conexao_multicast.enviaMensagem(mensagem.getBytes());
    }
    
    public void AdicionaUsuarioNaLista(String mensagemParaTratar){
        
        HashMap<String, PublicKey> hashTemp = new HashMap<>();
        String[] userTemp = mensagemParaTratar.split(Pattern.quote(";"));
        
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(userTemp[1].getBytes());
        PublicKey p = null;
        try {
            p = factory.generatePublic(encodedKeySpec);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        PublicKey p = null;
//        try {
//            p = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(userTemp[1].getBytes()));
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InvalidKeySpecException ex) {
//            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
//        }
        hashTemp.put(userTemp[0], p);
        System.out.println("RECEBEU: " + hashTemp.toString());
         
    }
    
    public void ExibeUsuariosDaRede(){
        
        this.listaDeChavesUsuarios.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
        
    }    
}
