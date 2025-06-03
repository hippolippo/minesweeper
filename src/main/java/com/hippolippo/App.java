package com.hippolippo;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class App extends Application {

    private Button[][] buttons;
    private Board board;

    @Override
    public void start(Stage primaryStage) {
        board = new Board(15, 15, 35);
        GridPane grid = new GridPane();
        buttons = create_grid(grid, 15, 15);
        StackPane root = new StackPane(grid);
        Scene scene = new Scene(root, 600, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Minesweepr");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void click(int x, int y){
        Button button = buttons[x][y];
        if(button.getStyleClass().contains("flag")){
            return;
        }
        Board.Tile tile = board.move(x, y);
        button.getStyleClass().add("pressed");
        try {
            if(tile.isMine()){
                button.setText("*");
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
    }

    private void flag(int x, int y){
        Button button = buttons[x][y];
        if(button.getStyleClass().contains("flag")){
            button.getStyleClass().remove("flag");
        }else{
            button.getStyleClass().add("flag");
        }
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
                click(x, y);
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
