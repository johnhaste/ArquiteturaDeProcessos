/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilherme Pc
 */
public class UDPServer extends Thread {
    
    DatagramSocket aSocket;
    int porta;
    ProcessoUsuario user;
    public UDPServer(ProcessoUsuario user) {
        
        this.user = user;
        this.porta = user.getPorta_usuario();
        this.aSocket = null;
        this.start();
    }
    @Override
    public void run() {
        try {
            aSocket = new DatagramSocket(porta);
            byte[] mensagem;
            //FICA OUVINDO A PORTA PARA POSSÍVEIS CONEXÕES
            while (true) {
                byte[] buffer = new byte[4096];
                DatagramPacket mensagemEntrada = new DatagramPacket(buffer, buffer.length);
                this.aSocket.receive(mensagemEntrada);
                mensagem = mensagemEntrada.getData();
                byte[] bufferSaida = new byte[4096];
                DatagramPacket mensagemResposta = null;
                if (mensagem[0] == '=') {
                    try {
                        user.AdicionaUsuarioNaLista(new String(mensagem),"UNICAST");
                        //SE CONSEGUIR ADICIONAR, ENVIA A RESPOSTA
                        bufferSaida = "Obrigado, lhe adicionei".getBytes();
                        mensagemResposta = new DatagramPacket(
                                bufferSaida,
                                bufferSaida.length,
                                mensagemEntrada.getAddress(),
                                mensagemEntrada.getPort());
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                        Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (mensagem[0] == '!') {
                    user.RecebeArq(new String(mensagem));
                    //SE CONSEGUIR ADICIONAR, ENVIA A RESPOSTA
                    bufferSaida = "Recebi o arquivo".getBytes();
                    mensagemResposta = new DatagramPacket(
                            bufferSaida,
                            bufferSaida.length,
                            mensagemEntrada.getAddress(),
                            mensagemEntrada.getPort());
                }

                aSocket.send(mensagemResposta);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}
