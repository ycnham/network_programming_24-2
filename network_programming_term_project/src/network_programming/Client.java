//package network_programming;
//
//import java.io.*;
//import java.net.*;
//import java.util.Scanner;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class Client {
//    private static final String SERVER_ADDRESS = "localhost"; // 서버 주소 설정
//    private static final int SERVER_PORT = 8080; // 서버 포트 설정
//    private static final Logger logger = Logger.getLogger(Client.class.getName()); // 로거 생성
//
//    public static void main(String[] args) {
//        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // 서버에 연결
//             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 서버로부터 메시지 수신
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // 서버로 메시지 전송
//             Scanner scanner = new Scanner(System.in)) { // 사용자 입력을 받기 위한 스캐너
//
//            System.out.println("Connected to the game server"); // 서버 연결 성공 메시지
//
//            // 서버로 메시지 전송을 위한 스레드
//            new Thread(() -> {
//                while (true) {
//                    String message = scanner.nextLine(); // 사용자로부터 메시지 입력
//                    out.println(message); // 서버로 메시지 전송
//                }
//            }).start();
//
//            // 서버로부터 메시지를 수신하는 부분
//            String response;
//            while ((response = in.readLine()) != null) {
//                System.out.println("Server: " + response); // 서버로부터 수신된 메시지 출력
//            }
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "An error occurred while communicating with the server", e); // 예외를 로깅
//        }
//    }
//}
