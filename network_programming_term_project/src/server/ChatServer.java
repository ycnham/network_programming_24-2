package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    private static ServerSocket serverSocket;
    private static final Vector<UserService> userVec = new Vector<>(); // 연결된 사용자 정보를 저장하는 벡터

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(30000);
            System.out.println("Chat Server Running...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트의 연결 요청을 대기
                UserService newUser = new UserService(clientSocket);
                userVec.add(newUser); // 사용자 정보를 벡터에 추가
                newUser.start(); // 새 스레드 시작
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트와 통신을 담당하는 스레드 클래스
    static class UserService extends Thread {
        private final Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String username;

        public UserService(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 클라이언트로부터 사용자 이름을 읽음
                username = dis.readUTF();
                broadcast(username + " joined the chat!"); // 모든 사용자에게 접속 메시지 전송

                String message;
                while (true) {
                    // 클라이언트로부터 메시지를 읽고 브로드캐스트
                    message = dis.readUTF();
                    broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                try {
                    // 사용자 연결 종료 처리
                    userVec.remove(this);
                    socket.close();
                    broadcast(username + " left the chat."); // 모든 사용자에게 접속 종료 알림
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 모든 사용자에게 메시지 전송
        private void broadcast(String message) {
            for (UserService user : userVec) {
                try {
                    user.dos.writeUTF(message); // 메시지를 각 사용자에게 보냄
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
