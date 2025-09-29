import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.swing.*;


public class main {

    public static Integer selectedFieldX = null;
    public static Integer selectedFieldY = null;
    public static Integer[] movesX = new Integer[500];
    public static Integer[] movesY = new Integer[500];

    public static char turn = 'w';

    // public static String[] board = {
    //     "b-r", "b-n", "b-b", "b-q", "b-k", "b-b", "b-n", "b-r",
    //     "b-p", "b-p", "b-p", "b-p", "b-p", "b-p", "b-p", "b-p",
    //     "", "", "", "", "", "", "", "",
    //     "", "", "", "", "", "", "", "",
    //     "", "", "", "", "", "", "", "",
    //     "", "", "", "", "", "", "", "",
    //     "w-p", "w-p", "w-p", "w-p", "w-p", "w-p", "w-p", "w-p",
    //     "w-r", "w-n", "w-b", "w-q", "w-k", "w-b", "w-n", "w-r"
    // };

    public static String[] board = {
        "b-r", "b-n", "b-b", "b-q", "b-k", "b-b", "b-n", "b-r",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "w-n", "", "", "",
        "", "b-r", "", "", "", "", "b-n", "",
        "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "",
        "w-r", "w-n", "", "w-q", "w-k", "w-b", "w-n", "w-r"
    };


    public static void main(String[] args) {
        window();
    }

    public static void window() {
        JFrame frame = new JFrame("chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(415, 435);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        frame.add(panel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / 50;
                int y = e.getY() / 50;
                
                //move
                if(coordinateInMoves(x, y)) {
                    board[x + y * 8] = board[selectedFieldX + selectedFieldY * 8];
                    board[selectedFieldX + selectedFieldY * 8] = "";
                    selectedFieldX = null;
                    selectedFieldY = null;
                    turn = turn == 'w' ? 'b' : 'w';
                    movesX = new Integer[500];
                    movesY = new Integer[500];
                    checkForPawnPromotion();                    
                }
                //select Figure
                else if(board[x + y * 8] != "" && (board[x + y * 8].charAt(0) == turn)) {
                    selectedFieldX = x;
                    selectedFieldY = y;
                    getMoves();
                //disselect Figure
                } else {
                    selectedFieldX = null;
                    selectedFieldY = null;
                    movesX = new Integer[500];
                    movesY = new Integer[500];
                }
                panel.repaint();
            }
        });
    }

    public static void checkForPawnPromotion() {
        for (int x = 0; x < 8; x++) {
            if(board[x + 0 * 8] == "w-p") board[x + 0 * 8] = "w-q";
            if(board[x + 7 * 8] == "b-p") board[x + 7 * 8] = "b-q";
        }
    }

    public static void draw(Graphics g)  {
        // draw board
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if((x + 7 * y) % 2 == 0) {
                    g.setColor(new Color(0xffffff));
                } else g.setColor(new Color(0x666666));

                if(selectedFieldX != null && selectedFieldX == x && selectedFieldY == y) g.setColor(new Color(0, 200, 0));
                g.fillRect(x * 50, y * 50, 50, 50);
            }
        }

        // draw figures
        for(int x = 0; x<8; x++) {
            for (int y = 0; y < 8; y++) {
                if(board[x + y * 8] == "") continue;
                BufferedImage img = getImage(board[x + y * 8]);
                g.drawImage(img, x * 50, y * 50, 50, 50, null);
            }
        }

        // draw moveOptions
        for(int x = 0; x<8; x++) {
            for (int y = 0; y < 8; y++) {
                if(coordinateInMoves(x, y)) {
                    g.setColor(new Color(0, 200, 0));
                    g.fillOval(x * 50 + 15, y * 50 + 15, 20, 20);
                }
            }
        }
    }

    public static boolean coordinateInMoves(int x, int y) {
        boolean contain = false;
        for (int i = 0; i < movesX.length; i++) {
            if(movesX[i] == null || movesY[i] == null) continue;
            if(movesX[i] == x && movesY[i] == y) contain = true;
        }
        return contain;
    } 

    public static void getMoves() {
        movesX = new Integer[500];
        movesY = new Integer[500];
        if(selectedFieldX == null || selectedFieldY == null) return;
        int x = selectedFieldX;
        int y = selectedFieldY;

        String figure = getFigure(x, y);
        switch(figure.charAt(2)) {
            case 'p':
                addPawnMoveLogic();
                break;
            case 'n':
                addKnightLogic();
                break;
            case 'q':
                addStraightMoves();
                addDiagonalMoves();
                break;
            case 'k':
                addMove(x-1, y-1);
                addMove(x+1, y+1);
                addMove(x+1, y-1);
                addMove(x-1, y+1);
                addMove(x-1, y);
                addMove(x+1, y);
                addMove(x, y-1);
                addMove(x, y+1);
                break;
            case 'b':
                addDiagonalMoves();
                break;
            case 'r':
                addStraightMoves();
                break;
        }

    }

    public static void addKnightLogic() {
        int x = selectedFieldX;
        int y = selectedFieldY;
        int[][] knightMoves = { //up right, up left, down right, down left
	    {2, -1}, {1, -2}, {-2, -1}, {-1, -2}, {2, 1}, {1, 2}, {-2, 1}, {-1, 2}
	};


	for (int i = 0; i<knightMoves.length; i++) {
	
	if(x+knightMoves[i][0]>7 || x+knightMoves[i][0]<0 || y+knightMoves[i][1]>7 || y+knightMoves[i][1]<0) continue;
	
	//add move if place is empty or opponent
	if(getFigure(x + knightMoves[i][0], y + knightMoves[i][1]) == "" || getFigure(x+knightMoves[i][0], y+knightMoves[i][1]).charAt(0) == (turn == 'w' ? 'b' : 'w')) 	addMove(x+knightMoves[i][0], y+knightMoves[i][1]); 	
	}

    }

    public static void addMove(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return;

        for (int i = 0; i < movesX.length; i++) {
            if (movesX[i] != null) continue;
            movesX[i] = x;
            movesY[i] = y;
            break;
        }
    }

    public static void addPawnMoveLogic() {
        int x = selectedFieldX;
        int y = selectedFieldY;
        if(turn == 'w') {
            move: if(getFigure(selectedFieldX, selectedFieldY - 1) == "") {
                if(selectedFieldY < 6) {
                    addMove(x, y - 1);
                    break move;
                }
                addMove(x, y - 1);
                addMove(x, y - 2);
                break move;
            }
            String fieldtoHitRight = getFigure(selectedFieldX + 1, selectedFieldY - 1);
            String fieldtoHitLeft = getFigure(selectedFieldX - 1, selectedFieldY - 1);

            if (fieldtoHitRight != "") {
                if(getFigure(selectedFieldX + 1, selectedFieldY - 1).charAt(0) == 'b') {
                    addMove(x + 1, y - 1);
                }
            }
            if (fieldtoHitLeft != "") {
                if(getFigure(selectedFieldX - 1, selectedFieldY - 1).charAt(0) == 'b') {
                    addMove(x - 1, y - 1);
                }
            }
        } else if (turn == 'b') {
            if(getFigure(selectedFieldX, selectedFieldY + 1) == "") {
                if(y > 1) {
                    addMove(x, y + 1);
                    return;
                }
                addMove(x, y + 1);
                addMove(x, y + 2);
                return;
            }
        }
    }

    public static void addStraightMoves() {
        right: for(int a = 1; a<8; a++) {
            if(selectedFieldX + a > 7) break right;
            String positionToMove = board[selectedFieldX + a + (selectedFieldY * 8)];
            //destroy loop before adding field so it ends before own figure
            if(positionToMove != "" && positionToMove.charAt(0) == turn) break right;
            addMove(selectedFieldX + a, selectedFieldY);
            //destroy loop after adding field so you cant hit figures that stand behind figures of the enemy 
            if(positionToMove != "" && positionToMove.charAt(0) == (turn == 'w' ? 'b' : 'w')) break right;
        }
        left: for(int a = 1; a<8; a++) {
            if(selectedFieldX - a < 0) break left;
            String positionToMove = board[selectedFieldX - a + (selectedFieldY * 8)];
            if(positionToMove != "" && positionToMove.charAt(0) == turn) break left;
            addMove(selectedFieldX - a, selectedFieldY);
            if(positionToMove != "" && positionToMove.charAt(0) == (turn == 'w' ? 'b' : 'w')) break left;
        }
        up: for(int a = 1; a<8; a++) {
            if(selectedFieldY - a < 0) break up;
            String positionToMove = board[selectedFieldX + (selectedFieldY * 8) - a];
            if(positionToMove != "" && positionToMove.charAt(0) == turn) break up;
            addMove(selectedFieldX, selectedFieldY - a);
            if(positionToMove != "" && positionToMove.charAt(0) == (turn == 'w' ? 'b' : 'w')) break up;
        }
        down: for(int a = 1; a<8; a++) {
            if(selectedFieldY + a > 7) break down;
            String positionToMove = board[selectedFieldX + (selectedFieldY * 8) + a];
            if(positionToMove != "" && positionToMove.charAt(0) == turn) break down;
            addMove(selectedFieldX, selectedFieldY + a);
            if(positionToMove != "" && positionToMove.charAt(0) == (turn == 'w' ? 'b' : 'w')) break down;
        }
    }

    public static void addDiagonalMoves() {
        for (int a = 0; a<8; a++) {
            addMove(selectedFieldX + a, selectedFieldY + a);
            addMove(selectedFieldX - a, selectedFieldY - a);
            addMove(selectedFieldX + a, selectedFieldY - a);
            addMove(selectedFieldX - a, selectedFieldY + a);
        }
    }

    public static String getFigure(int x, int y) {
        return board[x + y * 8];
    }

    public static BufferedImage getImage(String name) {
        BufferedImage img = null;
        try {
            InputStream stream = main.class.getResourceAsStream("public/" + name + ".png");
            img = ImageIO.read(stream);
        } catch(Exception err) {
            System.out.println(err);
            return null;
        }
        return img;
    }

}
