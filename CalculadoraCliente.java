package calculadoracliente;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.annotation.processing.Messager;
import javax.swing.JOptionPane;
public class CalculadoraCliente {
    public static void main(String[] args) {
        
        
        try{
            System.out.println("Creando socket cliente");
            Socket clientSocket=new Socket();
            System.out.println("Estableciendo la conexion");

            InetSocketAddress addr=new InetSocketAddress("192.168.0.1",6666);
            clientSocket.connect(addr);

            InputStream is = clientSocket.getInputStream();
            OutputStream os= clientSocket.getOutputStream();

            DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
            
            String mensajeEnviar = "";

            // enviar numeros y sus operaciones

            String acumulador = "";
            int numeroNumeros = Integer.parseInt(JOptionPane.showInputDialog("Ingrese la cantidad de numeros que quiere operar"));
            if (numeroNumeros <= 0){
                System.out.println("No se enviara nada");
            }
            else{
                for (int i = 1; i <= numeroNumeros; i++) {
                    mensajeEnviar = JOptionPane.showInputDialog("Ingrese el numero a enviar--- Esta en el "+i+"º número");
                    acumulador= acumulador+ mensajeEnviar;
                    if(i!=(numeroNumeros)){
                        acumulador = acumulador + ",";
                    }
                }
                os.write(acumulador.getBytes());
                System.out.println("Todos los numeros enviados: "+ mensajeEnviar);
            }

            // operadores
            acumulador = "";
            
            if(numeroNumeros==0){
                System.out.println("No hay ningun numero con el que trabajar, por lo que no se necesitan operandos");
            }else{
                int numeroOperandos = Integer.parseInt(JOptionPane.showInputDialog("Ingrese la cantidad de operadores a usar"));
                if (numeroOperandos <= 0){
                    System.out.println("No se enviara nada");
                }
                else{
                    for (int i = 1; i <= numeroOperandos; i++) {
                        mensajeEnviar = JOptionPane.showInputDialog("Ingrese el operando a enviar--- Esta en el "+i+"º operando");
                        acumulador= acumulador + mensajeEnviar;
                        if(i!=(numeroNumeros-1)){
                            acumulador = acumulador + ",";
                        }
                    }
                    os.write(acumulador.getBytes());
                    System.out.println("Todos los operandos enviados: "+ mensajeEnviar);
                }
            }
            
            


            
            
            
            
            int length = 10000;//dis.readInt();
            System.out.println("tamaño: "+ length);
            byte[] messageByte = new byte[length];
            boolean end = false;
            StringBuilder dataString = new StringBuilder(length);
            int totalBytesRead = 0;
            String agregar="";
            //contador para saber cuantos mensages se mandaron
            int contador = 1;
            while(!end) {

                agregar= "";

                int currentBytesRead = dis.read(messageByte);
                System.out.println("data "+contador+" bytes: "+ currentBytesRead);
                totalBytesRead = currentBytesRead + totalBytesRead;
                if (currentBytesRead!=-1){
                    if(totalBytesRead <= length) {
                        agregar =  new String(messageByte, 0, currentBytesRead, StandardCharsets.UTF_8);
                        System.out.println("Mensaje Recibido: " + agregar);
                        if (agregar.equalsIgnoreCase("adios")){
                            System.out.println("Se recibio el mensaje de salida");
                            break;
                        }
                        dataString.append(agregar);
                        System.out.println("dataString 1: "+ dataString);
                    } else {
                        dataString
                        .append(new String(messageByte, 0, length - totalBytesRead + currentBytesRead,StandardCharsets.UTF_8));
                        System.out.println("dataString 2: "+ dataString);
                    }
                    if(dataString.length()>=length) {
                        end = true;
                    }
                }else{
                    System.out.println("Finalizado el mensaje");
                    end= true;
                }
                // aumento el contador
                contador +=1;
            }
            System.out.println("el data String final/ el mensaje completo: "+dataString);
            
            
            

            System.out.println("Mensaje recibido: "+dataString);


            System.out.println("Cerrando el socket cliente");

            clientSocket.close();

            System.out.println("Terminado");

            }catch (IOException e) {
                    e.printStackTrace();
            }
        
        
        
    }
}