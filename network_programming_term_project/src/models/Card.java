package models;

import javax.swing.ImageIcon;
import java.awt.*;

public class Card {
  private final int number;       // 카드 숫자
  private final boolean isBlack;  // 카드 색상 여부 (짝수: 흑색, 홀수: 백색)

  public Card(int number) {
    this.number = number;
    this.isBlack = (number % 2 == 0); // 짝수면 흑색, 홀수면 백색
  }

  // 카드 숫자 반환
  public int getNumber() {
    return number;
  }

  // 카드 색상 확인 (흑색인지 여부)
  public boolean isBlack() {
    return isBlack;
  }

  // 카드 이미지 반환 (숫자 보이거나 뒷면 이미지)
  public ImageIcon getCardImage(boolean showNumber) {
    String imageName;

    if (showNumber) {
      imageName = "Card-" + number + ".png"; // 숫자가 보이는 카드 이미지
    } else {
      imageName = isBlack ? "Card-Black.png" : "Card-White.png"; // 뒷면 이미지
    }

    String path = "/cards/" + imageName; // 클래스패스 기준 이미지 경로 설정
    java.net.URL imageUrl = getClass().getResource(path); // 이미지 불러오기

    if (imageUrl == null) { // 이미지 경로가 잘못되었을 경우
      System.err.println("이미지를 찾을 수 없습니다: " + path);
      return null; // 에러 발생 시 null 반환
    }

    // 이미지 크기 조절
    ImageIcon originalIcon = new ImageIcon(imageUrl);
    Image resizedImage = originalIcon.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH);
    return new ImageIcon(resizedImage); // 조절된 이미지 반환
  }
}
