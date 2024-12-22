package server;

import models.Card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {
    private static ServerSocket serverSocket;
    private static final Vector<UserService> userVec = new Vector<>(); // 연결된 사용자 목록
    private static final ConcurrentMap<String, Boolean> playerReadyMap = new ConcurrentHashMap<>(); // 플레이어 준비 상태
    private static final Object lock = new Object(); // 동기화를 위한 락
    private static int roundNumber = 1;
    private static int currentPlayerIndex = 0; // 현재 턴의 플레이어 인덱스
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
        synchronized (userVec) {
            if (playerReadyMap.size() == 2 && playerReadyMap.values().stream().allMatch(Boolean::booleanValue)) {
                broadcast("GAME_START");
                System.out.println("[INFO] 모든 플레이어 준비 완료. 게임을 시작합니다.");
                startGame(); // 게임 시작 호출 추가
            } else {
                System.out.println("[INFO] 준비되지 않은 플레이어가 있습니다.");
            }
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

    // 특정 사용자에게만 메시지 전송
    private static void sendToUser(UserService targetUser, String message) {
        synchronized (userVec) {
            try {
                targetUser.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to send message to " + targetUser.getUsername());
            }
        }
    }

    private static void switchTurn() {
        synchronized (lock) {
            if (userVec.size() < 2) {
                System.err.println("[WARN] 턴을 전환할 수 없습니다. 충분한 플레이어가 없습니다.");
                return;
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % userVec.size(); // 턴 전환
            UserService currentPlayer = userVec.get(currentPlayerIndex);

            try {
                // 현재 플레이어에게 턴 시작 메시지 전송
                currentPlayer.sendMessage("TURN_START:" + currentPlayer.getUsername());

                // 다른 플레이어에게 대기 메시지 전송
                for (UserService user : userVec) {
                    if (user != currentPlayer) {
                        user.sendMessage("WAIT_FOR_OPPONENT");
                    }
                }

                System.out.println("[INFO] 현재 턴: " + currentPlayer.getUsername());
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to send turn-related messages: " + e.getMessage());
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
                    userVec.add(this);

                    // 기존 사용자에게 새 사용자의 이름 알림
                    for (UserService user : userVec) {
                        if (user != this) {
                            user.sendMessage("OPPONENT_NAME:" + username);
                        }
                    }

                    // 새 사용자에게 기존 사용자의 이름 알림
                    if (userVec.size() > 1) {
                        for (UserService user : userVec) {
                            if (user != this) {
                                sendMessage("OPPONENT_NAME:" + user.username);
                                break;
                            }
                        }
                    }

                    // 사용자 번호 및 상태 브로드캐스트
                    int playerNumber = userVec.indexOf(this) + 1;
                    sendMessage("PLAYER_NUMBER:" + playerNumber);
                    broadcast(username + " has joined as Player " + playerNumber);
                }

                String message;
                while ((message = dis.readUTF()) != null) {
                    System.out.println("[DEBUG] 수신된 메시지: " + message); // 메시지 로그 출력
                    if (message.equals("TURN_END")) {
                        // 턴 전환하기
                        System.out.println("[DEBUG] TURN_END 메시지 수신. 턴 전환 중...");
                        switchTurn();
                    } else if (message.startsWith("CARD_SELECTED:")) {
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
                    // 현재 플레이어가 아닌 사용자가 카드 선택을 시도한 경우
                    if (userVec.indexOf(this) != currentPlayerIndex) {
                        sendMessage("NOT_YOUR_TURN");
                        return;
                    }

                    // 현재 플레이어의 카드 선택 처리
                    if (userVec.indexOf(this) == 0) { // Player 1
                        if (player1Card != null) { // 이미 선택된 경우 방지
                            sendMessage("ALREADY_SELECTED");
                            return;
                        }
                        player1Card = new Card(cardNumber);
                        broadcast("PLAYER1_CARD_SELECTED:" + cardNumber); // 카드 번호 전송
                        broadcastToOpponent(this, "PLAYER2_CARD_VIEW:" + (player1Card.isBlack() ? "BLACK" : "WHITE")); // 상대방에게 뒷면 표시
                        sendMessage("CARD_SELECTED_SUCCESS");
                    } else { // Player 2
                        if (player2Card != null) { // 이미 선택된 경우 방지
                            sendMessage("ALREADY_SELECTED");
                            return;
                        }
                        player2Card = new Card(cardNumber);
                        broadcast("PLAYER2_CARD_SELECTED:" + cardNumber); // 카드 번호 전송
                        broadcastToOpponent(this, "PLAYER1_CARD_VIEW:" + (player2Card.isBlack() ? "BLACK" : "WHITE")); // 상대방에게 뒷면 표시
                        sendMessage("CARD_SELECTED_SUCCESS");
                    }

                    // 양 플레이어 모두 카드를 선택한 경우 결과 처리
                    if (player1Card != null && player2Card != null) {
                        determineRoundResult();
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid card number received: " + message);
                try {
                    sendMessage("INVALID_CARD_NUMBER");
                } catch (IOException ioException) {
                    System.err.println("Failed to send INVALID_CARD_NUMBER message: " + ioException.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Failed to send error message: " + e.getMessage());
            }
        }

        private void broadcastToOpponent(UserService currentPlayer, String message) {
            synchronized (userVec) {
                for (UserService user : userVec) {
                    // 현재 플레이어가 아닌 사용자에게만 메시지 전송
                    if (user != currentPlayer) {
                        try {
                            user.sendMessage(message);
                        } catch (IOException e) {
                            System.err.println("[ERROR] Failed to send message to opponent: " + e.getMessage());
                        }
                    }
                }
            }
        }

        private void determineRoundResult() {
            String resultMessage;
            if (player1Card.getNumber() > player2Card.getNumber()) {
                player1Score++;
                resultMessage = "ROUND_RESULT:Player1_Wins";
                currentPlayerIndex = 0; // Player 1이 다음 턴 시작
            } else if (player1Card.getNumber() < player2Card.getNumber()) {
                player2Score++;
                resultMessage = "ROUND_RESULT:Player2_Wins";
                currentPlayerIndex = 1; // Player 2가 다음 턴 시작
            } else {
                resultMessage = "ROUND_RESULT:Draw";
            }

            // 결과 및 점수 브로드캐스트
            broadcast(resultMessage + ":" + player1Score + ":" + player2Score);

            // 게임 종료 조건 확인
            if (roundNumber >= MAX_ROUNDS) {
                broadcast("GAME_OVER:" + player1Score + ":" + player2Score);
                resetGame();
            } else {
                roundNumber++;
                switchTurn(); // 다음 턴으로 전환
            }
        }

        private void resetGame() {
            player1Score = 0;
            player2Score = 0;
            roundNumber = 1;
            currentPlayerIndex = 0; // 턴 초기화
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

    private static void startGame() {
        synchronized (userVec) {
            currentPlayerIndex = new Random().nextInt(userVec.size()); // 첫 번째 플레이어 랜덤 설정
            UserService currentPlayer = userVec.get(currentPlayerIndex);

            broadcast("GAME_START");

            try {
                // 첫 번째 플레이어에게 턴 시작 메시지
                currentPlayer.sendMessage("YOUR_TURN");

                // 다른 플레이어에게 대기 메시지
                for (UserService user : userVec) {
                    if (user != currentPlayer) {
                        user.sendMessage("WAIT_FOR_OPPONENT");
                    }
                }

                System.out.println("[INFO] 게임 시작. 첫 번째 턴: Player" + (currentPlayerIndex + 1));
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to send turn-related messages: " + e.getMessage());
            }
        }
    }

}
