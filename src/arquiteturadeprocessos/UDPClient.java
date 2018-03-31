/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arquiteturadeprocessos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilherme Pc
 */

public class UDPClient {

    DatagramSocket aSocket;
    byte[] mensagem;
    String enderecoIP;
    int porta;   
    
    public UDPClient(byte[] mensagem, String enderecoIP, int porta) {
        this.mensagem = mensagem;
        this.porta = porta;
        this.enderecoIP = enderecoIP;
        this.enviaMensagem(); 
    }

    private void enviaMensagem() {
        try {
            aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getByName(enderecoIP);
            DatagramPacket request = new DatagramPacket(mensagem, mensagem.length, aHost, porta);
            aSocket.send(request);
            byte[] buffer = new byte[4096];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            System.out.println("Reply: " + new String(reply.getData()));
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}