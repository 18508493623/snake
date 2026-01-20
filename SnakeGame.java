import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.*;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 25;
    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int ALL_TILES = (WIDTH * HEIGHT) / (TILE_SIZE * TILE_SIZE);

    private final int[] x = new int[ALL_TILES];
    private final int[] y = new int[ALL_TILES];

    private int bodyParts = 3;
    private int applesEaten;
    private int appleX;
    private int appleY;

    private char direction = 'R';
    private boolean running = false;
    private boolean inStartScreen = true;
    private Timer timer;
    private Random random;

    private List<Integer> scores = new ArrayList<>();
    private final String SCORE_FILE = "scores.txt";

    private JButton restartButton;
    private JButton startButton;
    private JButton returnButton;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        this.setLayout(null);

        loadScores();

        startButton = new JButton("Start Game");
        startButton.setBounds(250, 450, 100, 40);
        startButton.addActionListener(e -> {
            inStartScreen = false;
            startButton.setVisible(false);
            startGame();
        });
        this.add(startButton);

        restartButton = new JButton("Restart");
        restartButton.setBounds(250, 400, 100, 40);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        this.add(restartButton);

        returnButton = new JButton("Return");
        returnButton.setBounds(250, 450, 100, 40);
        returnButton.setVisible(false);
        returnButton.addActionListener(e -> returnToMenu());
        this.add(returnButton);
    }

    public void startGame() {
        bodyParts = 3;
        applesEaten = 0;
        direction = 'R';
        for(int i = 0; i < bodyParts; i++) {
            x[i] = 50 - i * TILE_SIZE;
            y[i] = 50;
        }
        newApple();
        running = true;
        if (timer != null) timer.stop();
        timer = new Timer(75, this);
        timer.start();
        restartButton.setVisible(false);
        returnButton.setVisible(false);
    }

    public void restartGame() {
        restartButton.setVisible(false);
        returnButton.setVisible(false);
        startGame();
    }

    public void returnToMenu() {
        inStartScreen = true;
        running = false;
        restartButton.setVisible(false);
        returnButton.setVisible(false);
        startButton.setVisible(true);
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inStartScreen) {
            drawStartScreen(g);
        } else {
            draw(g);
        }
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.green);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Snake Game", (WIDTH - metrics.stringWidth("Snake Game")) / 2, 100);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        g.drawString("Leaderboard:", 200, 200);
        
        g.setFont(new Font("Ink Free", Font.PLAIN, 20));
        int yPos = 240;
        for (int i = 0; i < Math.min(scores.size(), 5); i++) {
            g.drawString((i + 1) + ". " + scores.get(i), 250, yPos);
            yPos += 30;
        }
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, TILE_SIZE, TILE_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
                }
            }
            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (WIDTH / TILE_SIZE)) * TILE_SIZE;
        appleY = random.nextInt((int) (HEIGHT / TILE_SIZE)) * TILE_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] = (y[0] - TILE_SIZE + HEIGHT) % HEIGHT; break;
            case 'D': y[0] = (y[0] + TILE_SIZE) % HEIGHT; break;
            case 'L': x[0] = (x[0] - TILE_SIZE + WIDTH) % WIDTH; break;
            case 'R': x[0] = (x[0] + TILE_SIZE) % WIDTH; break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }
        
        if (!running) {
            timer.stop();
            saveScore(applesEaten);
            loadScores();
            restartButton.setVisible(true);
            returnButton.setVisible(true);
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (WIDTH - metrics1.stringWidth("Game Over")) / 2, HEIGHT / 2);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
    }

    private void saveScore(int score) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(SCORE_FILE, true)))) {
            out.println(score);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScores() {
        scores.clear();
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    scores.add(Integer.parseInt(line.trim()));
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(scores, Collections.reverseOrder());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (direction != 'U') direction = 'D';
                    break;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.setTitle("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
