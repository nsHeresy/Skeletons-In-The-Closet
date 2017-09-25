package View;

import Entities.Player;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * An extension of JComponent to control the display of things that
 * won't be interacted with (Like health bar, special power bar, experience bar etc)
 *
 * Created: 18/08/17
 * @author Nicholas Snellgrove
 */
public class PlayerPanel extends JComponent implements Observer {

    Player player;
    JFrame parent;

    //drawing calc fields
    int startX = 50;
    int startY = 10;

    public PlayerPanel(JFrame parent) {
        this.parent = parent;
        player = new Player(50,50);
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }

    @Override
    public void paintComponent(Graphics _g) {
        _g.setColor(Color.red);
        _g.fillRect(0,0,50,50);
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(parent.getWidth()/2, parent.getHeight()/5);
    }
}
