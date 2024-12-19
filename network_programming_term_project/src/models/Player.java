package models;

import java.util.ArrayList;
import java.util.List;

public class Player {
  private final String name; // 플레이어 이름
  private final List<Card> cards = new ArrayList<>(); // 플레이어의 카드 목록
  private int points = 0; // 점수

  public Player(String name) {
    this.name = name;
    for (int i = 0; i <= 8; i++) { // 0부터 8까지 카드 초기화
      cards.add(new Card(i));
    }
  }

  // 플레이어 이름 반환
  public String getName() {
    return name;
  }

  // 카드 목록 반환
  public List<Card> getCards() {
    return cards;
  }

  // 점수 반환
  public int getPoints() {
    return points;
  }

  // 점수 추가
  public void addPoint() {
    points++;
  }

  // 카드와 점수 초기화
  public void reset() {
    cards.clear();
    for (int i = 0; i <= 8; i++) {
      cards.add(new Card(i));
    }
    points = 0;
  }

  public void setPoints(int points) {
    this.points = points;
  }
}
