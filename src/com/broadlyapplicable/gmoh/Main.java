package com.broadlyapplicable.gmoh;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
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
    private static final Color BORDER_COLOR = Color.MEDIUMAQUAMARINE;
    private static final Color BORDER_SAFE_COLOR = Color.ROYALBLUE;
    private static final Color USER_COLOR = Color.DARKSEAGREEN;
    private static final Color[] BLOCK_COLORS = {Color.CRIMSON, Color.DARKMAGENTA, Color.DARKTURQUOISE, Color.DARKORANGE};
    private static final int USER_CENTER_X = 50;
    private static final int USER_CENTER_Y = 50;
    private static final int USER_RADIUS = 15;
    private static final int BLOCK_RADIUS = 15;
    private static final int JUMP = 15;
    private static final int SLEEP_MILLISECONDS = 50;
    private static final int HOLE_SIZE = 55;
    private Circle user;
    private int totalBlocks = 200;
    private Label movesLabel;
    private int moves = 0;
    private Pane gamePane;
    private Set<Point> blocks;
    private Set<Point> visited;
    private int mainMV;

    @Override
    public void start(Stage primaryStage) {

        initializeVariables();
        VBox vbox = new VBox();
        HBox buttonHbox = new HBox();
        buttonHbox.setPadding(new Insets(10, 0, 0, 10));
        gamePane = new Pane();
        vbox.getChildren().add(gamePane);
        updateGamePane();
        vbox.getChildren().add(buttonHbox);
        addButtons(buttonHbox);
        addLabels(buttonHbox);

        Scene scene = new Scene(vbox, APP_WIDTH, APP_HEIGHT);

        primaryStage.setTitle("Get Me Outta Here");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void initializeVariables() {
        blocks = new HashSet<>();
        visited = new HashSet<>();
    }

    private void updateGamePane() {
        addBorder(gamePane);
        addBlocks(gamePane);
        addUser(gamePane);
    }

    private void addButtons(Pane root) {
        Button goButton = new Button("Go");
        goButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                moveUser();
            }
        });

        goButton.setPadding(new Insets(5, 5, 5, 5));
        root.getChildren().add(goButton);

        Button startOverButton = new Button("Stop");
        startOverButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                gamePane.getChildren().clear();
                blocks.clear();
                updateGamePane();
            }
        });

        root.getChildren().add(startOverButton);
    }

    private void moveUser() {
        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int cntr = 0;

                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            int mv = getMV();

                            switch (mv) {
                                case 0:
                                    updateUser(null, user.getCenterY(), 0, -JUMP, mv);
                                    break;
                                case 1:
                                    updateUser(null, user.getCenterY(), 0, JUMP, mv);
                                    break;
                                case 2:
                                    updateUser(user.getCenterX(), null, -JUMP, 0, mv);
                                    break;
                                case 3:
                                    updateUser(user.getCenterX(), null, JUMP, 0, mv);
                                    break;

                                default:
                                    System.out.println("Invalid Direction");
                            }
                        }
                    });
                    cntr++;
                    Thread.sleep(SLEEP_MILLISECONDS);
                    if (user.getCenterX() + 5 >= MAX_X - HOLE_SIZE && user.getCenterY() + 5 >= MAX_Y - HOLE_SIZE) {
                        System.out.println("You found the exit: Moves=" + moves);
                        break;
                    }
                    moves++;
                    System.out.println(moves);
                }
                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

    }

    private int getMV() {
        if (mainMV == -1) {
            Random rand = new Random();
            return rand.nextInt(4);
        }
        return mainMV;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_MILLISECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean updateUser(Double x, Double y, int xJump, int yJump, int mv) {

        Double currentX = user.getCenterX();
        Double currentY = user.getCenterY();

        if (x != null) {
            Double newX = x + xJump;
            if (!blockBlock(newX, currentY) && newX > MIN_X && newX < MAX_X) {
                user.setCenterX(newX);
                Point p = new Point(newX.intValue(), currentY.intValue());
                visited.add(p);
                mainMV = mv;
            } else {
                mainMV = -1;
                return false;
            }
        }

        if (y != null) {
            Double newY = y + yJump;
            if (!blockBlock(currentX, newY) && newY > MIN_Y && newY < MAX_Y) {
                user.setCenterY(newY);
                Point p = new Point(currentX.intValue(), newY.intValue());
                visited.add(p);
                mainMV = mv;
            } else {
                mainMV = -1;
                return false;
            }
        }
        moves++;
        return true;
    }

    private boolean blockBlock(Double xPosition, Double yPosition) {

        for (Point point : blocks) {
            if (point.getX() == xPosition + USER_RADIUS && point.getY() == yPosition) {
                return true;
            }
        }

        return false;
    }

    private void addLabels(Pane pane) {
        movesLabel = new Label("Moves:" + moves);
        pane.getChildren().add(movesLabel);
    }

    private void updateLabels() {
        movesLabel.setText("Moves:" + moves);
    }

    private void addBlocks(Pane root) {
        Random rand = new Random();
        for (int i = 0; i < totalBlocks; i++) {

            Circle c = new Circle();

            boolean added = false;
            int x = 0;
            int y = 0;
            while (!added) {
                x = rand.nextInt(MAX_X);

                y = rand.nextInt(MAX_Y);

//                if (withinBounds(x, width, MIN_X, MAX_X) && withinBounds(y, height, MIN_Y, MAX_Y)) {
//                    added = true;
//                }
                if (withinBounds(x, y)) {
                    added = true;
                }
            }
            c.setCenterX(x);
            c.setCenterY(y);
            c.setRadius(BLOCK_RADIUS);
            updateBlockPosition(x, y);

            int colorPosition = rand.nextInt(4);
            c.setFill(BLOCK_COLORS[colorPosition]);
//c.setFill(Color.BLACK);            
//rec.setFill(Color.BLACK);
            root.getChildren().add(c);

        }
    }

//    private void addBlocks(Pane root) {
//        Random rand = new Random();
//        for (int i = 0; i < totalBlocks; i++) {
//            boolean added = false;
//            Rectangle rec = new Rectangle();
//            int x = 0;
//            int y = 0;
//            int width = 0;
//            int height = 0;
//            while (!added) {
//                x = rand.nextInt(MAX_X);
//                y = rand.nextInt(MAX_Y);
//
//                width = rand.nextInt(50);
//                height = rand.nextInt(50);
//                if (withinBounds(x, width, MIN_X, MAX_X) && withinBounds(y, height, MIN_Y, MAX_Y)) {
//                    added = true;
//                }
//            }
//
//            rec.setX(x);
//            rec.setY(y);
//            rec.setHeight(height);
//            rec.setWidth(width);
//
//            updateBlockPosition(x, height, y, width);
//
//            int colorPosition = rand.nextInt(4);
//            rec.setFill(BLOCK_COLORS[colorPosition]);
//          //rec.setFill(Color.BLACK);
//            root.getChildren().add(rec);
//
//        }
//    }
//    private void updateBlockPosition(int x, int width, int y, int height) {
//        for (int xCoord = x; xCoord <= width + x; xCoord++) {
//            for (int yCoord = y; yCoord <= height + y; yCoord++) {
//                Point p = new Point(x, y);
//                blocks.add(p);
//            }
//        }
//    }
    private void updateBlockPosition(int x, int y) {
        Point p = new Point(x, y);
        blocks.add(p);

    }

//    private boolean withinBounds(int start, int size, int min, int max) {
//        if (start < min) {
//            return false;
//        }
//        if ((start + size) >= max) {
//            return false;
//        }
//        if (start < 25) {
//            return false;
//        }
//
//        return true;
//    }
    private boolean withinBounds(int x, int y) {
        return !(x - BLOCK_RADIUS <= MIN_X || x + BLOCK_RADIUS >= MAX_X || y - BLOCK_RADIUS <= MIN_Y || y + BLOCK_RADIUS >= MAX_Y);
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

        Line line1 = new Line(MIN_X, MIN_Y, MIN_X, MAX_Y);
        line1.setStroke(BORDER_COLOR);
        line1.setStrokeWidth(BORDER_STROKE_WIDTH);

        Line line2 = new Line(MIN_X, MIN_Y, MAX_X, MIN_Y);
        line2.setStroke(BORDER_COLOR);
        line2.setStrokeWidth(BORDER_STROKE_WIDTH);

        Line line3 = new Line(MAX_X, MIN_Y, MAX_X, MAX_Y - HOLE_SIZE);
        line3.setStroke(BORDER_COLOR);
        line3.setStrokeWidth(BORDER_STROKE_WIDTH);

        Line line4 = new Line(MIN_X, MAX_Y, MAX_X - HOLE_SIZE, MAX_Y);
        line4.setStroke(BORDER_COLOR);
        line4.setStrokeWidth(BORDER_STROKE_WIDTH);

        root.getChildren().addAll(line1, line2, line3, line4);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
