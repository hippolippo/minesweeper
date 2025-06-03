package com.hippolippo;

import java.util.List;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class App extends Application {

    private Button[][] buttons;
    private Board board;
    private Label statusLabel;
    private Label bombCount;
    private int flagCount;

    @Override
    public void start(Stage primaryStage) {
        int width = 40;
        int height = 20;
        int numMines = 175;
        flagCount = 0;
        board = new Board(width, height, numMines);
        GridPane grid = new GridPane();
        buttons = create_grid(grid, width, height);
        statusLabel = new Label("");
        bombCount = new Label(String.format("Bombs: %d", board.getMineCount()-flagCount));
        bombCount.getStyleClass().add("bombs");
        statusLabel.getStyleClass().add("status");
        StackPane center = new StackPane(grid, statusLabel);
        HBox statusBar = new HBox(bombCount);
        statusBar.setAlignment(Pos.CENTER);
        grid.setAlignment(Pos.CENTER);
        BorderPane root = new BorderPane(center);
        root.setTop(statusBar);
        Scene scene = new Scene(root, 40*width+40, 40*height+70);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Minesweeper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void click(int x, int y){
        Button button = buttons[x][y];
        if(button.getStyleClass().contains("flag") || button.getStyleClass().contains("mine")){
            return;
        }
        if(board.getGameState() != Board.GameState.PLAYING){
            return;
        }
        Board.Tile tile = board.getTile(x,y);
        if(tile.isRevealed()){
            List<Board.Tile> neighbors = board.getNeighbors(x, y);
            int flagged_neighbors = 0;
            for (Board.Tile neighbor : neighbors) {
                if(buttons[neighbor.getX()][neighbor.getY()].getStyleClass().contains("flag")) {
                    flagged_neighbors++;
                }
            }
            try {
                if(flagged_neighbors == tile.getNumber()){
                    for (Board.Tile neighbor : neighbors) {
                        if(!neighbor.isRevealed()){
                            click(neighbor.getX(), neighbor.getY());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                // Won't happen, inside isRevealed check
                e.printStackTrace();
            }
        }
        tile = board.move(x, y);
        button.getStyleClass().add("pressed");
        try {
            if(tile.isMine()){
                button.getStyleClass().add("mine");
            }else if(tile.getNumber() == 0){
                button.setText(" ");
                board.getNeighbors(x, y).forEach(neighbor -> {if(!neighbor.isRevealed()) click(neighbor.getX(), neighbor.getY());});
            }else{
                button.setText(String.valueOf(tile.getNumber()));
                button.setId(String.format("number-%d", tile.getNumber()));
            }
        } catch (IllegalAccessException e) {
            // This should never happen because board.move reveals the tile
            e.printStackTrace();
        }
        if(board.getGameState() == Board.GameState.LOST){
            for(Board.Tile mine: board.getMines()){
                buttons[mine.getX()][mine.getY()].getStyleClass().add("mine");
                buttons[mine.getX()][mine.getY()].getStyleClass().add("pressed");
            }
            statusLabel.setText("Game Over");
        }else if(board.getGameState() == Board.GameState.WON){
            statusLabel.setText("You Win!");
        }
    }

    private void flag(int x, int y){
        if(board.getGameState() != Board.GameState.PLAYING || board.getTile(x, y).isRevealed()){
            return;
        }
        Button button = buttons[x][y];
        if(button.getStyleClass().contains("flag")){
            button.getStyleClass().remove("flag");
            flagCount--;
        }else{
            button.getStyleClass().add("flag");
            flagCount++;
        }
        bombCount.setText(String.format("Bombs: %d", board.getMineCount()-flagCount));
    }

    private class ClickHandler implements EventHandler<MouseEvent>{
        private final int x;
        private final int y;

        public ClickHandler(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public void handle(MouseEvent event) {
            if(event.getButton().equals(MouseButton.PRIMARY))
                if(event.isControlDown()){
                    flag(x, y);
                }else{
                    click(x, y);
                }
            else if(event.getButton().equals(MouseButton.SECONDARY))
                flag(x, y);
        }
    }

    private Button[][] create_grid(GridPane grid, int width, int height){
        Button[][] buttons = new Button[width][height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Button button = new Button("");
                button.setOnMouseClicked(new ClickHandler(x, y));
                buttons[x][y] = button;
                grid.add(button, x, y);
            }
        }
        return buttons;
    }

    public static void main(String[] args) {
        launch();
    }
}
