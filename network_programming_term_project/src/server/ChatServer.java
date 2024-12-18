package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * ChatServer 클래스: 서버 소켓을 생성하고 클라이언트와의 연결 및 통신을 관리합니다.
 */
public class ChatServer {
    private static ServerSocket serverSocket; // 서버 소켓
    private static final Vector<UserService> userVec = new Vector<>(); // 연결된 사용자 목록
    private static int leaderAssigned = 0; // 리더가 지정되었는지 여부 (0: 미지정, 1: 리더 있음)

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(30000); // 포트 30000에서 서버 시작
            System.out.println("Chat Server Running on port 30000...");

            while (true) {
                // 클라이언트 연결 요청 수락
                Socket clientSocket = serverSocket.accept();
                UserService newUser = new UserService(clientSocket);

                synchronized (userVec) {
                    userVec.add(newUser); // 사용자 목록에 추가
                }

                newUser.start(); // 사용자와의 통신 스레드 시작
                broadcastPlayerCount(); // 현재 플레이어 수를 클라이언트에게 전송
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 모든 사용자에게 현재 접속한 플레이어 수를 브로드캐스트합니다.
     */
    private static void broadcastPlayerCount() {
        synchronized (userVec) {
            String message = "PLAYER_COUNT:" + userVec.size();
            for (UserService user : userVec) {
                try {
                    user.sendMessage(message);
                } catch (IOException e) {
                    System.err.println("Failed to send player count to " + user.getUsername());
                }
            }
        }
    }

    /**
     * 클라이언트와 통신을 처리하는 스레드 클래스
     */
    static class UserService extends Thread {
        private final Socket socket; // 클라이언트 소켓
        private DataInputStream dis; // 입력 스트림
        private DataOutputStream dos; // 출력 스트림
        private String username; // 사용자 이름

        public UserService(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Failed to initialize streams.");
            }
        }

        @Override
        public void run() {
            try {
                username = dis.readUTF();
                System.out.println(username + " connected.");

                synchronized (userVec) {
                    if (leaderAssigned == 0) {
                        sendMessage("PLAYER_NUMBER:1"); // 리더 지정
                        leaderAssigned = 1;
                    } else {
                        sendMessage("PLAYER_NUMBER:2");
                    }
                }

                broadcast(username + " joined the chat.");
                broadcastPlayerCount();

                String message;
                while ((message = dis.readUTF()) != null) {
                    if (message.equals("GAME_START")) {
                        broadcast("GAME_START"); // 게임 시작 메시지 브로드캐스트
                        System.out.println("GAME_START message sent to all clients.");
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                cleanup();
            }
        }


        public void sendMessage(String message) throws IOException {
            dos.writeUTF(message);
            dos.flush();
        }

        public String getUsername() {
            return username;
        }

        private void cleanup() {
            synchronized (userVec) {
                userVec.remove(this);

                // 리더가 나간 경우 새로운 리더 지정
                if (leaderAssigned == 1 && userVec.size() > 0 && this == userVec.firstElement()) {
                    try {
                        UserService newLeader = userVec.firstElement();
                        newLeader.sendMessage("PLAYER_NUMBER:1"); // 새 리더 지정
                        leaderAssigned = 1; // 리더 상태 유지
                        System.out.println(newLeader.getUsername() + " is reassigned as leader.");
                    } catch (IOException e) {
                        System.err.println("Failed to reassign leader.");
                    }
                }

                if (userVec.isEmpty()) {
                    leaderAssigned = 0; // 모든 사용자가 나간 경우 리더 초기화
                }
            }
            broadcast(username + " left the chat.");
            broadcastPlayerCount(); // 접속자 수 갱신
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {
            synchronized (userVec) {
                for (UserService user : userVec) {
                    try {
                        user.sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Failed to send message to " + user.getUsername());
                    }
                }
            }
        }
    }
}
