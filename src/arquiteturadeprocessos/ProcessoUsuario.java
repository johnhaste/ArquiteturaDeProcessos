/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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
    //Path onde os arquivos estarão disponíveis para compartilhamento
    private File file;
    
    //Inicializa as variáveis do usuário
    public ProcessoUsuario(String nome_usuario, int porta_usuario) {
        try {
            this.nome_usuario = nome_usuario;
            this.porta_usuario = porta_usuario;
            this.enderecoIP = "10.10.33.194"; //COLOCAR O IP DA REDE ONDE O APP RODARÁ
            this.listaDeChavesUsuarios = new HashMap();
            this.CriaParDeChaves();
            this.listaDeArquivos = new ArrayList<>();
            this.listaDeChavesUsuarios = new HashMap<>();
            this.listaDeChavesUsuarios.put(this.porta_usuario,this.chave_publica);
            this.conexao_multicast = new MulticastPeer(this);
            this.conexao_unicast_server = new UDPServer(this);
            this.listaArquivos();
            this.listaUsersArquivo = new ArrayList<Integer>();
            this.pedindoArquivo = false;
            this.listaDeReputacao = new HashMap<>();
            
        } catch (IOException ex) {
            Logger.getLogger(ProcessoUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Cria uma chave pública e uma privada
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
    
    //Debug: Imprime detalhes do usuário
    public void ImprimeUsuario(){
        System.out.println("Nome: " + this.nome_usuario +" Porta: " + this.porta_usuario);
        System.out.println("Chave Publica: " + this.chave_publica.toString());
        System.out.println("Chave Privada: " + this.chave_privada.toString());
    }
    
    //Envia mensagens por MULTICAST ou UNICAST de acordo com o parâmetro
    public void SendOlah(String metodo, int porta){
        byte[] chavePub = this.chave_publica.getEncoded();
        String mensagem = "="+ this.porta_usuario.toString() + ";" + Base64.getEncoder().encodeToString(chavePub)+";";
        if(metodo.equals("MULTICAST")){
            
            this.conexao_unicast_server.escutando = true;
            try {
                Thread.sleep(100);
            }catch (Exception e) {
                e.printStackTrace();
            }
            conexao_multicast.enviaMensagem(mensagem.getBytes());
            try {
                Thread.sleep(3000);
            }catch (Exception e) {
                e.printStackTrace();
            }
            this.conexao_unicast_server.escutando = false;
            
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
            this.listaDeReputacao.put(portaAux, 0);
            if(metodo.equals("MULTICAST"))
                SendOlah("UNICAST", portaAux);
        }else if(portaAux.equals(this.porta_usuario)){
            System.out.println("VOCÊ ENTROU NA REDE COM SUCESSO");
        }else{
            System.out.println("USUÁRIO JÁ EXISTENTE NA SUA LISTA LHE ENVIOU UM OLAH");
        }
    }   
    
    //Solicitação de um arquivo por multicast, escutando por unicast
    public void PedeArquivo(String nomeArq){
        this.arqSolicitado = nomeArq;
        String mensagem = "?"+this.porta_usuario+"?"+nomeArq+"?";
        this.pedindoArquivo = true;
        this.conexao_unicast_server.escutando = true;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conexao_multicast.enviaMensagem(mensagem.getBytes());
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.conexao_unicast_server.escutando = false;
        
        //Se alguém tiver o arquivo solicitado, inicia a o pedido para o escolhido
        if(!this.listaUsersArquivo.isEmpty())
            this.solicitaEnvio();
        else
            System.out.println("A BUSCA NÃO RETORNOU NENHUM RESULTADO");
    }
    
    //Verifica localmente se tem o arquivo solicitado
    void VerificaArq(String arq) {
        String[] aux = arq.split(Pattern.quote("?"));
        int porta = Integer.parseInt(aux[1]);
        if(porta == this.porta_usuario)
            return;
        System.out.println("O USUÁRIO " + porta + " ESTÁ PERGUNTANDO SE VOCÊ TEM O ARQUIVO " + aux[2]);
        for (String s : file.list()) {
            System.out.println(s + " == " + aux[2]);
            if (s.equals(aux[2])) {
                System.out.println("TENHO");
                UDPClient conexao_unicast = new UDPClient(this.enderecoIP, porta);
                System.out.println("!#" + this.porta_usuario + "!Eu tenho o arquivo!");
                conexao_unicast.enviaMensagem(("!#" + this.porta_usuario + "!Eu tenho o arquivo!").getBytes());
                return;
            }
        }
        
    }
    
    //Preenche a lista com usuários que possuem o arquivo
    void RecebeUsuarioComArquivo(String strParaTratar){
        if(this.pedindoArquivo){
            String[] tratado = strParaTratar.split("!");
            if(tratado[2].equals("Eu tenho o arquivo")){
                //Adiciona um usuário com o arquivo desejado.
                int i = Integer.parseInt(tratado[1].replace("#", ""));
                this.listaUsersArquivo.add(i);
            }
        }
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
    
    //Analisa qual usuário tem a melhor reputação para solicitar o arquivo em unicast
    void solicitaEnvio() {
        Integer melhorPorta = null;
        Integer melhorReputacao = -99;
        for (Integer i : this.listaUsersArquivo) {
            System.out.println(listaDeReputacao.get(i));
            if (this.listaDeReputacao.get(i) > melhorReputacao) {
                melhorPorta = i;
            }
        }
        UDPClient conexao_unicast = new UDPClient(this.enderecoIP, melhorPorta);
        this.listaUsersArquivo.clear();
        this.pedindoArquivo = false;
        if (conexao_unicast.enviaMensagem(("$"+this.porta_usuario+"$" + this.arqSolicitado + "$").getBytes())) {
            this.conexao_unicast_server.escutando = true;
            System.out.println("recebendo arquivo...");
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.conexao_unicast_server.escutando = false;
            if(!this.listaDeArquivos.contains(this.arqSolicitado)){
                //TODO: Decrementar a reputação do dono da porta "melhorPorta";
            }
            this.arqSolicitado = null;
        }
    }
    
    //Define qual pasta cada usuário terá para armazenar seus arquivos
    void listaArquivos(){
        if(this.porta_usuario < 4000)
            this.file = new File(".\\SHARE\\");
        else if(this.porta_usuario < 5000)
            this.file = new File(".\\SHARE2\\");
        else
            this.file = new File(".\\SHARE3\\");
        
        String[] arquivos = this.file.list();
        System.out.println("VOCÊ POSSUI OS SEGUINTES ARQUIVOS PARA COMPARTILHAR:");
        for(String s:arquivos){
            System.out.println(s);
            this.listaDeArquivos.add(s);
        }
    }
    
    //Envia o arquivo por unicast
    public void SendArquivo(String arquivo) {
        String[] s = arquivo.split(Pattern.quote("$"));
        int porta = Integer.parseInt(s[1]);
        arquivo = s[2];
        if(this.listaDeArquivos.contains(arquivo)){
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            UDPClient conexao_unicast = new UDPClient(this.enderecoIP, porta);
            System.out.println("SELECIONANDO O ARQUIVO PARA ENVIO EM:" + file.getName()+"\\"+arquivo);
            Path path = Paths.get(file.getName()+"\\"+arquivo);
            try {
                byte[] arqData = Files.readAllBytes(path);
                
                conexao_unicast.enviaMensagem(("@"+this.porta_usuario+"@"+criptografaPriv(new String(arqData))+"@").getBytes());

            } catch (IOException ex) {
                Logger.getLogger(ProcessoUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else{
            return;
        }
    }
    
    //Recebe o arquivo solicitado, criando um localmente em sua pasta adequada
    void RecebeArq(String arqParaTratar){
        String[] tratado = arqParaTratar.split("@");
        int porta = Integer.parseInt(tratado[1]); // usuário que mandou o arquivo corretamente.
        System.out.println(decriptografaPub(tratado[2],this.listaDeChavesUsuarios.get(porta)));
        //TODO: incrementar na lista de reputação o usuário da porta
        //salvar o conteúdo de "decriptografaPub(tratado[2],this.listaDeChavesUsuarios.get(porta))"
        //no arquivo com o nome contido em this.arqSolicitado.
        //Adicionar a lista de arquivos desse usuário o arquivo que foi salvo.

    }
}
