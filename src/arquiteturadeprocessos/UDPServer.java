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
    boolean escutando;
    
    //Inicializa variáveis
    public UDPServer(ProcessoUsuario user) {
        
        this.user = user;
        this.porta = user.getPorta_usuario();
        this.aSocket = null;
        this.escutando = false;
        try {
            aSocket = new DatagramSocket(porta);
        } catch (SocketException ex) {
            System.out.println("TESTE1");
        }
        this.start();
    }

    @Override
    public void run() {
        escutaPorta();
    }
    
    //Escuta pela porta do usuário
    public void escutaPorta() {
        byte[] mensagem;
        try {
            while (true) {
                byte[] buffer = new byte[4096];
                DatagramPacket mensagemEntrada = new DatagramPacket(buffer, buffer.length);
                this.aSocket.receive(mensagemEntrada);
                mensagem = mensagemEntrada.getData();
                byte[] bufferSaida = new byte[4096];
                DatagramPacket mensagemResposta = null;

                //Analísa o início da mensagem para saber o que fazer com a mensagem recebida
                
                //Adiciona um usuário novo na lista (SendOlah)
                if (mensagem[0] == '=' && this.escutando) {
                    try {
                        user.AdicionaUsuarioNaLista(new String(mensagem), "UNICAST");
                        //SE CONSEGUIR ADICIONAR, ENVIA A RESPOSTA
                        bufferSaida = "Obrigado, lhe adicionei".getBytes();
                        mensagemResposta = new DatagramPacket(
                                bufferSaida,
                                bufferSaida.length,
                                mensagemEntrada.getAddress(),
                                mensagemEntrada.getPort());
                        //Envia a resposta para o emissor
                        aSocket.send(mensagemResposta);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                        Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                //Confirma o recebimento da confirmação de outro ter o arquivo e imprime uma mensagem para aguardo
                if (mensagem[0] == '!' && this.escutando) {
                    //SE CONSEGUIR ADICIONAR, ENVIA A RESPOSTA
                    bufferSaida = "Vou analisar e lhe solicito".getBytes();

                    mensagemResposta = new DatagramPacket(
                            bufferSaida,
                            bufferSaida.length,
                            mensagemEntrada.getAddress(),
                            mensagemEntrada.getPort());
                    aSocket.send(mensagemResposta);
                    user.RecebeUsuarioComArquivo(new String(mensagem));
                }
                
                //Analisa se alguém pediu o arquivo
                if (mensagem[0] == '$') {
                    String arquivo = new String(mensagem);
                    bufferSaida = "Se prepara que agora é hora de receber meu arquivão!!!".getBytes();
                    mensagemResposta = new DatagramPacket(
                            bufferSaida,
                            bufferSaida.length,
                            mensagemEntrada.getAddress(),
                            mensagemEntrada.getPort());
                    aSocket.send(mensagemResposta);
                    this.user.SendArquivo(arquivo);
                }
                
                //Recebe o arquivo
                if (mensagem[0] == '@' && escutando) {
                    String arquivo = new String(mensagem);
                    bufferSaida = "RECEBI SEU ARQUIVÃO".getBytes();
                    mensagemResposta = new DatagramPacket(
                            bufferSaida,
                            bufferSaida.length,
                            mensagemEntrada.getAddress(),
                            mensagemEntrada.getPort());
                    aSocket.send(mensagemResposta);
                    this.user.RecebeArq(arquivo);
                }
                
            }
            }catch (SocketException ex) {
                System.out.println("SocketException: " + ex);
            }catch (IOException ex) {    
                System.out.println("IOException: " + ex);
        }    
    }
}


