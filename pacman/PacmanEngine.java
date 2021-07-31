/*
To do:
animate movement/eating
ghost speeds changing for levels
ghosts blinking when time is almost up
 */

package games.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PacmanEngine extends JPanel implements KeyListener {
    final static int rowCount = 22;
    final static int columnCount = 23;
    final static int INITIAL_GHOST_COUNT = 0;
    final static public int cellLength = 25;
    final static public int headerHeight = 50;
    final static public int frameWidth = columnCount*cellLength;
    final static public int frameHeight = rowCount*cellLength + headerHeight;
    final static char DOT = '.';
    final static char bigPellet = 'o';
    final static char wall = 'w';
    final static char emptyCell = ' ';
    final static char pacman = 'p';
    final static char ghost = 'g';
    final static int pacSpawnX = 11;
    final static int pacSpawnY = 20;
    final static int UP = 1;
    final static int DOWN = 3;
    final static int RIGHT = 2;
    final static int LEFT = 4;
    final static Color backgroundColor = new Color(0,0,0);
    final static Color WALL_COLOR = new Color(10,0,130);
    final static String assetPath = "C:\\Users\\Willi\\IdeaProjects\\Summer\\assets\\";
    final static int WON = 1;
    final static int LOST = -1;
    final static int NOT_OVER =0;
    final static int GHOST_SPEED = 5;
    final static int PACMAN_SPEED = 3;
    final static int PAUSE_TIME_MILLIS = 1000;
    public final static int TICK_PERIOD_MILLIS = 10;

    static class Ghost {
        int x = 0;
        int y = 0;
        int dir = UP;
        char prevSpotValue = DOT;
        long lastDeathTimeMillis = -1;
    }

    ArrayList<Ghost> ghosts = new ArrayList<Ghost>();
    ArrayList<Ghost> deadGhosts = new ArrayList<Ghost>();


    char[][] board;
    int isGameOver = NOT_OVER;
    boolean isGameStarted = true;
    long startTime;

    int pacX;
    int pacY;
    int direction;
    int livesLeft = 3;
    int score = 0;
    long lastPelletEatenTimeMillis;
    int dotsLeft;
    int pelletsLeft = 4;
    Image dotImage;
    Image pacmanUpImage;
    Image pacmanDownImage;
    Image pacmanLeftImage;
    Image pacmanRightImage;
    Image ghostImage;
    Image edibleGhostImage;
    int tickCount = 0;
    boolean ghostRandom = false;
    boolean replaceGhostDeath = false;

    public boolean tick(){
        if(tickCount*TICK_PERIOD_MILLIS < PAUSE_TIME_MILLIS){
            tickCount++;
            return false;
        }
        if(pelletsLeft == 0 && dotsLeft == 0){
            isGameOver = WON;
            repaint();
            System.out.println("Win, line 94");
            return true;
        }
        if(livesLeft <= 0){
            isGameOver = LOST;
            System.out.println("Lost, line 99");
            return true;
        }
        repaint();
        if (isGameStarted) {
            if (tickCount % GHOST_SPEED == 0) {
                isGameOver = checkCollision();
                calcGhostDirection();
                ghostMove();
                isGameOver = checkCollision();
            }
            if (tickCount % PACMAN_SPEED == 0) {
                isGameOver = checkCollision();
                pacMove(direction);
                isGameOver = checkCollision();
            }
        }

        reviveGhost();
        tickCount++;
        return false;
    }

    public PacmanEngine(){
        for(int i = 0; i < INITIAL_GHOST_COUNT;i++){
            ghosts.add(new Ghost());
        }

        addKeyListener(this);
        setSize(new Dimension(frameWidth, frameHeight));
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        board = new char[rowCount][columnCount];
        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < columnCount; c++){
                board[r][c] = '0';
            }
        }
        pacX = pacSpawnX;
        pacY = pacSpawnY;
        direction = RIGHT;
        createBoard();
        printBoard(board);
        dotImage = Toolkit.getDefaultToolkit().getImage(assetPath+"dot.png");
        pacmanUpImage = Toolkit.getDefaultToolkit().getImage(assetPath+"pacmanup.jpg");
        pacmanDownImage = Toolkit.getDefaultToolkit().getImage(assetPath+"pacmandown.jpg");
        pacmanLeftImage = Toolkit.getDefaultToolkit().getImage(assetPath+"pacmanleft.jpg");
        pacmanRightImage = Toolkit.getDefaultToolkit().getImage(assetPath+"pacmanright.jpg");
        ghostImage = Toolkit.getDefaultToolkit().getImage(assetPath + "ghost.jpg");
        edibleGhostImage = Toolkit.getDefaultToolkit().getImage(assetPath + "edibleghost.png");
    }

    private boolean isPelletOn(){
        return System.currentTimeMillis() - lastPelletEatenTimeMillis < 7000;
    }

    private Image getGhostImage(){
        return isPelletOn() ? edibleGhostImage : ghostImage;
    }

    @Override
    public void paint(Graphics g){
        g.setColor(Color.white);
        g.fillRect(0,0,frameWidth,frameHeight);
        g.setColor(Color.black);
        drawHeader(g);
        drawInitialMap(g);
        drawMap(g);
    }

    private int checkCollision(){
        int ghostsScoreCount = 0;
        for(int i = 0; i < ghosts.size(); i++){
            Ghost g = ghosts.get(i);
            if(g.x == pacX && g.y == pacY){
                if(isPelletOn()){
                    //System.out.println("Pac eats Ghost");
                    replaceGhostDeath = true;
                    g.lastDeathTimeMillis = System.currentTimeMillis();
                    deadGhosts.add(g);
                    ghosts.remove(i);
                    i--;
                    ghostsScoreCount++;
                    score += 200*ghostsScoreCount;
                }
                else {
                    livesLeft--;
                    //System.out.println("COLLISION");
                    if (livesLeft < 1) {
                        return LOST;
                    }
                    reset();
                }
            }
            if(livesLeft < 1){
                return LOST;
            }
        }
        if(livesLeft < 1){
            return LOST;
        }
        return NOT_OVER;
    }

    private void reviveGhost() {
        for (int i = 0; i < deadGhosts.size(); i++) {
            Ghost g = deadGhosts.get(i);
            if (g.lastDeathTimeMillis != -1 && (System.currentTimeMillis() - g.lastDeathTimeMillis) > 5000) {
                g.x = rowCount/2 + i;
                g.y = columnCount/2;
                board[g.y][g.x] = ghost;
                g.lastDeathTimeMillis = -1;
                deadGhosts.remove(i);
                ghosts.add(g);
                i--;
                //System.out.println("GHOSTS RESPAWNf sfgdfgsfdgsfdgsfgfsdgsfdgsdfgsfdgsfdg");
            }
        }
    }

    private void reset(){
        board[pacY][pacX] = emptyCell;
        pacX = pacSpawnX;
        pacY = pacSpawnY;
        direction = 2; //going right
        board[pacY][pacX] = pacman;
        for(int i = 0; i < ghosts.size(); i++){
            Ghost g = ghosts.get(i);
            board[g.y][g.x] = g.prevSpotValue;
            g.x = rowCount/2 + i;
            g.y = columnCount/2;
            board[g.y][g.x] = ghost;
        }
        tickCount = 0;
        repaint();
    }

    private void calcGhostDirection(){
        for(Ghost g : ghosts){
            if(Math.abs(g.x - pacX) > Math.abs(g.y - pacY)){
                if(g.x - pacX > 0){
                    g.dir = isPelletOn() ? RIGHT : LEFT;
                    //System.out.println("Ghost go left");
                }
                if(g.x - pacX < 0){
                    g.dir = isPelletOn() ? LEFT : RIGHT;
                    //System.out.println("Ghost go right");
                }
            }
            else if(Math.abs(g.x - pacX) < Math.abs(g.y - pacY)){
                if(g.y - pacY < 0){
                    g.dir = isPelletOn() ? UP : DOWN;
                    //System.out.println("Ghost go down");
                }
                if(g.y - pacY > 0){
                    g.dir = isPelletOn() ? DOWN : UP;
                    //System.out.println("Ghost go up");
                }
            }
            else{
                int x = (int) Math.floor(Math.random() * 4 + 1);
                g.dir = x;
                //System.out.println("Ghost Random");

            }
        }

    }

    private void ghostMove(int ghostIndex, int deltaX, int deltaY) {
        //throw dice and return if no dice

        Ghost g = ghosts.get(ghostIndex);
        board[g.y][g.x] = g.prevSpotValue;

        int newLocation = board[g.y + deltaY][g.x + deltaX];
        if (newLocation != wall && newLocation != ghost) {
            g.x += deltaX;
            g.y += deltaY;
        }
        if(newLocation == wall || newLocation == ghost){
            int x = (int) Math.floor(Math.random() * 4 + 1);
            g.dir = x;
            ghostRandom = true;
        }
        if(board[g.y][g.x] != pacman) {
            g.prevSpotValue = board[g.y][g.x];
        }
        board[g.y][g.x] = ghost;

    }

    private void ghostMove(){
        for (int i = 0; i < ghosts.size(); i++) {
            Ghost g = ghosts.get(i);
            if (g.dir == UP) {
                ghostMove(i, 0, -1);
            }
            if (g.dir == RIGHT) {
                ghostMove(i, 1, 0);
            }
            if (g.dir == DOWN) {
                ghostMove(i, 0, 1);
             }
            if (g.dir == LEFT) {
                ghostMove(i, -1, 0);
            }
            if(ghostRandom){
                ghostRandom = false;
                i--;
            }
        }
        repaint();
    }

    private void drawInitialMap (Graphics g){
        /*
        g.setColor(WALL_COLOR);
        g.drawLine(margin,headerHeight + margin,frameWidth-margin,headerHeight+margin);
        g.drawLine(margin,frameHeight - margin,frameWidth-margin,frameHeight - margin);
        g.drawLine(margin,headerHeight + margin,margin,frameHeight - margin);
        g.drawLine(frameWidth-margin,headerHeight + margin,frameWidth-margin,frameHeight - margin);
        */

        //draw wall
        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < columnCount; c++){
                if(board[r][c] == wall){
                    g.setColor(WALL_COLOR);
                    g.fillRect(cToCoordinate(c) - cellLength/2 ,rToCoordinate(r) - cellLength/2,cellLength,cellLength);
                }
            }
        }
    }

    private void drawHeader(Graphics g){
        g.setColor(backgroundColor);
        g.fillRect(0, 0, frameWidth, frameHeight);
        g.setColor(Color.white);
        g.drawLine(0, headerHeight, frameWidth, headerHeight);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.drawString("Pacman", frameWidth / 2 - 41, 33);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 14));
        g.drawString("Lives Left: " + livesLeft, frameWidth / 2 + 62, 31);
        g.drawString("Score: " + score, frameWidth / 2 - 130, 31);
        if(isGameOver == WON){
            g.setColor(backgroundColor);
            g.fillRect(0,0,frameWidth,headerHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            g.drawString("You win!", frameWidth / 2 - 50, 33);
            g.setColor(Color.white);
        }
        else if(isGameOver == LOST){
            g.setColor(backgroundColor);
            g.fillRect(0,0,frameWidth,headerHeight);
            g.setColor(Color.RED);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            g.drawString("You Lost.", frameWidth / 2 - 47 , 40);
            g.setColor(Color.white);
        }


    }

    private void drawMap(Graphics g){
        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < columnCount; c++) {
                if (board[r][c] == DOT) {
                    g.drawImage(dotImage, cToCoordinate(c), rToCoordinate(r), cellLength/10,cellLength/10,this);
                }
                if(board[r][c] == pacman){
                    g.drawImage(getPacmanImage(), cToCoordinate(c) - cellLength/2, rToCoordinate(r) - cellLength/2, cellLength,cellLength,this);
                }
                if(board[r][c] == ghost){
                    //implement blinking here
                    g.drawImage(getGhostImage(),cToCoordinate(c) - cellLength/2, rToCoordinate(r) - cellLength/2, cellLength,cellLength,this);
                }
                if(board[r][c] == bigPellet){
                    g.drawImage(dotImage, cToCoordinate(c) - cellLength/4, rToCoordinate(r) - cellLength/4, cellLength/2,cellLength/2,this);
                }
                if(board[r][c] == wall) {
                    g.setColor(WALL_COLOR);
                    g.fillRect(cToCoordinate(c) - cellLength / 2, rToCoordinate(r) - cellLength / 2, cellLength, cellLength);
                }
                //also think about walls
            }
        }
    }

    private void drawAnimation(Graphics g){

    }

    private Image getPacmanImage(){
        if(direction == UP){
            return pacmanUpImage;
        }
        if(direction == DOWN){
            return pacmanDownImage;
        }
        if(direction == LEFT){
            return pacmanLeftImage;
        }
        if(direction == RIGHT){
            return pacmanRightImage;
        }
        return pacmanRightImage;
    }

    private int rToCoordinate(int r){
        r *= cellLength;
        r += headerHeight + cellLength/2;
        return r;
    }

    private int cToCoordinate(int c){
        c *= cellLength;
        c += cellLength/2;
        return c;
    }

    public void printBoard(char[][] board){
        System.out.println("Your current board:");
        for(int i = 0; i < rowCount; i++){
            for(int j = 0; j < columnCount; j++){
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void createBoard(){
        board[pacY][pacX] = pacman;
        for(int i = 0; i < columnCount; i++){
            board[0][i] = wall;
        }
        for(int i = 0; i < columnCount; i++){
            board[rowCount-1][i] = wall;
        }
        for(int i = 0; i < rowCount; i++){
            board[i][0] = wall;
        }
        for(int i = 0; i < rowCount; i++){
            board[i][columnCount-1] = wall;
        }

        for(int i = 0; i < ghosts.size(); i++){
            Ghost g = ghosts.get(i);
            g.x = rowCount/2 + i;
            g.y = columnCount/2;
            board[g.y][g.x] = ghost;
        }

        //draw custom map
        //middle top line
        createRectangle(columnCount/2,1,1,3);
        //top left side rectangles
        createRectangle(2,2,3,2);
        createRectangle(6,2,4,2);
        createRectangle(columnCount - 5,2,3,2);
        createRectangle(columnCount - 10,2,4,2);
        //horz lines underneath
        createRectangle(2,5,3,1);
        createRectangle(columnCount/2 - 3,5,7,1);
        createRectangle(columnCount - 5,5,3,1);
        //vertical lines underneath
        createRectangle(6,5,1,5);
        createRectangle(columnCount/2,6,1,2);
        createRectangle(columnCount-7,5,1,5);
        //hor lines attached
        createRectangle(7,7,3,1);
        createRectangle(columnCount-10,7,3,1);
        //side rectangles
        createRectangle(1,7,4,7);
        createRectangle(columnCount-5,7,4,7);
        //vert lines
        createRectangle(6,11,1,3);
        createRectangle(columnCount-7,11,1,3);
        //create 2nd  cross
        createRectangle(columnCount/2 - 3,13,7,1);
        createRectangle(columnCount/2,14,1,2);
        //horizontal lines
        createRectangle(2,15,2,1);
        createRectangle(5,15,5,1);
        createRectangle(columnCount-10,15,5,1);
        createRectangle(columnCount-4,15,2,1);
        //vert lines attached
        createRectangle(3,16,1,2);
        createRectangle(columnCount-4,16,1,2);
        //small horizontal lines protruding from sides
        createRectangle(1,17,1,1);
        createRectangle(columnCount-2,17,1,1);
        //last cross
        createRectangle(columnCount/2 - 3,17,7,1);
        createRectangle(columnCount/2,18,1,2);
        //upside down crosses
        createRectangle(5,17,1,2);
        createRectangle(columnCount-6,17,1,2);
        createRectangle(2,19,8,1);
        createRectangle(columnCount - 10,19,8,1);

        board[2][1] = bigPellet;
        board[2][columnCount-2] = bigPellet;
        board[rowCount-6][2] = bigPellet;
        board[rowCount-6][columnCount-2] = bigPellet;

        for(int i = 0; i < rowCount; i++){
            for(int j = 0; j < columnCount; j++){
                if(board[i][j] == '0'){
                    board[i][j] = DOT;
                    dotsLeft++;
                }
            }
        }



    }

    private void createRectangle (int x, int y, int length, int height){
        for(int i = x; i < x + length; i++){
            for(int j = y; j < y + height; j++){
                board[j][i] = wall;
            }
        }
    }

    public void pacMove(int direction){
        if(direction == UP){
            board[pacY][pacX]  = emptyCell;
            if(pacY - 1 >= 0 && board[pacY-1][pacX] != 'w'){
                pacY--;
                //System.out.println("Pac go up");
            }
            if(board[pacY][pacX] == bigPellet){
                lastPelletEatenTimeMillis = System.currentTimeMillis();
                pelletsLeft--;
                score += 50;
                //somehow put a timer
            }
            if(board[pacY][pacX] == DOT){
                dotsLeft--;
                score += 10;
            }
            board[pacY][pacX]  = pacman;
        }
        if(direction == RIGHT){
            board[pacY][pacX]  = emptyCell;
            if(pacX + 1 < frameWidth && board[pacY][pacX+1] != 'w') {
                pacX++;
                //System.out.println("Pac go right");
            }
            if(board[pacY][pacX] == bigPellet){
                lastPelletEatenTimeMillis = System.currentTimeMillis();
                pelletsLeft--;
                score += 50;
                //somehow put a timer
            }
            if(board[pacY][pacX] == DOT){
                dotsLeft--;
                score += 10;
            }
            board[pacY][pacX]  = pacman;
        }
        if(direction == DOWN){
            board[pacY][pacX]  = emptyCell;
            if(pacY + 1 < frameWidth && board[pacY+1][pacX] != 'w') {
                pacY++;
                //System.out.println("Pac go down");
            }
            if(board[pacY][pacX] == bigPellet){
                lastPelletEatenTimeMillis = System.currentTimeMillis();
                pelletsLeft--;
                score += 50;
                //somehow put a timer
            }
            if(board[pacY][pacX] == DOT){
                dotsLeft--;
                score += 10;
            }
            board[pacY][pacX]  = pacman;
        }
        if(direction == LEFT){
            board[pacY][pacX]  = emptyCell;
            if(pacX - 1 > -1 && board[pacY][pacX-1] != 'w') {
                pacX--;
                //System.out.println("Pac go left");
            }
            if(board[pacY][pacX] == bigPellet){
                lastPelletEatenTimeMillis = System.currentTimeMillis();
                pelletsLeft--;
                //System.out.println("ate pellet");
                score += 50;
                //somehow put a timer
            }
            if(board[pacY][pacX] == DOT){
                dotsLeft--;
                score += 10;

            }
            board[pacY][pacX]  = pacman;
        }
        //System.out.println("Dots left: " + dotsLeft + " Pellets left: " + pelletsLeft);
        //printBoard(board);
        repaint();
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isGameStarted) {
            startTime = System.currentTimeMillis();
        }
        isGameStarted = true;
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_LEFT){
            direction = LEFT;
            //System.out.print("left ");
        }
        if(key == KeyEvent.VK_RIGHT){
            direction = RIGHT;
            //System.out.print("right ");
        }
        if(key == KeyEvent.VK_UP){
            direction = UP;
            //System.out.print("up ");
        }
        if(key == KeyEvent.VK_DOWN){
            direction = DOWN;
            //System.out.print("down ");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
