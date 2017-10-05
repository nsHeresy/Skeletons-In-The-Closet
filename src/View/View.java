package View;

import Map.*;
import Model.*;
import Utils.*;
import Entities.*;
import Controller.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * The main view of the game
 * Contained within a JFrame, the View handles the graphical representation of the game world
 * A separate JPanel is used to contain the PlayerPanel and the button controls.
 *
 * Created: 18/09/2017
 * @author Nicholas Snellgrove
 */
public class View extends JComponent implements Observer{

    //Panels of the view
    JFrame frame;
    PlayerPanel playerStats;
    Menu menu;
    JPanel interfacePanel = new JPanel();
    JPanel buttonPanel;
    JMenuBar menuBar = new JMenuBar();
    JPanel pauseMenu;

    //menu buttons
    JButton pauseBtn = new JButton("Pause");

    //control buttons
    JButton up = new JButton();
    JButton left = new JButton();
    JButton down = new JButton();
    JButton right = new JButton();
    JButton attack = new JButton();
    JButton AoE = new JButton();
    JButton defend = new JButton();

    //MVC fields
    Controller controller;
    Model model;
    Resources resources = new Resources();

    //visual fields
    int startX;
    int startY;
    int tileSize = 50;

    //other fields
    Image border;

    public boolean pauseMenuVisible = false;

    public View(Model m) {

        this.model = m;
        controller = new Controller(m, this);
        model.addObserver(this);

        //setting up the frame
        frame = new JFrame(Resources.TITLE);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(Resources.WINDOW_SIZE);
        frame.setFocusable(true);

        //setting up the other panels
        playerStats = new PlayerPanel(frame);

        //build the interface panel
        //a lot of logic in this part
        //so its a separate method
        buildInterface();
        buildControls();

        //adding the buttons to the menubar
        menuBar.add(pauseBtn);

        //adding the components
        frame.setJMenuBar(menuBar);
        frame.add(this, BorderLayout.CENTER);
        frame.add(interfacePanel, BorderLayout.SOUTH);

        //final setups

        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        this.setDoubleBuffered(true);
        frame.setLocationRelativeTo(null);

        border = Resources.getImage("border");

        this.getGraphics().drawImage(border, 0, this.getHeight()-border.getHeight(null),null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        long start = System.currentTimeMillis();

        Graphics2D gg = (Graphics2D) g;

        if(!pauseMenuVisible){
            drawWorld(gg);
            //drawNewShadows(gg, model.getCurrentRoom());
            drawShadows(gg, model.getPlayerLocation());
        }
        else{
            showPauseMenu(gg);
        }

        g.drawImage(border, 0, this.getHeight()-border.getHeight(null),null);

        //drawShadows(gg, model.getPlayerLocation());

        long end = System.currentTimeMillis()-start;
        System.out.println("View update took ms " + end);
    }

    @Override
    public Dimension getPreferredSize() {
        int width = frame.getWidth();
        int height = frame.getHeight()*4/5;
        Dimension screen = new Dimension(width, height);
        return screen;
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }


    ////////////////////////
    //Visual methods below//
    ////////////////////////


    /**
     * Will be called by a key press or a button, handled by the controller.
     * Will display a pop-up menu, with buttons on it to save, load, quit and resume.
     */

    private void showPauseMenu(Graphics2D g){
        this.setVisible(false);

        pauseMenu = new JPanel();
        pauseMenu.setLayout(new BoxLayout(pauseMenu, BoxLayout.PAGE_AXIS));

        //graphics.setColor(new Color(32,39,32));
        //graphics.fillRect(0,0, this.getWidth(), this.getHeight()-border.getHeight(null));

        JButton save = new JButton("Save"); save.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton load = new JButton("Load"); load.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton help = new JButton("Help"); help.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton quit = new JButton("Quit"); quit.setAlignmentX(Component.CENTER_ALIGNMENT);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Saving will happen here");
            }
        });

        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Loading will happen here");
            }
        });

        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, Resources.HELPDESC);
            }
        });

        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ans = JOptionPane.showConfirmDialog(frame, "Are you sure that you want to exit?");
                if(ans == 0) {
                    frame.dispose();
                    System.exit(0);
                }
            }
        });

        JLabel title = new JLabel("Game Paused");
        title.setFont(new Font("SansSerif", Font.PLAIN, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        pauseMenu.add(title);
        pauseMenu.add(save);
        pauseMenu.add(load);
        pauseMenu.add(help);
        pauseMenu.add(quit);

        frame.remove(this);
        frame.add(pauseMenu);
    }

    private void removePauseMenu(){
        if(pauseMenu != null){
            frame.remove(pauseMenu);
        }

        System.out.println("removing pause menu");
        this.setVisible(true);
        frame.add(this, BorderLayout.CENTER);
    }

    public void pauseMenuToggle(){
        pauseMenuVisible = !pauseMenuVisible;
        if(!pauseMenuVisible){ removePauseMenu(); pauseBtn.setText("Pause");}
        else pauseBtn.setText("Resume");
        repaint();
    }



    /**
     * Will draw the room in the game world that the player is currently in.
     *
     * @param g the graphics2D object to draw to
     */
    public void drawWorld(Graphics2D g){
        g.setColor(new Color(32,39,32));
        g.fillRect(0,0, this.getWidth(), this.getHeight()-border.getHeight(null));
        this.drawRoom(g, model.getCurrentRoom());
    }


    /**w
     * Will draw any given room to the graphics pane.
     * Should scale depending on size of window (Maybe)
     * Good for testing
     *
     * @param g the graphics2D object to draw to
     */
    public void drawRoom(Graphics2D g, Room r){
        int roomWidth = (r.getWidth()*50);
        int roomHeight = (r.getHeight()*50);

        startX = (this.getWidth()/2)-roomWidth/2;
        startY = ((this.getHeight()/2)-roomHeight/2)-tileSize/3;
        for(int y = 0; y < r.getHeight(); y++){
            for(int x = 0; x < r.getWidth(); x++){
                drawTile(g,r.getTileSet(), model.getCurrentRoom().getTileAtLocation(x,y), (x*tileSize)+startX, (y*tileSize)+startY);
            }
        }
    }

    /**
     * Will draw any given tile at its appropriate coordinates
     * Good for testing
     * Should scale based on size of window(maybe)
     *
     *
     * @param g the graphics2D object to draw to
     * @param tileSet the tileset of the current room
     * @param tile the tile to be drawn
     */
    public void drawTile(Graphics2D g, TileSet tileSet, Tile tile, int x, int y){

        Image img;

        int indexY = (y-startY)/tileSize;
        int indexX = (x-startX)/tileSize;
        if(!(model.getCurrentRoom().getTileAtLocation(indexX,indexY) instanceof  DoorTile)){
            if(indexY == 0){ //draw wall at the top of the room
                img = tileSet.getWallTop();
                g.drawImage(img, x, y-img.getHeight(null), null);
            }
            if(indexY == model.getCurrentRoom().getHeight()-1){
               img = tileSet.getWallBottom();
               g.drawImage(img, x, y+tileSize, null);
            }
            if(indexX == 0){
                img = tileSet.getWallLeft();
                g.drawImage(img, x-img.getWidth(null), y, null);
            }
            if(indexX == model.getCurrentRoom().getWidth()-1){
                img = tileSet.getWallRight();
                g.drawImage(img, x+tileSize, y, null);
            }
        }

        img = tileSet.getFloor();
        if(img == null)
            return;
        g.drawImage(img, x,y,null);

        if(tile instanceof DoorTile){
            img = tileSet.getFloor();
            if(img == null)
                return;
            g.drawImage(img, x,y,null);
          //TODO add the point to a list of light sources
        }

        if(tile.getEntity() != null){
            drawEntity(g, tile.getEntity(), tileSet, x,y);
        }
    }

    /**
     * Will draw an entity at its appropriate coordinates
     * Should scale based on size of window(maybe)
     *
     * @param g the graphics2D object to draw to
     */

    public void drawEntity(Graphics2D g, Entity e, TileSet tileSet, int x, int y){
        if(e instanceof Nothing)
            return;
        else if(e instanceof Player){
            g.setColor(Color.blue);
            g.fillOval( (tileSize/4)+x,tileSize/4+y,tileSize/2,tileSize/2);

        }
        else if(e instanceof  Wall){
            Image img = tileSet.getWall();
            if(img == null)
                return;
            g.drawImage(img, x,y,null);

        }
        else if(e instanceof Enemy){
            g.setColor(Color.red);
            g.fillOval((tileSize/4)+x,tileSize/4+y,tileSize/2,tileSize/2);
        }
    }

    public void drawShadows(Graphics2D g, Point p){
        Point centerPoint = new Point((int) (startX+(p.getX()*tileSize) + tileSize/2), (int) ((startY+p.getY()*tileSize)) + tileSize/2);
        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {Resources.transparent, Resources.transparent, Resources.shadowBack};
        RadialGradientPaint shadow = new RadialGradientPaint(centerPoint, Resources.radius, dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g.setPaint(shadow);
        g.fillRect(0,0,this.getWidth(),this.getHeight());
    }

    public void drawNewShadows(Graphics2D g, Room currentRoom){
        BufferedImage shadowOverlay = new BufferedImage((currentRoom.getWidth()*tileSize)+tileSize,
                        (currentRoom.getHeight()*tileSize)+tileSize,BufferedImage.TYPE_INT_ARGB);

        Graphics2D overlayGraphics = shadowOverlay.createGraphics();

        Point playerLocation = currentRoom.getPlayerLocation();
        Point coordinates = new Point( (int)playerLocation.getX()*tileSize + tileSize/2, (int)playerLocation.getY()*tileSize + tileSize/2);
        ArrayList<Point> lightSources = new ArrayList<>();

        if(currentRoom.isRoomCleared()) {
            for (int y = 0; y < currentRoom.getHeight(); y++) {
                for (int x = 0; x < currentRoom.getWidth(); x++) {
                    if (currentRoom.getTileAtLocation(x, y) instanceof DoorTile) {
                        lightSources.add((new Point(x,y)));
                    }
                }
            }
        }



        float[] dist = {0.0f, 0.5f, 1.0f};
        Color[] colors = {Resources.transparent, Resources.transparent, Resources.shadowBack};
        RadialGradientPaint shadow = new RadialGradientPaint(coordinates, Resources.radius, dist, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        overlayGraphics.setPaint(shadow);
        overlayGraphics.fillRect(0,0,shadowOverlay.getWidth(),shadowOverlay.getHeight());

        float[] lightDist = {0.0f, 1.0f};
        Color[] lightColours = {Resources.doorGlow, Resources.transparent};
        for(Point p : lightSources){
            Point lightSource = new Point( (int)p.getX()*tileSize + tileSize, (int)p.getY()*tileSize + tileSize);
            RadialGradientPaint light = new RadialGradientPaint(lightSource, Resources.lightRadius, lightDist, lightColours, MultipleGradientPaint.CycleMethod.NO_CYCLE);
            overlayGraphics.setPaint(light);
            overlayGraphics.fillRect(0,0,shadowOverlay.getWidth(),shadowOverlay.getHeight());
        }

        g.drawImage(shadowOverlay, startX-tileSize/2, startY-tileSize/2, null);


    }

    /**
     * Should animate an entity doing an action (Movement, attack, death, open, close...)
     * Maybe
     *
     * @param g the graphics2D object to draw to
     */

    public void animateEntity(Graphics2D g, Entity e){

    }

    /**
     * Maybe the screen should fade out and fade back into a new room
     *
     * @param g the graphics2D object to draw to
     */

    public void changeRoom(Graphics2D g){

    }


    /**
     * If we do any cutscene type thingies, I guess we can have something like this
     *
     * @param g the graphics2D object to draw to
     */
    public void showScene(Graphics2D g){

    }


    /**
     * If an entity speaks, and nobody has programmed a way for it to speak out loud,
     * did it speak at all?
     *
     * @param g the plane of existence
     * @param e the entity that's having a crisis
     */
    public void showText(Graphics2D g, Entity e){

    }

    /////////////////////////
    //Utility Methods below//
    /////////////////////////

    public void buildControls(){
        //custom actionListeners for the menu buttons
        this.pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseMenuToggle();
            }
        });

        frame.addKeyListener(controller);
        this.addMouseListener(controller);
        left.addActionListener(controller);
        up.addActionListener(controller);
        down.addActionListener(controller);
        right.addActionListener(controller);
        attack.addActionListener(controller);
        defend.addActionListener(controller);
        AoE.addActionListener(controller);
    }


    public void buildInterface(){

        attack.setName("Attack");
        defend.setName("Defend");
        AoE.setName("AOE");
        up.setName("Up");
        down.setName("Down");
        left.setName("Left");
        right.setName("Right");

        attack.setRequestFocusEnabled(false);
        defend.setRequestFocusEnabled(false);
        AoE.setRequestFocusEnabled(false);
        up.setRequestFocusEnabled(false);
        down.setRequestFocusEnabled(false);
        left.setRequestFocusEnabled(false);
        right.setRequestFocusEnabled(false);

        //setting up the movement buttons
        up.setIcon(new ImageIcon(Resources.getImage("up")));
        up.setBackground(Color.black);
        down.setIcon(new ImageIcon(Resources.getImage("down")));
        down.setBackground(Color.black);
        left.setIcon(new ImageIcon(Resources.getImage("left")));
        left.setBackground(Color.black);
        right.setIcon(new ImageIcon(Resources.getImage("right")));
        right.setBackground(Color.black);

        //setting up the action buttons
        attack.setIcon(new ImageIcon(Resources.getImage("attack")));
        attack.setBackground(Color.black);
        AoE.setIcon(new ImageIcon(Resources.getImage("aoe")));
        AoE.setBackground(Color.black);
        defend.setIcon(new ImageIcon(Resources.getImage("defend")));
        defend.setBackground(Color.black);


        //Setting up the gridBagLayout
        GridBagConstraints c = new GridBagConstraints();


        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        JPanel movePanel = new JPanel(new GridBagLayout());
        movePanel.setBackground(Color.BLACK);
        buttonPanel.add(movePanel);


        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1.0;
        //set the constraints for each button before adding it to the panel
        c.gridx = 1;
        c.gridy = 0;
        movePanel.add(up, c);
        c.gridx = 1;
        c.gridy = 2;
        movePanel.add(down, c);
        c.gridx = 0;
        c.gridy = 1;
        movePanel.add(left, c);
        c.gridx = 2;
        c.gridy = 1;
        movePanel.add(right, c);
        buttonPanel.add(attack);
        buttonPanel.add(AoE);
        buttonPanel.add(defend);


        //setting up the interface panel
        interfacePanel.setBackground(Color.black);
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()/5));
        interfacePanel.add(buttonPanel, BorderLayout.WEST);
        interfacePanel.add(playerStats, BorderLayout.EAST);

    }
}
