package calculadoraservidor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
public class CalculadoraServidor {
    
    public static void main(String[] args) {
        try {
            System.out.println("Creando socket servidor");
            ServerSocket serverSocket = new ServerSocket();

            System.out.println("Realizando el bind");
            InetSocketAddress addr = new InetSocketAddress("192.168.0.1", 6666);
            serverSocket.bind(addr);

            System.out.println("Aceptando conexiones");
            
            while (true) {
                
                Socket newSocket = serverSocket.accept();
                System.out.println("Conexion recibida");
                Thread t = new Thread(new CalculoServer(newSocket));
                t.start();
            }
        } catch (IOException e) {
            System.out.println("Excepción IO");
        }
    }

    static class CalculoServer extends Thread {
        private Socket socket;

        public CalculoServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is=socket.getInputStream();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
            OutputStream os=socket.getOutputStream();
            
            
            int length = 10000;//dis.readInt();
            System.out.println("tamaño: "+ length);
            byte[] messageByte = new byte[length];
            boolean end = false;
            StringBuilder dataString = new StringBuilder(length);
            int totalBytesRead = 0;
            String agregar="";
            // variable para luego manejar los numeros fuera del while
            List<Integer> listaNumeros = new ArrayList();
            // variable para luego manejar los operadores fuera del while
            ArrayList listaOperadores = new ArrayList();
            //contador para saber cuantos mensages se mandaron
            int contador = 1;
            while(!end) {
                // Por cada envio el agregar vuelva a estar vacio
                agregar= "";

                int currentBytesRead = dis.read(messageByte);
                System.out.println("data "+contador+" bytes: "+ currentBytesRead);
                if(contador ==1){
                    System.out.println("Se ingresaron los numeros");
                }
                if(contador ==2){
                    System.out.println("Se ingresaron los operadores");
                }
                totalBytesRead = currentBytesRead + totalBytesRead;
                if (currentBytesRead!=-1){
                    ArrayList cortaLista = new ArrayList();
                    if(totalBytesRead <= length) {
                        agregar =  new String(messageByte, 0, currentBytesRead, StandardCharsets.UTF_8);
                        System.out.println("Mensaje Recibido: " + agregar);
                        // Usar un split con ,
                        String[] cortado = agregar.split(",");
                        cortaLista = new ArrayList();
                        int vueltas=0;
                        for (String e: cortado) {
                            cortaLista.add(e);
                            //listaNumeros.add(Integer.parseInt(cortado[vueltas]));
                            vueltas++;
                        }
                        // cortaLista se debe de usar para luego re usarla con el uso anterior

                        if(contador ==1){
                            listaNumeros = new ArrayList();
                            for (int i = 0; i< cortaLista.size(); i++){
                                listaNumeros.add(Integer.parseInt(cortaLista.get(i).toString()));
                            }
                        }
                        if(contador ==2){
                            listaOperadores = new ArrayList(cortaLista);
                        }
                        
                        
                        System.out.println("cortado: "+ cortaLista);
                        dataString.append(agregar);
                        System.out.println("dataString 1: "+ dataString);
                        
                        // para mantener que todo sea una operación y no tener que cerrar la conexion
                        if(contador == 2){
                            end = true;
                            System.out.println("paso por aqui");
                        }
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
            
            // ahora debo de hacer la forma para hacer la operacion, que 
            // sera devuelva despues con un string y simplemente operado con
            // la forma base la maquina que manejara los () y demas operaciones
            Integer numeroReal = 0;
            System.out.println("tamaño lista numeros "+ listaNumeros.size());
            for(int i = 0; i<listaNumeros.size(); i++){
                numeroReal = listaNumeros.get(i);
                System.out.println("numero real"+numeroReal.toString());
            }
            for(int i = 0; i<listaOperadores.size(); i++){
                System.out.println("operador string: "+listaOperadores.get(i));
            }
            
            // 2+3
            // 2+(3+2)
            // (2+1)
            
            // para saber el indice del numero en el que estoy
            int indiceNumero = 0;
            // para saber el indice del operador en el que estoy
            int indiceOperador = 0;
            // la cadena final 
            String cadena = "";
            while(listaNumeros.size()>indiceNumero || listaOperadores.size()>indiceOperador) {
                int numeroActual = 0;
                if(listaNumeros.size()>indiceNumero){
                    numeroActual = listaNumeros.get(indiceNumero); // para obtener el numero actual de la vuelta
                }
                String operadorActual = "";
                if(listaOperadores.size()>indiceOperador){
                    operadorActual = listaOperadores.get(indiceOperador).toString();
                }
                
                
                boolean yaInsertado = false;
                
                // primero debe de ser el if de los operadores por si existe algun parentesis
                
                if(operadorActual.equals("(")){
                    cadena = cadena + operadorActual;
                    yaInsertado = true;
                }
                if(listaOperadores.size()>indiceOperador+1){
                    while(listaOperadores.get(indiceOperador+1).toString().equals("(")){
                        cadena = cadena + listaOperadores.get(indiceOperador+1).toString();
                        indiceOperador++;
                        if (yaInsertado==false) {
                            operadorActual = listaOperadores.get(indiceOperador).toString();
                        }
                    }
                    // del while de arriba es esta palanca
                    // yaInsertado = true;
                    while(listaOperadores.get(indiceOperador+1).toString().equals(")")){
                        cadena = cadena + listaOperadores.get(indiceOperador+1).toString();
                        indiceOperador++;
                        operadorActual = listaOperadores.get(indiceOperador).toString();
                        yaInsertado = true;
                    }
                }
                
                cadena = cadena + numeroActual;
                indiceNumero++;
                
                // para evitar meter un operador al final
                if(indiceNumero!=listaNumeros.size()){
                    if(yaInsertado==false){
                        if(operadorActual.equals("+") ||operadorActual.equals("-") ||operadorActual.equals("*") || operadorActual.equals("/")){
                            cadena = cadena + operadorActual;
                            indiceOperador++;
                            yaInsertado=true;
                        }
                    }
                }
                System.out.println("indice numero final while"+ indiceNumero );
                System.out.println("indice operador final while"+ indiceOperador );
            }
            System.out.println("cadena final: " + cadena);
            
            // usar el stack para descomponer el string cadena y luego enviar los datos
            int resultadoFinal = evaluate(cadena);
            System.out.println("resultado final: "+ resultadoFinal);
            
            
            os.write(String.valueOf(resultadoFinal).getBytes());
            
            
            System.out.println("Cerrando el nuevo socket");

            socket.close();
            
                // Aquí va la lógica de manejo de la conexión
                // Asegúrate de ajustar el código según tus necesidades
            } catch (IOException e) {
                System.out.println("Excepción IO");
            }
        }
        
    }
    public static int evaluate(String expression) {
        Stack<Integer> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int openParentheses = 0; // Variable para contar paréntesis abiertos

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == ' ') {
                continue;
            }

            if (Character.isDigit(ch)) {
                int num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + Character.getNumericValue(expression.charAt(i));
                    i++;
                }
                i--; // preguntar despues por que resta en esta parte
                operands.push(num);
            } else if (ch == '(') {
                operators.push(ch);
                openParentheses++; // Incrementamos el contador de paréntesis abiertos
            } else if (ch == ')') {
                // Verificamos si la pila de operadores está vacía antes de desapilar
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException("Invalid expression: mismatched parentheses");
                }
                while (!operators.isEmpty() && operators.peek() != '(') {
                    applyTopOperator(operands, operators);
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop(); // Pop '('
                } else {
                    // Si la pila está vacía en este punto, significa que hay un paréntesis no emparejado
                    throw new IllegalArgumentException("Invalid expression: mismatched parentheses");
                }
            } else if (isOperator(ch)) {
                while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                    applyTopOperator(operands, operators);
                }
                operators.push(ch);
            }
        }
        if (openParentheses > 0) {
            throw new IllegalArgumentException("Invalid expression: mismatched parentheses Falta de parentesis de cierre por conteo");
        }

        while (!operators.isEmpty()) {
            applyTopOperator(operands, operators);
        }

        if (operands.size() != 1 || !operators.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return operands.pop();
    }

    private static boolean isOperator(char op) {
        return op == '+' || op == '-' || op == '*' || op == '/';
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    private static void applyTopOperator(Stack<Integer> operands, Stack<Character> operators) {
        if (operators.isEmpty()) {
            return; // No hay operadores, simplemente retornamos
        }
        char operator = operators.pop();
        if (operator == '(') {
            return; // Ignoramos el paréntesis de apertura, ya que se manejará de otra manera
        }
        if (operands.size() < 2) {
            throw new IllegalArgumentException("Invalid expression: not enough operands");
        }
        int operand2 = operands.pop();
        int operand1 = operands.pop();
        operands.push(applyOperation(operator, operand1, operand2));
    }

    private static int applyOperation(char op, int a, int b) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }
    
}
