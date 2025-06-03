package com.hippolippo;

import java.util.LinkedList;

public class Board{

    public class Tile{
        private boolean revealed;
        private boolean mine;
        private int x;
        private int y;
        private Board board;
        private int number;

        public Tile(int x, int y, Board board){
            this.x = x;
            this.y = y;
            this.board = board;
            this.revealed = false;
            this.mine = false;
            this.number = 0;
        }

        private void makeMine(){
            this.mine = true;
            for(Tile neighbor: board.getNeighbors(x,y)){
                neighbor.number++;
            }
        }

        private void reveal(){
            this.revealed = true;
        }

        private boolean _isMine(){
            return this.mine;
        }

        public boolean isMine() throws IllegalAccessException{
            if(this.revealed){
                return this._isMine();
            }
            else{
                throw new IllegalAccessException("Tile is not revealed");
            }
        }

        private int _getNumber(){
            return this.number;
        }

        public int getNumber() throws IllegalAccessException{
            if(this.revealed){
                return this._getNumber();
            }
            else{
                throw new IllegalAccessException("Tile is not revealed");
            }
        }

        public boolean isRevealed(){
            return this.revealed;
        }

        public int getX(){
            return this.x;
        }

        public int getY(){
            return this.y;
        }

    }
    public enum GameState{
        PLAYING,
        WON,
        LOST
    }

    private Tile[][] board;
    private int width;
    private int height;
    private int numMines;
    private int revealed = 0;
    private boolean revealedBomb = false;

    public Board(int width, int height, int numMines){
        this.width = width;
        this.height = height;
        this.numMines = numMines;
    }

    public Tile move(int x, int y){
        if(board == null){
            initialize_board(x, y);
        }
        Tile tile = board[x][y];
        if(tile.isRevealed()){
            return tile;
        }
        tile.reveal();
        revealed++;
        return tile;
    }

    public Tile getTile(int x, int y){
        return board[x][y];
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public int getMineCount(){
        return numMines;
    }

    public GameState getGameState(){
        if(revealedBomb){
            return GameState.LOST;
        }
        if(revealed == width * height - numMines){
            return GameState.WON;
        }
        return GameState.PLAYING;
    }

    private boolean within(int a, int b, int range){
        return a >= b - range && a <= b + range;
    }

    private void initialize_board(int excludeX, int excludeY){
        // Create the board array and fill it with Tile objects
        this.board = new Tile[width][height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                board[x][y] = new Tile(x,y,this);
            }
        }
        // Select tiles to make into mines
        for(int i = 0; i < numMines; i++){
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);
            // Don't turn it into a mine if it is already a mine or if it's within the "exclusion zone" of within 1 tile of the first move
            if(board[x][y]._isMine() || (within(x, excludeX, 1) && within(y, excludeY, 1))){
                i--;
            }
            else{
                board[x][y].makeMine();
            }
        }
    }

    public LinkedList<Tile> getNeighbors(int x, int y){
        LinkedList<Tile> neighbors = new LinkedList<Tile>();
        // Add each neighbor to the LinkedList of neighbors
        for(int ix = Math.max(0, x-1); ix < Math.min(width, x+2); ix++){
            for(int iy = Math.max(0, y-1); iy < Math.min(height, y+2); iy++){
                if(ix != x || iy != y){
                    neighbors.add(board[ix][iy]);
                }
            }
        }
        return neighbors;
    }

    public static void main(String[] args){
        // Create a Scanner object to read user input
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        // Ask the user for the difficulty level
        System.out.println("Choose difficulty: beginner, intermediate, or advanced");
        String difficulty = scanner.nextLine().toLowerCase();

        int width, height, numMines;

        // Set board dimensions and mine count based on difficulty
        switch (difficulty) {
            case "beginner":
                width = 9;
                height = 9;
                numMines = 20;
                break;
            case "intermediate":
                width = 16;
                height = 16;
                numMines = 40;
                break;
            case "advanced":
                width = 24;
                height = 24;
                numMines = 99;
                break;
            default:
                System.out.println("Invalid difficulty. Using beginner.");
                width = 9;
                height = 9;
                numMines = 10;
                break;
        }

        // Create a new Board object
        Board gameBoard = new Board(width, height, numMines);

        // Game loop
        while (gameBoard.getGameState() == GameState.PLAYING) {
            // Print the current state of the board (optional, for debugging/visualization)
            // You would need to add a method to print the board state in the Board class

            // Ask the user for their move
            System.out.println("Enter your move (x y):");
            int x = scanner.nextInt();
            int y = scanner.nextInt();

            // Make the move
            Tile revealedTile = gameBoard.move(x, y);

            // Check the revealed tile and provide feedback
            if (revealedTile != null) {
                try {
                    if (revealedTile.isMine()) {
                        System.out.println("You hit a mine! Game Over.");
                        gameBoard.revealedBomb = true; // Set the flag for game over
                    } else {
                        int number = revealedTile.getNumber();
                        if (number == 0) {
                            System.out.println("Revealed a blank tile.");
                            // Implement logic to reveal adjacent blank tiles if needed
                        } else {
                            System.out.println("Revealed a tile with " + number + " mines nearby.");
                        }
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Error accessing tile information: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid move or tile already revealed.");
            }
        }

        // Game over message
        if (gameBoard.getGameState() == GameState.WON) {
            System.out.println("Congratulations! You won!");
        } else if (gameBoard.getGameState() == GameState.LOST) {
            System.out.println("Game Over. You lost.");
        }

        // Close the scanner
        scanner.close();

    }
}