package main;

import ai.PathFinder;
import entity.Entity;
import interactive_resources.SuperResource;
import interactive_resources.Tree;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import entity.Player;
import interactive_resources.ResourceManager;
import monster.MonsterManager;
import monster.GreenSlime;
import object.ObjectManager;
import object.SuperObject;
import tile.TileManager;
import interactive_resources.Rock;
import java.io.FileInputStream;
import java.util.ArrayList;

// Game Application
public class GameApplication extends Application {
    // Screen, World Settings
    final int ORIGINAL_TILE_SIZE = 16; // 16x16 tile
    public final int SCALE = 3;
    public final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // 48x48 tile
    public final int MAX_SCREEN_COL_SMALL = 24;
    public final int MAX_SCREEN_ROW_SMALL = 16;
    // Current maximum screen col and row values
    public int MAX_SCREEN_COL = MAX_SCREEN_COL_SMALL;
    public int MAX_SCREEN_ROW = MAX_SCREEN_ROW_SMALL;

    public final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL; // 1152 px
    public final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW; // 768 px
    public final int MAX_WORLD_COL = 100;
    public final int MAX_WORLD_ROW = 100;
    int FPS = 60;

    public TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler(this, false);
    Sound sound = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public ObjectManager objM = new ObjectManager(this);
//    public SuperObject[] obj = new SuperObject[12];

    public ArrayList<SuperObject> obj = new ArrayList<>();
    public UI ui = new UI(this);
    public PathFinder pFinder = new PathFinder(this);


    public Player player = new Player(this, keyH);
    public ResourceManager Resource = new ResourceManager(this);
    public MonsterManager Monster = new MonsterManager(this);
    public MonsterManager GreenSlime = new MonsterManager(this);

    public SuperResource[] resource = new SuperResource[80];
    public Entity[] rock = new Entity[10];
    public Entity[] tree = new Entity[10];

    public Entity[] monster = new Entity[10];
    public ArrayList<GreenSlime> greenSlime = new ArrayList<>();

    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int characterState = 3;
    public Pane root;
    public boolean showTitleScreen = true;
    private Canvas canvas;
    public double mouseX;
    public double mouseY;
    ImageView upgradeBootsButton;
    ImageView upgradeMeleeButton;
    ImageView upgradeArmorButton;
    ImageView upgradeProjectileButton;

    Image buttonImage;
    Image titleLogo;
    Image backgroundImage;

    private Scene createGameScene(Stage stage) {
        root = new Pane();
        Scene gameScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        gameScene.setFill(Color.TURQUOISE);

        canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT); // Initialize canvas here
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gameScene.setOnKeyPressed(keyH = new KeyHandler(this, true));
        gameScene.setOnKeyReleased(keyH = new KeyHandler(this, false));
        gameScene.setOnMouseMoved(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        gameScene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                player.scythe.attack();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                player.slingshot.attack();
            }
        });


        root.getChildren().add(canvas);

        // Set button positions and sizes
        int buttonWidth = TILE_SIZE * 5 / 2;
        int buttonHeight = TILE_SIZE + 18;
        int buttonSpacing = TILE_SIZE + 6; // Spacing between buttons
        int buttonX = SCREEN_WIDTH / 9 + TILE_SIZE * 17 / 2;
        int buttonY = SCREEN_HEIGHT / 8 + TILE_SIZE + 28;

        try {
            Image buttonImage = new Image(new FileInputStream("Slime_Time/res/ui/button_image.png"), buttonWidth, buttonHeight, false, false);
            upgradeBootsButton = new ImageView(buttonImage);
            upgradeMeleeButton = new ImageView(buttonImage);
            upgradeArmorButton = new ImageView(buttonImage);
            upgradeProjectileButton = new ImageView(buttonImage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        upgradeArmorButton.setLayoutX(buttonX);
        upgradeArmorButton.setLayoutY(buttonY);

        upgradeMeleeButton.setLayoutX(buttonX);
        upgradeMeleeButton.setLayoutY(buttonY + buttonHeight + buttonSpacing);

        upgradeProjectileButton.setLayoutX(buttonX);
        upgradeProjectileButton.setLayoutY(buttonY + 2 * (buttonHeight + buttonSpacing));

        upgradeBootsButton.setLayoutX(buttonX);
        upgradeBootsButton.setLayoutY(buttonY + 3 * (buttonHeight + buttonSpacing));


        upgradeBootsButton.setVisible(false);
        upgradeMeleeButton.setVisible(false);
        upgradeArmorButton.setVisible(false);
        upgradeProjectileButton.setVisible(false);

        // Add buttons to the root pane
        root.getChildren().addAll(
                upgradeBootsButton,
                upgradeMeleeButton,
                upgradeArmorButton,
                upgradeProjectileButton
        );


        stage.setTitle("2D Adventure");
        return gameScene;
    }

    private Scene createTitleScreen(Stage stage) {
        Pane titleRoot = new Pane();
        Scene titleScene = new Scene(titleRoot, SCREEN_WIDTH, SCREEN_HEIGHT);

        //titleScene.setFill(Color.BLACK);
//        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("Slime_Time/res/ui/startscreenbg.png")));
        try {
            backgroundImage = new Image(new FileInputStream("Slime_Time/res/ui/startscreenbg.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(SCREEN_WIDTH, SCREEN_HEIGHT, false, false, false, false)
        );

        titleRoot.setBackground(new Background(background));
        setupTitleScene();

//        Image titleLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("Slime_Time/res/ui/slimetime_logo.png")));
        try {
            titleLogo = new Image(new FileInputStream("Slime_Time/res/ui/slimetime_logo.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        double titleWidth = titleLogo.getWidth() * 0.5; // Adjust the scaling factor as needed
        double titleHeight = titleLogo.getHeight() * 0.5;
        ImageView titleImageView = new ImageView(titleLogo);
        titleImageView.setFitWidth(titleWidth);
        titleImageView.setFitHeight(titleHeight);
        titleImageView.setLayoutX(SCREEN_WIDTH / 2 - titleLogo.getWidth() / 4);
        titleImageView.setLayoutY(SCREEN_HEIGHT / 2 - (titleLogo.getHeight() / 2) - 30 );
        titleRoot.getChildren().add(titleImageView);

        //Start game button
//        Image buttonImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("Slime_Time/res/ui/start_button.png")));
        try {
            buttonImage = new Image(new FileInputStream("Slime_Time/res/ui/start_button.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ImageView buttonImageView = new ImageView(buttonImage);
        double buttonWidth = 150; // Set the desired width
        double buttonHeight = 60; // Set the desired height
        buttonImageView.setFitWidth(buttonWidth);
        buttonImageView.setFitHeight(buttonHeight);

        Button startButton = new Button();
        startButton.setGraphic(buttonImageView);
        //startButton.setPadding(INSETS.Empty);
        startButton.setPrefSize(buttonWidth, buttonHeight);
        startButton.setStyle("-fx-background-color: transparent;"); // Set background color to transparent
        startButton.setLayoutX(SCREEN_WIDTH / 2 - 60); // Adjust X position
        startButton.setLayoutY(SCREEN_HEIGHT / 2 + 50); // Y position

        startButton.setOnAction(e -> {
            showTitleScreen = false;
            startGame(stage);});
        titleRoot.getChildren().add(startButton);

        return titleScene;
    }

    private void startGame(Stage stage) {
        showTitleScreen = false;
        gameState = playState;
        stage.setScene(createGameScene(stage));
        setupGame();
        startGameLoop(canvas.getGraphicsContext2D());
    }


    @Override
    public void start(Stage stage) {
        Scene titleScene = createTitleScreen(stage);
        stage.setTitle("2D Adventure");
        stage.setScene(titleScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // Any Methods to Set up at Beginning of Game
    public void setupGame() {
        Resource.setResource();
        Monster.setMonster();
        // playMusic(0);
        gameState = playState;
    }

    public void setupTitleScene() {
        gameState = titleState;
    }
    // Game Loop
    public void startGameLoop(GraphicsContext gc) {
        System.out.println("Setup Game");
        //setting objects/monsters (important for proper updates after resource/monster destruction)
        GreenSlime.setGreenSlime();
        objM.setObject();
        setupGame();
        new AnimationTimer() {
            final double drawInterval = 1000000000 / FPS; // Running at Certain FPS
            double delta = 0;
            long lastTime = System.nanoTime();
            long currentTime;
            long timer = 0;
            int drawCount = 0;
            boolean firstRender = true;

            @Override
            public void handle(long now) {
                currentTime = System.nanoTime();
                delta += (currentTime - lastTime) / drawInterval;
                timer += (currentTime - lastTime);
                lastTime = currentTime;
                if (showTitleScreen) {
                    setupTitleScene();
                    return;
                }

                if (delta >= 1) {
                    update();
                    render(gc);
                    --delta;
                    ++drawCount;
                    firstRender = false;
                }
                if (timer >= 1000000000) {
                    System.out.println("FPS: " + drawCount);
                    drawCount = 0;
                    timer = 0;
                }
            }
        }.start();
    }

    public void update() {
        //constantly checks for playstate, if gamestate changes, background processes pause
        if (gameState == playState) {
            player.update();

            //checking for green slime health
            for (int i = 0; i < greenSlime.size(); i++) {
                if (greenSlime.get(i) != null) {
                    greenSlime.get(i).updateGreenSlime(i);
                }
            }

            //checking for resource health
            for (int i = 0; i < resource.length; i++) {
                if (resource[i] != null) {
                    if (resource[i] instanceof Rock) {
                        ((Rock) resource[i]).update(i);
                    } else if (resource[i] instanceof Tree) {
                        ((Tree) resource[i]).update(i);
                        // Add logic here if needed for Tree resources
                    }
                }
            }
        }
    }


    public void render(GraphicsContext gc) {
        //rendering what the player can see
        long drawStart = 0;
        if (keyH.checkDrawTime) {
            drawStart = System.nanoTime();
        }
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        tileM.render(gc);

        for (SuperObject superObject : obj) {
            if (superObject != null) {
                superObject.render(gc, this);
            }
        }

        for (Entity superResource : resource) {
            if (superResource != null) {
                superResource.render(gc, this);
            }
        }

        for (Entity Rocks : rock) {
            if (Rocks != null) {
                (Rocks).render(gc, this);
            }
        }

        for (Entity Trees : tree) {
            if (Trees != null) {
                (Trees).render(gc, this);
            }
        }

        for (Entity Monsters : monster) {
            if (Monsters != null) {
                (Monsters).render(gc, this);
            }
        }

        for (Entity GreenSlime : greenSlime) {
            if (GreenSlime != null) {
                (GreenSlime).render(gc, this);
            }
        }

        player.render(gc);
        ui.render(gc);

        if (keyH.checkDrawTime) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("Draw Time: " + passed, 10, 400);
            System.out.println("Draw Time: " + passed);
            gc.fillText("World X: " + player.worldX, 10, 450);
            gc.fillText("World Y: " + player.worldY, 10, 500);
        }
    }

    public void playMusic(int i) {
        sound.play(0);
        sound.loop(0);
    }

    public void stopMusic() {
        sound.stop(1);
    }

    public void playSE(int i) {
        sound.play(i);
    }

}


