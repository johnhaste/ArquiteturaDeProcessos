/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
    public ArrayList<String> listaDeArquivos;
    //Reputação dos outros
    private HashMap<Integer, Integer> listaDeReputacao;
    //MulticastPeer
    public MulticastPeer conexao_multicast;
    //Servidor Unicast
    public UDPServer conexao_unicast_server;
    //Flag que identifica o pedido de um arquivo
    public boolean pedindoArquivo;
    //Lista de usuários com o arquivo solicitado
    private ArrayList<Integer> listaUsersArquivo;
    //Ultimo arquivo pedido pelo usuario
    private String arqSolicitado;
    
    public ProcessoUsuario(String nome_usuario, int porta_usuario) {
        try {
            this.nome_usuario = nome_usuario;
            this.porta_usuario = porta_usuario;
            this.enderecoIP = "192.168.1.4"; //COLOCAR O IP DA REDE ONDE O APP RODARÁ
            this.listaDeChavesUsuarios = new HashMap();
            this.CriaParDeChaves();
            this.listaDeArquivos = new ArrayList<>();
            this.listaDeChavesUsuarios = new HashMap<>();
            this.listaDeChavesUsuarios.put(this.porta_usuario,this.chave_publica);
            this.conexao_multicast = new MulticastPeer(this);
            this.conexao_unicast_server = new UDPServer(this);
            this.listaArquivos();
            this.pedindoArquivo = false;
            
        } catch (IOException ex) {
            Logger.getLogger(ProcessoUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void CriaParDeChaves(){
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            random.setSeed(System.currentTimeMillis());
            keyGen.initialize(1024);
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
            this.conexao_unicast_server.start();
            for(int i = 0 ; i< 1000000 ; i++){}
            conexao_multicast.enviaMensagem(mensagem.getBytes());
        }else if(metodo.equals("UNICAST")){
            UDPClient conexao_unicast = new UDPClient(this.enderecoIP, porta);
            conexao_unicast.enviaMensagem(mensagem.getBytes());
        }
    }
    
    //O USUÁRIO QUE RECEBE UM NOVO 'Olah' DO MULTICAST, ADICIONA O USUÁRIO QUE ENVIOU A MENSAGEM SE NÃO FOR ELE MESMO
    public void AdicionaUsuarioNaLista(String mensagemParaTratar, String metodo) throws NoSuchAlgorithmException, InvalidKeySpecException{

        String[] userTemp = mensagemParaTratar.split(Pattern.quote(";"));
        //DECODIFICAÇÃO DA CHAVE PUBLICA
        byte[] encodedKey = Base64.getDecoder().decode(userTemp[1]);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey p = keyFactory.generatePublic(publicKeySpec);
        Integer portaAux = Integer.parseInt(userTemp[0].replace("=", ""));
        if(!this.listaDeChavesUsuarios.containsKey(portaAux)){
            System.out.println("ADICIONADO O USUÁRIO DA PORTA: " + portaAux + " COM A CHAVE PUB: " + p.toString());
            this.listaDeChavesUsuarios.put(portaAux,p);
            //this.listaDeReputacao.put(portaAux, 0);
            if(metodo.equals("MULTICAST"))
                SendOlah("UNICAST", portaAux);
        }else if(portaAux.equals(this.porta_usuario)){
            System.out.println("VOCÊ ENTROU NA REDE COM SUCESSO");
        }else{
            System.out.println("USUÁRIO JÁ EXISTENTE NA SUA LISTA LHE ENVIOU UM OLAH");
        }
    }        
    public void PedeArquivo(String nomeArq){
        this.arqSolicitado = nomeArq;
        String mensagem = "?"+this.porta_usuario+"?"+nomeArq;
        this.pedindoArquivo = true;
        this.conexao_unicast_server.interrupt();
        this.conexao_unicast_server.start();
        for(int i = 0 ; i< 1000000 ; i++){}
        conexao_multicast.enviaMensagem(mensagem.getBytes());
    }
    
    void VerificaArq(String arq) {
        String[] aux = arq.split(Pattern.quote("?"));
        int porta = Integer.parseInt(aux[1]);
        if(porta == this.porta_usuario)
            return;
        System.out.println("O USUÁRIO "+porta+" ESTÁ PERGUNTANDO SE VOCÊ TEM O ARQUIVO "+aux[2]);
        if(this.listaDeArquivos.contains(arq)){
            UDPClient conexao_unicast = new UDPClient(this.enderecoIP, porta);
            conexao_unicast.enviaMensagem(("!#"+this.porta_usuario+"!Eu tenho o arquivo!").getBytes());
        }
    }
    void RecebeUsuarioComArquivo(String strParaTratar){
        if(this.pedindoArquivo){
            String[] tratado = strParaTratar.split("!");
            if(tratado[2].equals("Eu tenho o arquivo")){
                //Adiciona um usuário com o arquivo desejado.
                this.listaUsersArquivo.add(Integer.parseInt(tratado[1].replace("#", "")));
            }
        }
    }
    void RecebeArq(String arqParaTratar){
        String[] tratado = arqParaTratar.split("!");
        System.out.println(tratado[1]);
        String decriptado = decriptografaPub(tratado[1],this.listaDeChavesUsuarios.get(Integer.parseInt(tratado[0])));
        System.out.println(decriptado);
    }
    //MÉTODOS USADOS PARA CRIPTOGRAFAR E DECRIPTOGRAFAR MENSAGENS
    
    public String criptografaPriv(String texto) {
      try {
        final Cipher cipher;
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, this.chave_privada);
        byte[] cipherText = cipherText = cipher.doFinal(texto.getBytes());
        return new String(Base64.getEncoder().encodeToString(cipherText));
      } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
        e.printStackTrace();
      }
      return null;
    }

    public String decriptografaPub(String texto, PublicKey pubKey) {
      try {
        final Cipher cipher;
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        byte[] decryptedText = cipher.doFinal(Base64.getDecoder().decode(texto));
        return new String(decryptedText);
      } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
        ex.printStackTrace();
      }
      return null;
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
    
    public boolean TemMaisUsuarios(){
        return this.listaDeChavesUsuarios.size() > 1;
    }
    void solicitaEnvio() {
        if(this.listaUsersArquivo.isEmpty()){
            System.out.println("A busca não retornou nenhum usuário com o arquivo");
            this.pedindoArquivo = false;
            return;
        }
        else{
            Integer melhorPorta = null;
            Integer melhorReputacao = -99;
            for(Integer i : this.listaUsersArquivo){
                if(this.listaDeReputacao.get(i) > melhorReputacao){
                    melhorPorta = i;
                }
            }
            if(melhorPorta != null){
                UDPClient conexao_unicast = new UDPClient(this.enderecoIP, melhorPorta);
                this.conexao_unicast_server.start();
                conexao_unicast.enviaMensagem(("$"+this.arqSolicitado+"$").getBytes()); 
            }
            else{
                System.out.println("Não foi encontrado um bom peer para lhe enviar o arquivo");
            }
            
        }
    }
    
    void listaArquivos(){
        File file = new File(".\\SHARE\\");
        String[] arquivos = file.list();
        System.out.println("VOCÊ POSSUI OS SEGUINTES ARQUIVOS PARA COMPARTILHAR:");
        for(String s:arquivos){
            System.out.println(s);
            this.listaDeArquivos.add(s);
        }
    }
    
    String SendArquivo(String arquivo, int porta) {
        if(this.listaDeArquivos.contains(arquivo)){
            //TODO: CRIPTOGRAFAR E ENVIAR O ARQUIVO POR AQUI
            return "Arquivo enviado";
        }
        else{
            return "Não possuo o arquivo solicitado";
        }
    }
}
