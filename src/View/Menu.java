package View;

import Controller.*;
import Model.*;
import Utils.Resources;
import Utils.SaveLoad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * The game 'main menu'
 *
 * @author Morgan French-Stagg
 */
public class Menu extends JComponent {
    JFrame frame;

    public Menu() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        JLabel title = new JLabel(Resources.TITLE);
        title.setFont(new Font("SansSerif", Font.PLAIN, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);


        //new game button
        JButton newGame = new JButton(Resources.MENU_NEWGAME_BUTTON);
        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });
        panel.add(newGame);

        //saved games
        JButton savedGame = new JButton(Resources.MENU_SAVEDGAME_BUTTON);
        savedGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        savedGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveLoad saveLoad = new SaveLoad();
                if(saveLoad.saves.size() >= 1) {
                    int result = JOptionPane.showOptionDialog(frame, Resources.LOAD_PROMPT_MESSAGE, Resources.LOAD_TITLE_MESSAGE, 0, 0, null, saveLoad.saves.keySet().toArray(), saveLoad.saves.get(saveLoad.saves.keySet().toArray()[0]));
                    if(result == -1) return;
                    Model m = saveLoad.load((String) saveLoad.saves.keySet().toArray()[result]);
                    if (m == null) {
                        JOptionPane.showMessageDialog(frame, Resources.LOAD_UNSUCCESSFUL_MESSAGE);
                    } else {
                        loadGame(m);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(frame, Resources.LOAD_NOSAVES_MESSAGE);
                }
            }
        });
        panel.add(savedGame);

        //Help button
        JButton infoBut = new JButton(Resources.MENU_HELP_BUTTON);
        infoBut.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, Resources.HELPDESC);
            }
        });
        panel.add(infoBut);

        //quit button
        JButton quit = new JButton(Resources.MENU_QUIT_BUTTON);
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
        panel.add(quit);

        frame.add(panel);

        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setFocusable(true);

    }

    private void startNewGame(){
        Model model = new Model();
        try {
             model.initialise();
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame.dispose();
        SwingUtilities.invokeLater(()-> new View(model));
    }

    private void loadGame(Model m){
        frame.dispose();
        SwingUtilities.invokeLater(()-> new View(m));
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(200,200);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
