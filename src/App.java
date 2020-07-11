import java.util.*;
import jcurses.util.*;
import jcurses.system.*;

public class App {
  static final int H = 20;
  static final int W = 10;
  static final int BLOCK_SIZE = 4;
  static final int NEXT_BLOCK_START_Y_POS = BLOCK_SIZE;
  static final int NEXT_BLOCK_START_X_POS = W + BLOCK_SIZE;
  static final int POINT_START_Y_POS = BLOCK_SIZE * 2 + 1;
  static final int POINT_START_X_POS = (W + BLOCK_SIZE) * 2;
  static final int DELAY = 250;
  static int board[][] = new int[H + BLOCK_SIZE * 2][W + BLOCK_SIZE];
  static Rectangle pos;
  static int direction, shape, point, drop;
  static List<Integer> sequence;
  static Queue<Integer> bag = new LinkedList<>();
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

    // 빈칸으로 초기화
    for (int i = 0; i < H + BLOCK_SIZE; i++)
      Arrays.fill(board[i], -1);
  }

  public static void main(final String[] args) throws Exception {
    Toolkit.init();
    Toolkit.setEncoding("UTF-8");

    final Thread game = new Thread() {
      public void run() {
        try {
          while (true) {
            if (bag.size() <= 2) {
              Collections.shuffle(sequence);
              bag.addAll(sequence);
            }
            pos = createBlock(bag.poll());
            if (chkDownTouch(pos)) {
              System.out.println("GAME OVER!");
              System.exit(0);
            }
            while (!chkDownTouch(pos)) {
              down(pos);
              sleep(DELAY);
            }
            final Rectangle range = bindBlock(pos);
            if (chkAndDelLine(range)) {
              sleep(DELAY);
              putDownBlock(range);
            }
          }
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    final Thread renderer = new Thread() {
      public void run() {
        try {
          while (true) {
            sleep(25);
            render();
          }
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    final Thread controller = new Thread() {
      public void run() {
        while (true) {
          final int keyCode = Toolkit.readCharacter().getCode();

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

  static Rectangle createBlock(final int num) {
    final int START_X_POS = 4;
    shape = num;
    direction = 0;
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        if (block[shape][direction][i][j] == 1)
          board[i][j + START_X_POS] = 9;

    return new Rectangle(START_X_POS, 0, 4, 4);
  }

  static Rectangle bindBlock(final Rectangle r) {
    int min = (int) 1e9, max = (int) -1e9;
    final int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
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

  static boolean chkAndDelLine(final Rectangle r) {
    boolean flag = false;
    int cnt = 0;
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      int j;
      for (j = 0; j < W; j++)
        if (board[ci][j] == -1)
          break;

      // 한줄 가득 찼음
      if (j == W) {
        flag = true;
        cnt++;
        for (j = 0; j < W; j++)
          board[ci][j] = -2;
      }
    }
    if (cnt > 0)
      point += cnt * 10 + (drop >= 10 ? 5 : (drop >= 5 ? 3 : (drop >= 1 ? 1 : 0)));
    drop = 0;
    return flag;
  }

  static void putDownBlock(final Rectangle r) {
    for (int j = 0; j < W; j++) {
      int p = r.getY() + r.getHeight();
      while (p >= BLOCK_SIZE && board[--p][j] != -2);
      int q = p + 1;
      while (p >= BLOCK_SIZE) {
        while (board[--q][j] == -2);
        while (q >= BLOCK_SIZE && board[q][j] != -2) {
          board[p][j] = board[q][j];
          board[q][j] = -1;
          p--;
          q--;
        }
        p = q;
      }
    }
  }

  static boolean chkDownTouch(final Rectangle r) {
    final int nb[][] = block[shape][direction];
    for (int j = 0; j < r.getWidth(); j++) {
      final int cj = r.getX() + j;
      for (int i = r.getHeight() - 1; i >= 0; i--) {
        final int ci = r.getY() + i;
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

  static boolean chkRightTouch(final Rectangle r) {
    final int nb[][] = block[shape][direction];
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      final int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          if (cj + 1 >= W || (0 <= board[ci][cj + 1] && board[ci][cj + 1] < block.length))
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkLeftTouch(final Rectangle r) {
    final int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          if (cj - 1 < 0 || (0 <= board[ci][cj - 1] && board[ci][cj - 1] < block.length))
            return true;
          break;
        }
      }
    }
    return false;
  }

  static boolean chkRotateTouch(final Rectangle r) {
    final int nd = (direction + 1) % 4;
    final int nb[][] = block[shape][nd];
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1 && (ci >= H + BLOCK_SIZE || cj >= W
            || (0 <= board[ci][cj] && board[ci][cj] < block.length)))
          return true;
      }
    }
    return false;
  }

  static void down(final Rectangle r) {
    final int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci + 1][cj] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setY(r.getY() + 1);
  }

  static void right(final Rectangle r) {
    if (chkRightTouch(pos))
      return;
    final int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      final int ci = r.getY() + i;
      for (int j = r.getWidth() - 1; j >= 0; j--) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj + 1] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() + 1);
  }

  static void left(final Rectangle r) {
    if (chkLeftTouch(r))
      return;
    final int nb[][] = block[shape][direction];
    // board 상태 변경
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj - 1] = board[ci][cj];
          board[ci][cj] = -1;
        }
      }
    }
    // 좌표 변경
    r.setX(r.getX() - 1);
  }

  static void rotate(final Rectangle r) {
    if (chkRotateTouch(r))
      return;

    direction = (direction + 1) % 4;
    final int nb[][] = block[shape][direction];
    for (int i = 0; i < r.getHeight(); i++) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (board[ci][cj] == 9)
          board[ci][cj] = -1;
        if (nb[i][j] == 1)
          board[ci][cj] = 9;
      }
    }
  }

  static void fall(final Rectangle r) {
    int d = 0;
    final Rectangle nr = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    while (!chkDownTouch(nr))
      nr.setY(r.getY() + ++d);

    final int nb[][] = block[shape][direction];
    for (int i = r.getHeight() - 1; i >= 0; i--) {
      final int ci = r.getY() + i;
      for (int j = 0; j < r.getWidth(); j++) {
        final int cj = r.getX() + j;
        if (nb[i][j] == 1) {
          board[ci][cj] = -1;
          board[ci + d][cj] = 9;
        }
      }
    }
    drop = nr.getY() - r.getY();
    pos = nr;
  }

  static void render() {
    Toolkit.startPainting();

    // 미노 가방
    int nb[][] = block[bag.peek()][0];
    for (int i = 0; i < BLOCK_SIZE; i++) {
      int ci = NEXT_BLOCK_START_Y_POS + i;
      for (int j = 0; j < BLOCK_SIZE; j++) {
        int cj = NEXT_BLOCK_START_X_POS + j;
        if (nb[i][j] == 1)
          Toolkit.drawRectangle(cj * 2, ci, 2, 1, blockColors[bag.peek()]);
        else
          Toolkit.drawRectangle(cj * 2, ci, 2, 1, whiteColor);
      }
    }

    // 점수
    Toolkit.printString("Point : " + point, POINT_START_X_POS, POINT_START_Y_POS, whiteColor);

    // 보드
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
