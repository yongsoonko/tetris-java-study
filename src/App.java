import jcurses.util.*;
import jcurses.event.*;
import jcurses.system.*;
import jcurses.widgets.*;

public class App {
  static final int H = 20;
  static final int W = 10;
  static final int BLOCK_SIZE = 4;
  static int delay = 100;
  static int board[][] = new int[H + BLOCK_SIZE * 2][W + BLOCK_SIZE];
  static CharColor defaultColor = new CharColor(CharColor.WHITE, CharColor.BLACK);
  static Rectangle pos;

  // I, O, Z, S, J, L, T
  // 좌측 모서리에 붙어있게끔 지정
  static int block[][][] = new int[][][] {{{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0},},
      {{0, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
      {{0, 0, 0, 0}, {0, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 1, 0},},
      {{0, 0, 0, 0}, {0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
      {{0, 1, 0, 0}, {0, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
      {{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
      {{0, 0, 0, 0}, {1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},}};

  public static void main(String[] args) throws Exception {
    Toolkit.setEncoding("UTF-8");

    Thread game = new Thread() {
      public void run() {
        try {
          for (int i = 0; i < block.length; i++) {
            pos = createBlock(i);
            while (true) {
              sleep(delay);
              if (!chkDownTouch(pos))
                down(pos);
              else {
                bindBlock(pos);
                break;
              }
              render();
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
          int keyCode = Toolkit.readCharacter().getCode();

          if (pos != null) {
            if (keyCode == InputChar.KEY_LEFT)
              left(pos);
            else if (keyCode == InputChar.KEY_RIGHT)
              right(pos);
          }
        }
      }
    };

    game.start();
    controller.start();
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

  static void bindBlock(Rectangle r) {
    for (int i = 0; i < r.getHeight(); i++)
      for (int j = 0; j < r.getWidth(); j++) {
        int ci = r.getY() + i, cj = r.getX() + j;
        if (board[ci][cj] == 9)
          board[ci][cj] = 1;
      }
  }

  static boolean chkDownTouch(Rectangle r) {
    for (int j = 0; j < r.getWidth(); j++) {
      int cj = r.getX() + j;
      for (int i = r.getHeight() - 1; i >= 0; i--) {
        int ci = r.getY() + i;
        if (board[ci][cj] == 9) {
          if (ci + 1 >= H + BLOCK_SIZE || board[ci + 1][cj] == 1)
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkRightTouch(Rectangle r) {
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          if (cj + 1 >= W || board[ci][cj + 1] == 1)
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkLeftTouch(Rectangle r) {
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          if (cj - 1 < 0 || board[ci][cj - 1] == 1)
            return true;
          break;
        }
      }
    }
    return false;
  }

  static void down(Rectangle r) {
    // board 상태 변경
    for (int j = 0; j < r.getWidth(); j++) {
      int cj = r.getX() + j;
      for (int i = r.getHeight() - 1; i >= 0; i--) {
        int ci = r.getY() + i;
        if (board[ci][cj] == 9) {
          board[ci + 1][cj] = board[ci][cj];
          board[ci][cj] = 0;
        }
      }
    }
    // 좌표 변경
    r.setY(r.getY() + 1);
  }

  static void right(Rectangle r) {
    if (chkRightTouch(pos))
      return;
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          board[ci][cj + 1] = board[ci][cj];
          board[ci][cj] = 0;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() + 1);
  }

  static void left(Rectangle r) {
    if (chkLeftTouch(r))
      return;
    // board 상태 변경
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          board[ci][cj - 1] = board[ci][cj];
          board[ci][cj] = 0;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() - 1);
  }

  static void render() {
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
