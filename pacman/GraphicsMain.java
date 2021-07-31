package games.pacman;

import javax.swing.JFrame;
import java.awt.event.KeyListener;

public class GraphicsMain {
    public static void main(String[] args){
        PacmanEngine p1 = new PacmanEngine();
        TimerThread timer = new TimerThread(p1);
        JFrame frame = new JFrame();
        frame.add(p1);
        frame.pack();
        frame.addKeyListener(p1);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        timer.start();
    }
}


