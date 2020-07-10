import jcurses.util.*;
import jcurses.event.*;
import jcurses.system.*;
import jcurses.widgets.*;

public class App {
  static final int H = 20;
  static final int W = 10;
  static final int BLOCK_SIZE = 4;
  static int board[][] = new int[H + BLOCK_SIZE * 2][W + BLOCK_SIZE * 2];
  static CharColor defaultColor = new CharColor(CharColor.WHITE, CharColor.BLACK);
  static Rectangle pos;

  static int block[][][] = new int[][][] {{{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0},},
      {{0, 0, 0, 0}, {0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
      {{0, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
      {{0, 0, 0, 0}, {0, 0, 1, 1}, {0, 1, 1, 0}, {0, 0, 0, 0},},
      {{0, 0, 1, 0}, {0, 0, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
      {{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
      {{0, 0, 0, 0}, {0, 0, 0, 0}, {1, 1, 1, 0}, {0, 1, 0, 0},}};

  public static void main(String[] args) throws Exception {

    Toolkit.setEncoding("UTF-8");
    System.out.println(Toolkit.getEncoding());

    Thread game = new Thread() {
      public void run() {
        try {
          for (int i = 6; i >= 0; i--) {
            pos = createBlock(i);
            while (true) {
              sleep(300);
              if (!chkTouch(pos))
                down(pos);
              else {
                bindBlock(pos);
                System.out.println("DROP");
                break;
              }
              print();
              // Toolkit.drawRectangle(pos, new CharColor(CharColor.RED, CharColor.WHITE));
            }
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    Thread controller = new Thread() {
      public void run() {
        while (true) {
          InputChar key = Toolkit.readCharacter();
          int keyCode = key.getCode();

          System.out.println(pos.getX());
          if (pos != null) {
            if (keyCode == InputChar.KEY_LEFT) {
            } else if (keyCode == InputChar.KEY_RIGHT) {
              right(pos);
            }
          }
        }
      }
    };

    game.start();
    controller.start();
  }

  static void bindBlock(Rectangle r) {
    for (int i = 0; i < r.getHeight(); i++)
      for (int j = 0; j < r.getWidth(); j++) {
        int ci = r.getY() + i, cj = r.getX() + j;
        if (board[ci][cj] == 9)
          board[ci][cj] = 1;
      }
  }

  // TODO: 한줄이 가득 찼는지 확인
  static boolean chkLine() {
    return true;
  }

  static Rectangle createBlock(int shape) {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        if (block[shape][i][j] == 1)
          board[i][j + 3] = 9;

    return new Rectangle(3, 0, 4, 4);
  }

  static boolean chkTouch(Rectangle r) {
    final int si = r.getY(), sj = r.getX();
    boolean touched = false;
    for (int j = 0; j < r.getWidth(); j++) {
      int cj = sj + j;
      for (int i = r.getHeight() - 1; i >= 0; i--) {
        int ci = si + i;
        if (board[ci][cj] == 9) {
          if (ci + 1 == H + BLOCK_SIZE || board[ci + 1][cj] == 1)
            touched = true;
          break;
        }
      }
    }
    return touched;
  }

  static void down(Rectangle r) {
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--)
      for (int j = 0; j < r.getWidth(); j++) {
        int ci = r.getY() + i, cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          board[ci + 1][cj] = board[ci][cj];
          board[ci][cj] = 0;
        }
      }
    // 좌표 변경
    r.setY(r.getY() + 1);
  }

  static void right(Rectangle r) {
    // board 상태 변경
    for (int j = r.getWidth() - 1; j >= 0; j--)
      for (int i = 0; i < r.getHeight(); i++) {
        int ci = r.getY() + i, cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          board[ci][cj + 1] = board[ci][cj];
          board[ci][cj] = 0;
        }
      }
    // 좌표 변경
    r.setX(r.getX() + 1);
  }

  static void print() {
    for (int i = BLOCK_SIZE; i < H + BLOCK_SIZE; i++) {
      StringBuffer sb = new StringBuffer();
      for (int j = 0; j < W; j++) {
        if (board[i][j] != 0)
          sb.append("# ");
        else
          sb.append(". ");
      }
      sb.append('\n');
      Toolkit.printString(sb.toString(), 0, i, defaultColor);
    }
  }
}
