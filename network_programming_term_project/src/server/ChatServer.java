package server;

import models.Card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    private static ServerSocket serverSocket;
    private static final Vector<UserService> userVec = new Vector<>(); // 연결된 사용자 목록
    private static final Object lock = new Object(); // 동기화를 위한 락

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(30000);
            System.out.println("Chat Server Running on port 30000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                UserService newUser = new UserService(clientSocket);

                synchronized (userVec) {
                    userVec.add(newUser);
                }

                newUser.start();
                broadcastPlayerCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    static class UserService extends Thread {
        private final Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String username;

        private static Card player1Card, player2Card; // 각 플레이어의 카드
        private static int player1Score = 0, player2Score = 0;

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
                // 클라이언트로부터 사용자 이름을 수신
                username = dis.readUTF();
                System.out.println(username + " connected.");

                synchronized (userVec) {
                    int playerNumber = userVec.indexOf(this) + 1;
                    sendMessage("PLAYER_NUMBER:" + playerNumber); // 클라이언트에게 플레이어 번호 전달
                    broadcast(username + " has joined as Player " + playerNumber); // 다른 사용자들에게 알림
                }

                String message;
                while ((message = dis.readUTF()) != null) {
                    if (message.startsWith("CARD_SELECTED:")) {
                        // 카드 선택 처리
                        handleCardSelection(message);
                    } else if (message.equals("GAME_START")) {
                        // 게임 시작 메시지 브로드캐스트
                        broadcast("GAME_START");
                    } else {
                        // 일반 채팅 메시지 처리
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                cleanup();
            }
        }

        // 카드 선택 처리 메서드
        private void handleCardSelection(String message) {
            try {
                int cardNumber = Integer.parseInt(message.split(":")[1]);
                synchronized (lock) {
                    if (userVec.indexOf(this) == 0) {
                        player1Card = new Card(cardNumber);
                        broadcast("PLAYER1_CARD_SELECTED:" + cardNumber);
                    } else {
                        player2Card = new Card(cardNumber);
                        broadcast("PLAYER2_CARD_SELECTED:" + cardNumber);
                    }

                    // 양쪽 플레이어가 모두 카드를 선택한 경우 결과 처리
                    if (player1Card != null && player2Card != null) {
                        determineRoundResult();
                        player1Card = null;
                        player2Card = null;
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid card number received: " + message);
            }
        }

        // 라운드 결과를 처리하는 메서드
        private void determineRoundResult() {
            String result;
            if (player1Card.getNumber() > player2Card.getNumber()) {
                player1Score++;
                result = "ROUND_RESULT:Player1_Wins";
            } else if (player1Card.getNumber() < player2Card.getNumber()) {
                player2Score++;
                result = "ROUND_RESULT:Player2_Wins";
            } else {
                result = "ROUND_RESULT:Draw";
            }

            // 결과 및 점수 브로드캐스트
            broadcast(result + ":" + player1Score + ":" + player2Score);
        }

        public void sendMessage(String message) throws IOException {
            dos.writeUTF(message);
            dos.flush();
        }

        private void cleanup() {
            synchronized (userVec) {
                userVec.remove(this);
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

        public String getUsername() {
            return username;
        }
    }
}
