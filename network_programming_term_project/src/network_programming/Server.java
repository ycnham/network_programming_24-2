package network_programming;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final int PORT = 8080; // 서버 포트 번호 설정
    private static final Set<Socket> clients = new HashSet<>(); // 연결된 클라이언트 소켓을 저장하는 집합
    private static final Logger logger = Logger.getLogger(Server.class.getName()); // 로거 생성

    public static void main(String[] args) {
        logger.info("Server is starting..."); // 서버 시작 메시지 로깅
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 서버 소켓 생성
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // 클라이언트의 연결 요청 수락
                    clients.add(clientSocket); // 클라이언트 소켓을 집합에 추가
                    new ClientHandler(clientSocket).start(); // 각 클라이언트에 대해 새로운 핸들러 스레드 시작
                    logger.info("New client connected: " + clientSocket); // 새로운 클라이언트 연결 메시지 로깅
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error accepting client connection", e); // 예외 발생 시 로그 기록
                    continue; // 예외 발생 시 루프 재시작
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while starting the server", e); // 서버 시작 시 예외를 심각 레벨로 로깅
        }
    }

    // 클라이언트 연결을 처리하는 내부 클래스
    static class ClientHandler extends Thread {
        private final Socket socket; // 클라이언트 소켓

        public ClientHandler(Socket socket) {
            this.socket = socket; // 클라이언트 소켓 초기화
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 클라이언트로부터 메시지 입력 스트림
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) { // 클라이언트로 메시지 출력 스트림
                String message;
                while ((message = in.readLine()) != null) { // 클라이언트로부터 메시지를 읽어옴
                    logger.info("Received: " + message); // 수신된 메시지 로깅
                    broadcastMessage(message); // 다른 클라이언트들에게 메시지 전송

                    // 수신 확인 메시지를 클라이언트에 전송
                    out.println("Message received: " + message);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Connection lost: " + socket, e); // 연결 끊김 메시지를 경고 레벨로 로깅
            } finally {
                try {
                    clients.remove(socket); // 연결이 종료된 클라이언트를 집합에서 제거
                    socket.close(); // 소켓 닫기
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error closing client socket", e); // 소켓 종료 시 예외를 심각 레벨로 로깅
                }
            }
        }

        // 모든 클라이언트에게 메시지를 브로드캐스트하는 메서드
        private void broadcastMessage(String message) {
            for (Socket client : clients) {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true); // 각 클라이언트에 메시지 전송
                    out.println(message); // 메시지 전송
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error broadcasting message to client", e); // 예외 발생 시 경고 레벨로 로깅
                }
            }
        }
    }
}
