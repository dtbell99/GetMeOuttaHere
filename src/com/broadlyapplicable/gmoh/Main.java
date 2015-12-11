package com.broadlyapplicable.gmoh;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 *
 * @author dbell
 */
public class Main extends Application {

    private static final int MIN_X = 25;
    private static final int MIN_Y = 20;
    private static final int MAX_X = 775;
    private static final int MAX_Y = 545;
    private static final int APP_HEIGHT = 600;
    private static final int APP_WIDTH = 800;
    private static final int BORDER_STROKE_WIDTH = 3;
    private static final int TASK_MAX = 10000;
    private static final Color BORDER_COLOR = Color.MEDIUMAQUAMARINE;
    private static final Color BORDER_SAFE_COLOR = Color.ROYALBLUE;
    private static final int BLOCK_HEIGHT = 25;
    private static final int BLOCK_WIDTH = 25;
    private static final Color USER_COLOR = Color.DARKSEAGREEN;
    private static final Color[] BLOCK_COLORS = {Color.CRIMSON, Color.DARKMAGENTA, Color.DARKTURQUOISE, Color.DARKORANGE};
    private static final double USER_CENTER_X = 50.0;
    private static final double USER_CENTER_Y = 50.0;
    private static final double USER_RADIUS = 15.0;
    private static final double BLOCK_RADIUS = 15.0;
    private static final int JUMP = 2;
    private static final int SLEEP_MILLISECONDS = 5;
    private static final int HOLE_SIZE = 55;
    private static final String GO_BUTTON_TEXT = "Start";
    private static final String RESET_BUTTON_TEXT = "Reset";
    private static final String STOP_BUTTON_TEXT = "Stop";
    private static final String HIT_SOUND_FILE = "blip.wav";
    private static final String BLOCKS_LABEL = "Blocks";
    private static final double MIN_BLOCKS = 5;
    private static final double MAX_BLOCKS = 100;
    private static final double SLIDER_DEFAULT = 20;

    private Circle user;
    //private int totalBlocks = 1;
    private Label movesLabel;
    private int moves = 0;
    private Pane gamePane;
    private Set<Rectangle> blocks;
    private Set<Line> borders;
    private int currentDirection = -1;

    private Button resetButton;
    private Button stopButton;
    private Button goButton;
    private ProgressBar progressBar;
    private Slider blockSlider;
    private AudioClip hitPlayer;

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        HBox buttonHbox = new HBox();
        buttonHbox.setPadding(new Insets(10, 10, 10, 10));
        gamePane = new Pane();
        vbox.getChildren().add(gamePane);

        vbox.getChildren().add(buttonHbox);
        addButtons(buttonHbox);
        progressBar = new ProgressBar();
        buttonHbox.getChildren().add(progressBar);
        addSlider(buttonHbox);
        setupMultimedia();

        updateGamePane();

        Scene scene = new Scene(vbox, APP_WIDTH, APP_HEIGHT);
        primaryStage.setTitle("Get Me Outta Here");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupMultimedia() {
        hitPlayer = new AudioClip(new File(HIT_SOUND_FILE).toURI().toString());
    }

    private void addSlider(Pane pane) {
        blockSlider = new Slider();
        blockSlider.setMin(MIN_BLOCKS);
        blockSlider.setMax(MAX_BLOCKS);
        blockSlider.setValue(SLIDER_DEFAULT);
        blockSlider.setShowTickLabels(false);
        blockSlider.setShowTickMarks(true);
        blockSlider.setMajorTickUnit(50);
        blockSlider.setMinorTickCount(5);
        blockSlider.setBlockIncrement(10);
        Label blocksLabel = new Label(BLOCKS_LABEL);
        pane.getChildren().add(blocksLabel);
        pane.getChildren().add(blockSlider);

        blockSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                System.out.println("Update blockSlider value=" + blockSlider.getValue());
                reset();
            }
        });

        blockSlider.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent e) {
                //totalBlocks = (int) blockSlider.getValue();

            }

        });
    }

    private void updateGamePane() {
        addBorder(gamePane);
        addBlocks(gamePane);
        addUser(gamePane);
    }

    private void addButtons(Pane root) {
        addGoButton(root);
        addStopButton(root);
        addRestartButton(root);
    }

    private void addRestartButton(Pane root) {
        resetButton = new Button(RESET_BUTTON_TEXT);
        resetButton.setPadding(new Insets(10, 10, 10, 10));
        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                reset();
            }
        });
        root.getChildren().add(resetButton);
    }

    private void reset() {
        currentDirection = -2;
        System.out.println("Reset the Game");
        goButton.setDisable(false);
        stopButton.setDisable(true);
        gamePane.getChildren().clear();
        blocks.clear();
        updateGamePane();
    }

    private void addStopButton(Pane root) {
        stopButton = new Button(STOP_BUTTON_TEXT);
        stopButton.setPadding(new Insets(10, 10, 10, 10));
        stopButton.setDisable(true);
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                currentDirection = -2;
                stopButton.setDisable(true);
                goButton.setDisable(false);
            }
        });
        root.getChildren().add(stopButton);
    }

    private void addGoButton(Pane root) {
        goButton = new Button(GO_BUTTON_TEXT);
        goButton.setPadding(new Insets(10, 10, 10, 10));
        goButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                currentDirection = -1;
                goButton.setDisable(true);
                stopButton.setDisable(false);
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        for (int i = 1; i <= TASK_MAX; i++) {
                            updateProgress(i, TASK_MAX);
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    autoMove();
                                }
                            });
                            try {
                                Thread.sleep(SLEEP_MILLISECONDS);
                            } catch (InterruptedException interrupted) {
                            }
                            if (currentDirection == -2) {
                                System.out.println("User Escaped");
                                currentDirection = -1;
                                reset();
                                break;
                            }
                        }
                        return null;
                    }
                };
                progressBar.progressProperty().bind(task.progressProperty());
                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();
            }
        });
        root.getChildren().add(goButton);
    }

    private void autoMove() {
        int dir = getDirection();
        switch (dir) {
            case 0:
                updateUser(null, user.getCenterY(), 0, JUMP);
                break;
            case 1:
                updateUser(user.getCenterX(), null, JUMP, 0);
                break;
            case 2:
                updateUser(null, user.getCenterY(), 0, -JUMP);
                break;
            case 3:
                updateUser(user.getCenterX(), null, -JUMP, 0);
                break;
            case 4:
                updateUser(user.getCenterX(), user.getCenterY(), JUMP, JUMP);
                break;
            case 5:
                updateUser(user.getCenterX(), user.getCenterY(), -JUMP, JUMP);
                break;
            case 6:
                updateUser(user.getCenterX(), user.getCenterY(), -JUMP, -JUMP);
                break;
            case 7:
                updateUser(user.getCenterX(), user.getCenterY(), JUMP, -JUMP);
                break;
            default:
                System.out.println("Invalid Direction");

        }

    }

    private int getDirection() {
        if (currentDirection == -1) {
            Random rand = new Random();
            currentDirection = rand.nextInt(8);
        }
        return currentDirection;
    }

    private void updateUser(Double x, Double y, int xJump, int yJump) {

        if (user.getCenterX() > MAX_X && user.getCenterY() > (MAX_Y - HOLE_SIZE) || user.getCenterY() > MAX_Y && user.getCenterX() > (MAX_X - HOLE_SIZE)) {
            currentDirection = -2;
            return;
        }

        double currentX = user.getCenterX();
        double currentY = user.getCenterY();
        if (x != null) {
            user.setCenterX(x.intValue() + xJump);
        }
        if (y != null) {
            user.setCenterY(y.intValue() + yJump);
        }
        for (Rectangle block : blocks) {
            Shape intersect = Shape.intersect(block, user);
            if (intersect.getBoundsInLocal().getWidth() != -1) {
                System.out.println("Collision Detected");
                hitPlayer.play(1.0);
                user.setCenterX(currentX);
                user.setCenterY(currentY);
                currentDirection = -1;
                break;
            }

        }

        for (Line border : borders) {
            Shape intersect = Shape.intersect(border, user);
            if (intersect.getBoundsInLocal().getWidth() != -1) {
                System.out.println("Hit a border");
                hitPlayer.play(1.0);
                user.setCenterX(currentX);
                user.setCenterY(currentY);
                currentDirection = -1;
                break;
            }
        }
    }

    private void addLabels(Pane pane) {
        movesLabel = new Label("Moves:" + moves);

        pane.getChildren().add(movesLabel);
    }

    private void addBlocks(Pane root) {
        blocks = new HashSet<>();
        Random rand = new Random();
        for (int i = 0; i < blockSlider.getValue(); i++) {
            Rectangle r = new Rectangle();
            //Circle c = new Circle();
            boolean added = false;
            int x = 0;
            int y = 0;
            while (!added) {
                x = rand.nextInt(MAX_X);
                y = rand.nextInt(MAX_Y);
                if (withinBounds(x, y)) {
                    added = true;
                }
            }
            r.setX(x);
            r.setY(y);
            r.setHeight(BLOCK_HEIGHT);
            r.setWidth(BLOCK_WIDTH);
            int colorPosition = rand.nextInt(4);
            r.setFill(BLOCK_COLORS[colorPosition]);
            // root.getChildren().add(c);
            // blocks.add(c);
            root.getChildren().add(r);
            blocks.add(r);
        }
    }

    private boolean withinBounds(int x, int y) {
        return !(x - BLOCK_WIDTH <= MIN_X || x + BLOCK_WIDTH >= MAX_X || y - BLOCK_HEIGHT <= MIN_Y || y + BLOCK_HEIGHT >= MAX_Y);
    }

    private void addUser(Pane root) {
        user = new Circle();
        user.setCenterX(USER_CENTER_X);
        user.setCenterY(USER_CENTER_Y);
        user.setRadius(USER_RADIUS);
        user.setFill(USER_COLOR);
        root.getChildren().add(user);
    }

    private void addBorder(Pane root) {
        borders = new HashSet<>();
        Line line1 = new Line(MIN_X, MIN_Y, MIN_X, MAX_Y);
        line1.setStroke(BORDER_COLOR);
        line1.setStrokeWidth(BORDER_STROKE_WIDTH);

        borders.add(line1);

        Line line2 = new Line(MIN_X, MIN_Y, MAX_X, MIN_Y);
        line2.setStroke(BORDER_COLOR);
        line2.setStrokeWidth(BORDER_STROKE_WIDTH);

        borders.add(line2);

        Line line3 = new Line(MAX_X, MIN_Y, MAX_X, MAX_Y - HOLE_SIZE);
        line3.setStroke(BORDER_COLOR);
        line3.setStrokeWidth(BORDER_STROKE_WIDTH);

        borders.add(line3);

        Line line4 = new Line(MIN_X, MAX_Y, MAX_X - HOLE_SIZE, MAX_Y);
        line4.setStroke(BORDER_COLOR);
        line4.setStrokeWidth(BORDER_STROKE_WIDTH);

        borders.add(line4);

        root.getChildren().addAll(line1, line2, line3, line4);

    }

    public static void main(String[] args) {
        launch(args);
    }

    private void addEvents(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent keyEvent) {
                if (null != keyEvent.getCode()) {
                    switch (keyEvent.getCode()) {
                        case DOWN:
                            updateUser(null, user.getCenterY(), 0, JUMP);
                            break;
                        case UP:
                            updateUser(null, user.getCenterY(), 0, -JUMP);
                            break;
                        case RIGHT:
                            updateUser(user.getCenterX(), null, JUMP, 0);
                            break;
                        case LEFT:
                            updateUser(user.getCenterX(), null, -JUMP, 0);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

}
