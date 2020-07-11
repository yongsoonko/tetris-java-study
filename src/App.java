import java.util.*;
import jcurses.util.*;
import jcurses.system.*;

public class App {
  static final int H = 20;
  static final int W = 10;
  static final int BLOCK_SIZE = 4;
  static int delay = 250;
  static int board[][] = new int[H + BLOCK_SIZE * 2][W + BLOCK_SIZE];
  static Rectangle pos;
  static int direction, shape;
  static java.util.List<Integer> sequence;
  static CharColor whiteColor = new CharColor(CharColor.WHITE, CharColor.NORMAL);
  static CharColor blackColor = new CharColor(CharColor.BLACK, CharColor.NORMAL);
  static CharColor blockColors[] = {new CharColor(CharColor.BLUE, CharColor.NORMAL),
      new CharColor(CharColor.BOLD, CharColor.NORMAL),
      new CharColor(CharColor.CYAN, CharColor.NORMAL),
      new CharColor(CharColor.GREEN, CharColor.NORMAL),
      new CharColor(CharColor.MAGENTA, CharColor.NORMAL),
      new CharColor(CharColor.RED, CharColor.NORMAL),
      new CharColor(CharColor.YELLOW, CharColor.NORMAL),};
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
    Toolkit.init();
    Toolkit.setEncoding("UTF-8");

    // 빈칸으로 초기화
    for (int i = 0; i < H + BLOCK_SIZE; i++)
      Arrays.fill(board[i], -1);

    Thread game = new Thread() {
      public void run() {
        try {
          while (true) {
            Collections.shuffle(sequence);
            for (int i : sequence) {
              pos = createBlock(i);
              if (chkDownTouch(pos)) {
                System.out.println("GAME OVER!");
                System.exit(0);
              }
              while (!chkDownTouch(pos)) {
                down(pos);
                sleep(delay);
              }
              Rectangle range = bindBlock(pos);
              if (chkAndDelLine(range)) {
                sleep(delay);
                putDownBlock(range);
              }
            }
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
            sleep(25);
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
    int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj] = shape;

          if (min > ci)
            min = ci;
          if (max < ci)
            max = ci;
        }
      }
    }
    return new Rectangle(0, min, 0, max - min + 1);
  }

  static boolean chkAndDelLine(Rectangle r) {
    boolean flag = false;
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i, j;
      for (j = 0; j < W; j++)
        if (board[ci][j] == -1)
          break;

      // 한줄 가득 찼음
      if (j == W) {
        flag = true;
        for (j = 0; j < W; j++)
          board[ci][j] = -2;
      }
    }
    return flag;
  }

  static void putDownBlock(Rectangle r) {
    for (int j = 0; j < W; j++) {
      int p = r.getY() + r.getHeight();
      while (p >= BLOCK_SIZE && board[--p][j] != -2);
      int q = p + 1;
      while (p >= BLOCK_SIZE) {
        while (board[--q][j] == -2);
        int diff = 0;
        while (q - diff >= BLOCK_SIZE && board[q - diff][j] != -2) {
          board[p][j] = board[q - diff][j];
          board[q - diff][j] = -1;
          p--;
          diff++;
        }
        q -= diff;
        p = q;
      }
    }
  }

  static boolean chkDownTouch(Rectangle r) {
    int nb[][] = block[shape][direction];
    for (int j = 0; j < r.getWidth(); j++) {
      int cj = r.getX() + j;
      for (int i = r.getHeight() - 1; i >= 0; i--) {
        int ci = r.getY() + i;
        if (nb[i][j] == 1) {
          if (ci + 1 >= H + BLOCK_SIZE
              || (0 <= board[ci + 1][cj] && board[ci + 1][cj] < block.length))
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkRightTouch(Rectangle r) {
    int nb[][] = block[shape][direction];
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          if (cj + 1 >= W || (0 <= board[ci][cj + 1] && board[ci][cj + 1] < block.length))
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkLeftTouch(Rectangle r) {
    int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          if (cj - 1 < 0 || (0 <= board[ci][cj - 1] && board[ci][cj - 1] < block.length))
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkRotateTouch(Rectangle r) {
    int nd = (direction + 1) % 4;
    int nb[][] = block[shape][nd];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1 && (ci >= H + BLOCK_SIZE || cj >= W
            || (0 <= board[ci][cj] && board[ci][cj] < block.length)))
          return true;
      }
    }
    return false;
  }

  static void down(Rectangle r) {
    int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci + 1][cj] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setY(r.getY() + 1);
  }

  static void right(Rectangle r) {
    if (chkRightTouch(pos))
      return;
    int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj + 1] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() + 1);
  }

  static void left(Rectangle r) {
    if (chkLeftTouch(r))
      return;
    int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj - 1] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() - 1);
  }

  static void rotate(Rectangle r) {
    if (chkRotateTouch(r))
      return;

    direction = (direction + 1) % 4;
    int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (board[ci][cj] == 9)
          board[ci][cj] = -1;
        if (nb[i][j] == 1)
          board[ci][cj] = 9;
      }
    }
  }

  static void fall(Rectangle r) {
    int d = 0;
    Rectangle nr = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    while (!chkDownTouch(nr))
      nr.setY(r.getY() + ++d);

    int nb[][] = block[shape][direction];
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj] = -1;
          board[ci + d][cj] = 9;
        }
      }
    }
    pos = nr;
  }

  static void render() {
    Toolkit.startPainting();
    for (int i = BLOCK_SIZE; i < H + BLOCK_SIZE; i++) {
      for (int j = 0; j < W; j++) {
        if (0 <= board[i][j] && board[i][j] < block.length)
          Toolkit.drawRectangle(j * 2, i, 2, 1, blockColors[board[i][j]]);
        else if (board[i][j] == 9)
          Toolkit.drawRectangle(j * 2, i, 2, 1, blockColors[shape]);
        else if (board[i][j] == -1)
          Toolkit.drawRectangle(j * 2, i, 2, 1, whiteColor);
        else if (board[i][j] == -2)
          Toolkit.drawRectangle(j * 2, i, 2, 1, blackColor);
      }
    }
    Toolkit.endPainting();
  }
}
