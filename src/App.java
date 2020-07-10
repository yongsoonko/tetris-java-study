import java.util.*;
import jcurses.util.*;
import jcurses.event.*;
import jcurses.system.*;
import jcurses.widgets.*;

public class App {
  static final int H = 20;
  static final int W = 10;
  static final int BLOCK_SIZE = 4;
  static int delay = 300;
  static int board[][] = new int[H + BLOCK_SIZE * 2][W + BLOCK_SIZE];
  static CharColor defaultColor = new CharColor(CharColor.WHITE, CharColor.BLACK);
  static Rectangle pos;
  static int direction, shape;
  static java.util.List<Integer> sequence;
  // 좌측 모서리에 붙어있게끔 지정
  static int block[][][][] = new int[][][][] {
      // I
      {{{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0},},
          {{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0},},},

      // O
      {{{0, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},},
      // Z
      {{{0, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
          {{0, 1, 0, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0},},
          {{0, 1, 0, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0},},},
      // S
      {{{0, 0, 0, 0}, {0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{1, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{1, 0, 0, 0}, {1, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},},},
      // J
      {{{0, 1, 0, 0}, {0, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0},},
          {{1, 1, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 0},},},
      // L
      {{{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {1, 1, 1, 0}, {1, 0, 0, 0}, {0, 0, 0, 0},},
          {{1, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0},},},
      // T
      {{{0, 0, 0, 0}, {1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 1, 0, 0}, {1, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0},},
          {{0, 0, 0, 0}, {0, 1, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0},},
          {{1, 0, 0, 0}, {1, 1, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0},},}};

  static {
    sequence = new ArrayList<>();
    for (int i = 0; i < block.length; i++)
      sequence.add(i);
  }

  public static void main(String[] args) throws Exception {
    Toolkit.setEncoding("UTF-8");

    // FIXME:
    Arrays.fill(board[H + BLOCK_SIZE - 3], 1);
    board[H + BLOCK_SIZE - 3][4] = 0;
    Arrays.fill(board[H + BLOCK_SIZE - 2], 1);
    board[H + BLOCK_SIZE - 2][4] = 0;
    board[H + BLOCK_SIZE - 2][5] = 0;
    Arrays.fill(board[H + BLOCK_SIZE - 1], 1);
    board[H + BLOCK_SIZE - 1][4] = 0;

    Thread game = new Thread() {
      public void run() {
        try {
          while (true) {
            Collections.shuffle(sequence);
            for (int i : sequence) {
              pos = createBlock(i);
              if (chkDownTouch(pos)) {
                System.out.println("GAME OVER!");
                Runtime.getRuntime().exit(0);
              }
              while (!chkDownTouch(pos)) {
                down(pos);
                sleep(delay);
              }
              chkAndDelLine(bindBlock(pos));
            }
            System.out.println();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    Thread renderer = new Thread() {
      public void run() {
        try {
          while (true) {
            sleep(100);
            render();
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
            else if (keyCode == InputChar.KEY_UP)
              rotate(pos);
            else if (keyCode == InputChar.KEY_DOWN)
              fall(pos);
          }
        }
      }
    };

    game.start();
    renderer.start();
    controller.start();
  }

  static void rotate(Rectangle r) {
    if (chkRightTouch(r))
      return;

    direction = (direction + 1) % 4;
    int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9)
          board[ci][cj] = 0;
        if (nb[i][j] == 1)
          board[ci][cj] = 9;
      }
    }
  }

  static void fall(Rectangle r) {
    
  }

  static boolean chkRotateTouch(Rectangle r) {
    int nd = (direction + 1) % 4;
    int nb[][] = block[shape][nd];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[ci][cj] == 1
            && (ci >= H + BLOCK_SIZE || board[ci][cj] == 1 || cj >= W || board[ci][cj] == 1))
          return false;
      }
    }
    return true;
  }

  static void chkAndDelLine(Rectangle r) {
    boolean flag = false;
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i, j;
      for (j = 0; j < W; j++) {
        if (board[ci][j] == 0)
          break;
      }
      // 한줄 가득 찼음
      if (j == W) {
        flag = true;
        for (j = 0; j < W; j++)
          board[ci][j] = 0;
      }
    }
    // 파괴 후처리
    if (flag)
      for (int j = 0; j < W; j++) {
        int p = r.getY() + r.getHeight() - 1;
        for (int i = p; i >= 0; i--)
          if (board[i][j] == 1) {
            if (p > i) {
              board[p][j] = board[i][j];
              board[i][j] = 0;
            }
            p--;
          }
      }
  }

  static Rectangle createBlock(int num) {
    final int START_X_POS = 4;
    shape = num;
    direction = 0;
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        if (block[shape][direction][i][j] == 1)
          board[i][j + START_X_POS] = 9;

    return new Rectangle(START_X_POS, 0, 4, 4);
  }

  static Rectangle bindBlock(Rectangle r) {
    int min = (int) 1e9, max = (int) -1e9;
    for (int i = 0; i < r.getHeight(); i++)
      for (int j = 0; j < r.getWidth(); j++) {
        int ci = r.getY() + i, cj = r.getX() + j;
        if (board[ci][cj] == 9) {
          board[ci][cj] = 1;

          if (min > ci)
            min = ci;
          if (max < ci)
            max = ci;
        }
      }
    return new Rectangle(0, min, 0, max - min + 1);
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
