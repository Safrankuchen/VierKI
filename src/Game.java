import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.ColorModel;
import java.beans.Expression;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    static public Canvas canvas = new Canvas();
    static BufferStrategy bufferStrategy;
    static Graphics graphics;
    static final String title = "Vier Gwinnt";
    static int frameWidth = 600;
    static int frameHeight = frameWidth / 1*1;
    static JFrame frame = new JFrame(title);
    static final int frameRate = 60;
    static char[][] grid = new char[6][7];
    static char player = 'B';
    static char AI = 'R';
    static boolean hasGameEnded = false;
    static int layerCount = 4;

    public static void main(String[] args) {

        //initialize array
        for (int row = 0; row < grid.length; row++){
            for (int col = 0; col < grid[0].length; col++){
                grid[row][col] = ' ';
            }
        }
        //Creating the frame.

        frame.setSize(frameWidth, frameHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);


        //Creating the canvas.


        canvas.setSize(frameWidth, frameHeight);
        canvas.setBackground(Color.BLACK);
        canvas.setVisible(true);
        canvas.setFocusable(false);


        //Putting it all together.
        frame.add(canvas);

        canvas.createBufferStrategy(3);

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                //System.out.println(e.getX());
                if(hasGameEnded)
                    resetgame();
                else {
                    int colw = frameWidth / (grid.length + 1);
                    makePlay(e.getX() / colw, grid);
                    System.out.println(getHeuristik(grid));
                    if (isWinner(otherPlayer(player),grid))
                        hasGameEnded = true;
                    System.out.println("////////////////////////////////////////////////");
                    if(!hasGameEnded)
                        makePlay(getBestPlay(), grid);
                    if (isWinner(otherPlayer(player),grid))
                        hasGameEnded = true;
                }


            }
        });

        final java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                timer();
            }
        }, 0, 1000/frameRate);
        //playGame();
        if (player == AI){
            makePlay(getBestPlay(), grid);
        }

        
    }
    public static char otherPlayer(char p){
        if (p == 'R') {
            return 'B';
        } else {
            return 'R';
        }
    }
    public static int getBestPlay(){
        int bestPlay = 0;
        int bestScore = -100000000;
        for (int i = 0; i < grid[0].length; i++) {
            if (grid[0][i] == ' ') {
                makePlay(i,grid);
                int playScore = getBestScore(layerCount, true);
                revertPlay(i);
                if (playScore > bestScore) {
                    bestScore = playScore;
                    bestPlay = i;
                }
                System.out.println("KI :" + i + "  " + playScore);
            }
        }
        System.out.println(bestPlay + "  " + bestScore);
        return bestPlay;
    }
    public static int getBestScore(int layer, boolean min){
        int bestScore = -100000000;
        if(layer == 0) {
            //display(grid);
            return getHeuristik(grid);
        }
        else {
            if (isWinner(player,grid) || isWinner(otherPlayer(player),grid))
                return getHeuristik(grid);
            if (!min) {
                for (int i = 0; i < grid[0].length; i++) {

                    if (grid[0][i] == ' ') {
                        makePlay(i, grid);
                        int score = getBestScore(layer - 1,true);
                        revertPlay(i);
                        if (score > bestScore)
                            bestScore = score;
                    }


                    //System.out.println("KI :" + i + "  " + playScore);
                }
            }else {
                bestScore = 100000000;
                for (int i = 0; i < grid[0].length; i++) {

                    if (grid[0][i] == ' ') {
                        makePlay(i, grid);
                        int score = getBestScore(layer - 1,false);
                        revertPlay(i);
                        if (score < bestScore)
                            bestScore = score;
                    }

                }
            }
        }
            //System.out.println(layer + "  " + bestScore);
        return bestScore;

    }
    public static void timer(){
        bufferStrategy = canvas.getBufferStrategy();
        graphics = bufferStrategy.getDrawGraphics();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        frameHeight = canvas.getHeight();
        frameWidth = canvas.getWidth();

        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(0,0,frameWidth,frameHeight);
        int colw = frameWidth/grid[0].length;
        int roww = (frameHeight-30)/grid.length;

        for (int row = 0; row < grid.length; row++){
            for (int col = 0; col < grid[0].length; col++){
                graphics.setColor(Color.GREEN);
                graphics.fillRect(colw*col,30+roww*row,colw-5,roww-5);
                if(grid[row][col] == 'R'){
                    graphics.setColor(Color.RED);
                    graphics.fillOval(colw*col+5,30+roww*row+5,colw-15,roww-15);
                }else if(grid[row][col] == 'B'){
                    graphics.setColor(Color.BLUE);
                    graphics.fillOval(colw*col+5,30+roww*row+5,colw-15,roww-15);
                }
            }
        }
        int redscore = getPlayerScore('R',grid);
        int bluescore = getPlayerScore('B',grid);
        int scoreSum = redscore + bluescore;

        int redWidth;
        int blueWidth;
        if (scoreSum != 0)
            redWidth = (int) (frameWidth *  (double) redscore / scoreSum);
        else
            redWidth = (int) (frameWidth * 0.5);
        blueWidth = frameWidth - redWidth;
        graphics.setColor(Color.RED);
        graphics.fillRect(0,0, redWidth,30);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(redWidth,0, blueWidth,30);
        graphics.setColor(Color.black);
        graphics.setFont(new Font("Comic Sans MS", Font.ITALIC + Font.BOLD, 20));
        graphics.drawString(String.valueOf(redscore),10, 20);
        graphics.drawString(String.valueOf(bluescore),frameWidth - graphics.getFontMetrics().stringWidth(String.valueOf(bluescore)) -10, 20);

        bufferStrategy.show();
        graphics.dispose();
    }
    public static void resetgame(){
        grid = new char[6][7];

        for (int row = 0; row < grid.length; row++){
            for (int col = 0; col < grid[0].length; col++){
                grid[row][col] = ' ';
            }
        }
        hasGameEnded = false;
        player = 'R';
    }
    public static void makePlay(int play, char[][] gr){
        if(validate(play,gr)) {

            for (int row = grid.length - 1; row >= 0; row--) {
                if (gr[row][play] == ' ') {
                    gr[row][play] = player;
                    break;
                }

            }

            switchPlayer();

        }
    }
    public static void revertPlay(int play){

        for (int row = 0; row <= grid.length; row++) {
            if (grid[row][play] != ' ') {
                grid[row][play] = ' ';
                break;
            }

        }
        switchPlayer();

    }
    public static void switchPlayer(){
        if (player == 'R') {
            player = 'B';
        } else {
            player = 'R';
        }
    }
    public static int getPlayScore(int play){

        makePlay(play,grid);

        display(grid);
        int score = getHeuristik(grid);
        revertPlay(play);

        if(player == 'R') {
            return score;
        }else{
            return -score;
        }
    }
    public static void playGame(){
        Scanner in = new Scanner(System.in);



        //initialize array


        int turn = 1;
        char player = 'R';
        boolean winner = false;

        //play a turn
        while (winner == false && turn <= 42){
            boolean validPlay;
            int play;
            do {
                display(grid);
                System.out.println("schore: "+getHeuristik(grid));

                System.out.print("Player " + player + ", choose a column: ");

                play = in.nextInt();



                //validate play
                validPlay = validate(play,grid);

            }while (validPlay == false);

            //drop the checker
            for (int row = grid.length-1; row >= 0; row--){
                if(grid[row][play] == ' '){
                    grid[row][play] = player;
                    break;
                }
            }

            //determine if there is a winner
            winner = isWinner(player,grid);

            //switch players
            if (player == 'R'){
                player = 'B';
            }else{
                player = 'R';
            }

            turn++;
        }
        display(grid);

        if (winner){
            if (player=='R'){
                System.out.println("Black won");
            }else{
                System.out.println("Red won");
            }
        }else{
            System.out.println("Tie game");
        }
    }

    public static void display(char[][] g){
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println("---------------");
        for (int row = 0; row < grid.length; row++){
            System.out.print("|");
            for (int col = 0; col < grid[0].length; col++){
                System.out.print(g[row][col]);
                System.out.print("|");
            }
            System.out.println();

        }
        System.out.println(" 0 1 2 3 4 5 6");
        System.out.println();
        System.out.println("R: "+getPlayerScore('R',grid));
        System.out.println("B: "+getPlayerScore('B',grid));
        System.out.println("ges B: "+getHeuristik(grid));
    }
    
    public static boolean validate(int column, char[][] grid){
        //valid column?
        try {
            if (column < 0 || column > grid[0].length) {
                return false;
            }

            //full column?
            if (grid[0][column] != ' ') {
                return false;
            }

            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static int getHeuristik(char[][] grid){

        //System.out.println("R: "+getPlayerScore('R',grid));
        //System.out.println("B: "+getPlayerScore('B',grid));

        return getPlayerScore(AI,grid) - getPlayerScore(otherPlayer(AI),grid);


    }
    public static int getPlayerScore(char player, char[][] grid){
        int score = 0;

        //check for 4 across
        for(int row = 0; row<grid.length; row++){
            for (int col = 0;col < grid[0].length - 3;col++){
                int playerCount = 0;
                if (grid[row][col] == player)playerCount++; else if(grid[row][col] != ' ') playerCount = -100;
                if (grid[row][col+1] == player)playerCount++; else if(grid[row][col+1] != ' ') playerCount = -100;
                if (grid[row][col+2] == player)playerCount++; else if(grid[row][col+2] != ' ') playerCount = -100;
                if (grid[row][col+3] == player)playerCount++; else if(grid[row][col+3] != ' ') playerCount = -100;
                if (playerCount == 1) score++;
                if (playerCount == 2) score += 5;
                if (playerCount == 3) score += 20;
                if (playerCount == 4) score += 10000;

            }
        }
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length; col++){
                int playerCount = 0;
                if (grid[row][col] == player)playerCount++; else if(grid[row][col] != ' ') playerCount = -100;
                if (grid[row+1][col] == player)playerCount++; else if(grid[row+1][col] != ' ') playerCount = -100;
                if (grid[row+2][col] == player)playerCount++; else if(grid[row+2][col] != ' ') playerCount = -100;
                if (grid[row+3][col] == player)playerCount++; else if(grid[row+3][col] != ' ') playerCount = -100;
                if (playerCount == 1) score++;
                if (playerCount == 2) score += 5;
                if (playerCount == 3) score += 20;
                if (playerCount == 4) score += 10000;
            }
        }
        for(int row = 3; row < grid.length; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                int playerCount = 0;
                if (grid[row][col] == player)playerCount++; else if(grid[row][col] != ' ') playerCount = -100;
                if (grid[row-1][col+1] == player)playerCount++; else if(grid[row-1][col+1] != ' ') playerCount = -100;
                if (grid[row-2][col+2] == player)playerCount++; else if(grid[row-2][col+2] != ' ') playerCount = -100;
                if (grid[row-3][col+3] == player)playerCount++; else if(grid[row-3][col+3] != ' ') playerCount = -100;
                if (playerCount == 1) score++;
                if (playerCount == 2) score += 5;
                if (playerCount == 3) score += 20;
                if (playerCount == 4) score += 10000;
            }
        }
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                int playerCount = 0;
                if (grid[row][col] == player)playerCount++; else if(grid[row][col] != ' ') playerCount = -100;
                if (grid[row+1][col+1] == player)playerCount++; else if(grid[row+1][col+1] != ' ') playerCount = -100;
                if (grid[row+2][col+2] == player)playerCount++; else if(grid[row+2][col+2] != ' ') playerCount = -100;
                if (grid[row+3][col+3] == player)playerCount++; else if(grid[row+3][col+3] != ' ') playerCount = -100;
                if (playerCount == 1) score++;
                if (playerCount == 2) score += 5;
                if (playerCount == 3) score += 20;
                if (playerCount == 4) score += 10000;
            }
        }

        // vierer
        if (isWinner(player,grid))
            score += 100000;

        return score;
    }
    public static boolean isWinner(char player, char[][] grid){
        //check for 4 across
        for(int row = 0; row<grid.length; row++){
            for (int col = 0;col < grid[0].length - 3;col++){
                if (grid[row][col] == player   && 
                    grid[row][col+1] == player &&
                    grid[row][col+2] == player &&
                    grid[row][col+3] == player){
                    return true;
                }
            }           
        }
        //check for 4 up and down
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length; col++){
                if (grid[row][col] == player   && 
                    grid[row+1][col] == player &&
                    grid[row+2][col] == player &&
                    grid[row+3][col] == player){
                    return true;
                }
            }
        }
        //check upward diagonal
        for(int row = 3; row < grid.length; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                if (grid[row][col] == player   && 
                    grid[row-1][col+1] == player &&
                    grid[row-2][col+2] == player &&
                    grid[row-3][col+3] == player){
                    return true;
                }
            }
        }
        //check downward diagonal
        for(int row = 0; row < grid.length - 3; row++){
            for(int col = 0; col < grid[0].length - 3; col++){
                if (grid[row][col] == player   && 
                    grid[row+1][col+1] == player &&
                    grid[row+2][col+2] == player &&
                    grid[row+3][col+3] == player){
                    return true;
                }
            }
        }
        return false;
    }
}
