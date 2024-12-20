package server;

import models.Card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {
    private static ServerSocket serverSocket;
    private static final Vector<UserService> userVec = new Vector<>(); // 연결된 사용자 목록
    private static final ConcurrentMap<String, Boolean> playerReadyMap = new ConcurrentHashMap<>(); // 플레이어 준비 상태
    private static final Object lock = new Object(); // 동기화를 위한 락
    private static int roundNumber = 1;

    private static final int MAX_ROUNDS = 9;

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

    private static void checkAllReady() {
        System.out.println("Checking all ready. Current state: " + playerReadyMap);
        if (playerReadyMap.size() == 2 && playerReadyMap.values().stream().allMatch(Boolean::booleanValue)) {
            broadcast("GAME_START");
        } else {
            System.out.println("Not all players are ready.");
        }
    }

    private static void broadcast(String message) {
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
                System.out.println("[DEBUG] UserService 생성자 호출됨");
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                System.out.println("[DEBUG] Input/Output 스트림 초기화 완료");
            } catch (IOException e) {
                System.err.println("Failed to initialize streams.");
            }
        }

        @Override
        public void run() {
            System.out.println("[DEBUG] UserService run() 시작됨. 사용자: " + username);
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
                    System.out.println("[DEBUG] 수신된 메시지: " + message); // 메시지 로그 출력
                    if (message.startsWith("CARD_SELECTED:")) {
                        // 카드 선택 처리
                        handleCardSelection(message);
                    } else if (message.equals("READY")) {
                        System.out.println("[DEBUG] READY 메시지 처리 시작");
                        // 준비 완료 처리
                        handleReadyState();
                    } else if (message.equals("GAME_START")) {
                        System.out.println("[DEBUG] GAME_START 메시지 수신됨");
                        // 게임 시작 요청 처리
                        handleGameStart();
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

        private void handleGameStart() {
            synchronized (userVec) {
                // 리더 플레이어 준비 상태 강제 설정
                if (!playerReadyMap.containsKey(username)) {
                    playerReadyMap.put(username, true);
                    System.out.println("[DEBUG] 리더 플레이어 " + username + " 준비 상태 강제 설정");
                }
                if (userVec.size() < 2) {
                    try {
                        sendMessage("WAITING_FOR_PLAYERS"); // 플레이어가 충분하지 않음을 알림
                    } catch (IOException e) {
                        System.err.println("Failed to notify player about insufficient players.");
                    }
                    return;
                }

                if (playerReadyMap.size() == 2 && playerReadyMap.values().stream().allMatch(Boolean::booleanValue)) {
                    broadcast("GAME_START");
                    System.out.println("Game has started!");
                } else {
                    try {
                        sendMessage("NOT_ALL_READY"); // 모두 준비되지 않았음을 알림
                    } catch (IOException e) {
                        System.err.println("Failed to notify player about readiness.");
                    }
                }
            }
        }

        private void handleReadyState() {
            playerReadyMap.put(username, true);
            System.out.println("[DEBUG] handleReadyState 호출됨. " + username + " 준비 완료. playerReadyMap 상태: " + playerReadyMap);;
            broadcast("PLAYER_READY:" + username);
            checkAllReady();
        }

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
            broadcast("ROUND_END:" + roundNumber++);

            if (roundNumber > MAX_ROUNDS) {
                broadcast("GAME_OVER:" + player1Score + ":" + player2Score);
                resetGame();
            } else {
                broadcast("ROUND_END:" + roundNumber++);
            }
        }

        private void resetGame() {
            player1Score = 0;
            player2Score = 0;
            roundNumber = 1;
            broadcast("RESET_GAME");
        }

        public void sendMessage(String message) throws IOException {
            dos.writeUTF(message);
            dos.flush();
        }

        private void cleanup() {
            synchronized (userVec) {
                userVec.remove(this);
            }
            playerReadyMap.remove(username);
            broadcast("PLAYER_DISCONNECTED:" + username);

            // 연결 종료 후 대기 상태 전환
            if (userVec.size() < 2) {
                broadcast("WAITING_FOR_PLAYERS");
            }
            broadcastPlayerCount();
        }

        public String getUsername() {
            return username;
        }
    }
}
