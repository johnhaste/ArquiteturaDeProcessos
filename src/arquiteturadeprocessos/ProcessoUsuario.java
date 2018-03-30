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
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
public class ProcessoUsuario implements Serializable{
    //Informações do Usuário
    private String nome_usuario;
    private Integer porta_usuario;
    private String enderecoIP;
    private PrivateKey chave_privada;
    private PublicKey chave_publica;
    //Chave dos outros (Porta dos Usuários, Chave Pública)
    private HashMap<Integer, PublicKey> listaDeChavesUsuarios;
    //Lista de arquivos que possui
    private ArrayList<String> listaDeArquivos;
    //Reputação dos outros
    private HashMap<String, String> listaDeReputacao;
    //MulticastPeer
    public MulticastPeer conexao_multicast;
    //Servidor Unicast
    public UDPServer conexao_unicast_server;
    
    public ProcessoUsuario(String nome_usuario, int porta_usuario) {
        try {
            this.nome_usuario = nome_usuario;
            this.porta_usuario = new Integer(porta_usuario);
            this.enderecoIP = "192.168.1.3"; //COLOCAR O IP DA REDE ONDE O APP RODARÁ
            this.listaDeChavesUsuarios = new HashMap();
            CriaParDeChaves();
            
            listaDeChavesUsuarios = new HashMap<>();
            listaDeChavesUsuarios.put(this.porta_usuario,this.chave_publica);
            conexao_multicast = new MulticastPeer(this);
            conexao_unicast_server = new UDPServer(this);
            
        } catch (IOException ex) {
            Logger.getLogger(ProcessoUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ProcessoUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void ImprimeUsuario(){
        System.out.println("Nome: " + this.nome_usuario +" Porta: " + this.porta_usuario);
        System.out.println("Chave Publica: " + this.chave_publica.toString());
        System.out.println("Chave Privada: " + this.chave_privada.toString());
    }
    
    public void SendOlah(String metodo, int porta){
        byte[] chavePub = this.chave_publica.getEncoded();
        String mensagem = "="+ this.porta_usuario.toString() + ";" + Base64.getEncoder().encodeToString(chavePub)+";";
        if(metodo.equals("MULTICAST")){
            conexao_multicast.enviaMensagem(mensagem.getBytes());
        }else if(metodo.equals("UNICAST")){
            UDPClient conexao_unicast = new UDPClient(mensagem.getBytes(), this.enderecoIP, porta);
        }
    }
    
    //O USUÁRIO QUE RECEBE UM NOVO 'Olah' DO MULTICAST, ADICIONA O USUÁRIO QUE ENVIOU A MENSAGEM SE NÃO FOR ELE MESMO
    public void AdicionaUsuarioNaLista(String mensagemParaTratar) throws NoSuchAlgorithmException, InvalidKeySpecException{
        HashMap<Integer, PublicKey> hashTemp = new HashMap<>();
        String[] userTemp = mensagemParaTratar.split(Pattern.quote(";"));
        //DECODIFICAÇÃO DA CHAVE PUBLICA
        byte[] encodedKey = Base64.getDecoder().decode(userTemp[1]);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey p = keyFactory.generatePublic(publicKeySpec);
        Integer portaAux = Integer.parseInt(userTemp[0].replace("=", ""));
        hashTemp.put(portaAux, p);
        System.out.println("RECEBEU: " + hashTemp.toString());
        
        if(!this.listaDeChavesUsuarios.containsKey(portaAux)){
            this.listaDeChavesUsuarios.put(portaAux,p);
            System.out.println("ADICIONADO O USUÁRIO DA PORTA:" + portaAux + "CONTENDO A CHAVE PUB: " + p.toString());
            SendOlah("UNICAST", portaAux);
        }else if(portaAux.equals(this.porta_usuario)){
            System.out.println("NÃO É POSSÍVEL ADICIONAR A SI MESMO.");
        }else{
            System.out.println("USUÁRIO JÁ EXISTENTE NA SUA LISTA LHE ENVIOU UM OLAH");
        }
    }    
    
    public String getNome_usuario() {
        return nome_usuario;
    }

    public int getPorta_usuario() {
        return porta_usuario;
    }
    
    public void ExibeUsuariosDaRede(){
        
        this.listaDeChavesUsuarios.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
    }    
}
