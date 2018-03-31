package arquiteturadeprocessos;

import arquiteturadeprocessos.ProcessoUsuario;

import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MulticastPeer extends Thread {

    private MulticastSocket socket;
    private InetAddress group = null;
    private ProcessoUsuario user;

    MulticastPeer(ProcessoUsuario user) throws IOException {

        this.user = user;                //"228.5.6.7"
        this.group = InetAddress.getByName("192.168.1.1");
        this.socket = new MulticastSocket(6789);
        this.socket.joinGroup(group);
        this.start();
    }

    @Override
    //FICA OUVINDO A PORTA DE MULTICAST PARA RECEBER AS MENSAGENS E DEPOIS TRATAR
    public void run() {
        byte[] mensagem;
        while(true){
            try{
                byte[] buffer = new byte[4096];
                DatagramPacket mensagemEntrada = new DatagramPacket(buffer,buffer.length);
                this.socket.receive(mensagemEntrada);
                mensagem = mensagemEntrada.getData();
                
                //SE NOVO USUÁRIO NA REDE
                if(mensagem[0]=='='){
                    try {
                        this.user.AdicionaUsuarioNaLista(new String(mensagem),"MULTICAST");
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                        Logger.getLogger(MulticastPeer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else if(mensagem[0]=='?'){
                    this.user.VerificaArq(new String(mensagem)); 
                }
            } catch (IOException ex) {
                Logger.getLogger(MulticastPeer.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    public void enviaMensagem(byte[] mensagem) {

        try {
            DatagramPacket mensagemEnviada;

            mensagemEnviada = new DatagramPacket(mensagem, mensagem.length, this.group, 6789);

            this.socket.send(mensagemEnviada);//com o socket já instanciado, envamos o datagrampacket    
            
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}