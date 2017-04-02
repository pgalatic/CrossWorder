import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents the board of the crossword grid.
 *
 * Currently only supports square boards.
 *
 * @author paul g
 */
public class Board {

    // CONSTANTS
    private static final Random rand = new Random();
    private static final Scanner userInput = new Scanner(System.in);
    private static final WordFinder wordFinder = new WordFinder();

    private static final char BLANK = '_';
    private static final char NULL_SPACE = '.'; // both for out of bounds and
                                                // building the regex

    private static final int MAX_OPTIONS_LENGTH = 10;
    private final int BOARD_SIZE;

    // STATE
    private Space[][] board;
    private HashSet<PointOfInterest> pointsOfInterest;
    private Stack<PointOfInterest> memoryStack;
    private boolean goalFound;
    private boolean waitUser; // wait for user input or not
    private int skip = 0;
    private int poiIndex = 0; // current poi

    public Board() throws IOException, Exception{

        Scanner in;
        File input;
        char[] line;

        // reading from sample file
        String filename = "resources/board1.txt";
        input = new File(filename);
        in = new Scanner(input);

        // initialize important state
        line = in.nextLine().toCharArray();
        BOARD_SIZE = line.length;
        board = new Space[BOARD_SIZE][BOARD_SIZE];
        pointsOfInterest = new HashSet<>();

        System.out.println("BOARD CONSTRUCTION YIELDS:\n");
        System.out.println("---------------------------");
        // construct the initial board
        for (int row = 0; row < BOARD_SIZE; row++){
            char c;
            for (int col = 0; col < BOARD_SIZE; col++){
                c = line[col];
                switch (c){
                    case BLANK:
                        board[row][col] = new Space(row, col);
                        break;
                    case NULL_SPACE:
                        board[row][col] = null;
                        break;
                    default:
                        throw new Exception("Invalid data in file. Cannot construct board."){};
                }
                System.out.print(c);
            }
            System.out.println();
            // advance at the end, since we clip one line at the start
            try {
                line = in.nextLine().toCharArray();
            }catch (NoSuchElementException e){
                break;
            }
        }
        System.out.println("---------------------------");

        /**
         * Utility code to determine whether or not to add a Space to the
         * pointsOfInterest array, which is a HashSet of words that mark the
         * 'start' of a word in the crossword puzzle.
         *
         * PointOfInterest objects are added with both the ACROSS and DOWN
         * parameters, to make backtracking easier. */
        for (int row = 0; row < board.length; row++){
            for (int col = 0; col < board.length; col++) {
                if (row == 0 || col == 0) {
                    pointsOfInterest.add(new PointOfInterest(board[row][col], Direction.ACROSS));
                    pointsOfInterest.add(new PointOfInterest(board[row][col], Direction.DOWN));
                } else if (board[row - 1][col] == null || board[row][col - 1] == null) {
                    pointsOfInterest.add(new PointOfInterest(board[row][col], Direction.ACROSS));
                    pointsOfInterest.add(new PointOfInterest(board[row][col], Direction.DOWN));
                }
            }
        }

    }

    /**
     * Backtracking algorithm:
     *  0) If all the points of interest are filled, check if the goal has been
     *      found. If it has, quit.
     *  1) Choose a random point of interest of those remaining in the list.
     *  2) Based on the direction of the point of interest, build a regular
     *      expression.
     *  3) Use the regular expression to find the next terms.
     *  4) While there are next terms, choose one, push it onto all the Spaces,
     *      push data to the MemoryStack, and recurse.
     *  5) After all the next terms have been exhausted, pop from the stack and
     *      return.
     */
    public Board backtrack(){
        System.out.println("CURRENT BOARD CONFIGURATION: ");
        System.out.println("-------------");
        System.out.println(boardToString());
        System.out.println("-------------");

        if (isGoal()){
            return this;
        }

        // This line of code takes the points of interest set, converts it to
        // an array, and then puts a random element of that array into curr.
        PointOfInterest poi = (PointOfInterest) pointsOfInterest.toArray()
                [rand.nextInt(pointsOfInterest.size())];
        while (poi.getChar() != BLANK) {
            poi = (PointOfInterest) pointsOfInterest.toArray()
                    [rand.nextInt(pointsOfInterest.size())];
        }

        // Now we build the regular expression.
        StringBuilder regex = new StringBuilder();
        switch (poi.d){
            case ACROSS:
                int r = poi.getRow();
                for (int col = poi.getCol(); col < BOARD_SIZE; col++){
                    char curr = board[r][col].getChar();
                    if (curr != BLANK){
                        regex.append(curr);
                    }else{
                        regex.append(NULL_SPACE);
                    }
                }
                break;
            case DOWN:
                int column = poi.getCol();
                for (int row = poi.getRow(); row < BOARD_SIZE; row++){
                    char curr = board[row][column].getChar();
                    if (curr != BLANK){
                        regex.append(curr);
                    }else{
                        regex.append(NULL_SPACE);
                    }
                }
                break;
        }
        regex.append("\\b"); // so that we don't match both "car" and "cargo"

        // Using that regular expression, find words that we can insert.
        ArrayList<String> nextVals = wordFinder.findMatches(regex.toString());

        while (!nextVals.isEmpty()){
            String nextVal = promptUser(poi, nextVals);
            insertValue(poi, nextVal);
            memoryStack.push(poi);
            backtrack();
            poi = memoryStack.pop();
            rollback(poi);
        }

        return null;

    }

    /**
     * Pastes a word onto the crossword grid. Visualize the grid as a sheet of
     * paper, and letters as removable stickers. Each Space has a stack
     * associated with it so that, if the backtracker reaches a dead end,
     * letters can be "peeled off" without much hassle.
     *
     * @param poi: the point of interest, where the word begins
     * @param nextVal: the word to be inserted
     */
    private void insertValue(PointOfInterest poi, String nextVal){
        char[] word = nextVal.toCharArray();
        int row = poi.getRow();
        int col = poi.getCol();
        switch (poi.d){
            case ACROSS:
                for (int i = 0; i + col < BOARD_SIZE; i++){
                    board[row][col + i].push(word[i]);
                }
                break;
            case DOWN:
                for (int i = 0; i + row < BOARD_SIZE; i++){
                    board[row + i][col].push(word[i]);
                }
                break;
        }
    }

    /**
     * Peels a letter off each of the spaces associated with a point of
     * interest.
     *
     * @param poi: the point of interest to be rolled back
     */
    private void rollback(PointOfInterest poi){
        int row = poi.getRow();
        int col = poi.getCol();
        switch (poi.d){
            case ACROSS:
                for (int i = 0; i + col < BOARD_SIZE; i++){
                    board[row][col + i].pop();
                }
                break;
            case DOWN:
                for (int i = 0; i + row < BOARD_SIZE; i++){
                    board[row + i][col].pop();
                }
                break;
        }
    }

    /**
     * Prints the board and presents to the user the next space to insert to,
     * requesting them to choose an option. The user can choose a specific
     * word, */
    private String promptUser(PointOfInterest poi, ArrayList<String> nextVals){
        // If we're not waiting on the user, pop a random term
        if (skip > 0){ skip--; return nextVals.remove(rand.nextInt(nextVals.size())); }
        if (!waitUser){ return nextVals.remove(rand.nextInt(nextVals.size())); }
        String[] command;
        boolean commandRecognized = false;

        System.out.println(String.format("INSERT: (%d, %d)", poi.getRow(), poi.getCol()));

        int listSize = nextVals.size();
        System.out.println(String.format("%d AVAILABLE OPTIONS", listSize));
        if (listSize > 10){
            System.out.println(String.format("PRINTING RANDOM %d...", MAX_OPTIONS_LENGTH));
            Collections.shuffle(nextVals);
            for (int i = 0; i < MAX_OPTIONS_LENGTH; i++){
                System.out.println(String.format("[%d] : %s", i, nextVals.get(i)));
            }
        }else{
            int i = 0;
            for (String s : nextVals){
                System.out.println(String.format("[%d] : %s", i, nextVals.get(i)));
                i++;
            }
        }

        System.out.println(boardToString());
        System.out.println("Options:");
        System.out.println("\t1) Enter the number of the word you'd like to choose");
        System.out.println("\t2) Enter \"r\" to choose a random word from the total list");
        System.out.println("\t3) Enter \"step n\" to step n times");
        System.out.println("\t4) Enter \"-1\" to turn off prompting");

        while (!commandRecognized) {
            System.out.println("\n>");
            command = userInput.next().split(" ");
            if (command.length == 1){
                if (command[0].equals("r")){
                    return nextVals.remove(rand.nextInt(nextVals.size()));
                }else {
                    char c = command[0].toCharArray()[0];
                    if (Character.isDigit(c)){
                        int i = Character.getNumericValue(c);
                        switch (i){
                            case -1:
                                waitUser = false;
                                return nextVals.remove(rand.nextInt(nextVals.size()));
                            default:
                                if (i < listSize){
                                    return nextVals.remove(i);
                                }else{
                                    System.out.println("Command not recognized.");
                                }
                        }
                    }
                }
            }else if (command.length == 2){
                if (!command[0].equals("step")){
                    System.out.println("Command not recognized.");
                }else{
                    try {
                        int i = Integer.parseInt(command[1]);
                        if (i < 1){
                            System.out.printf("Must skip at least once.");
                        }
                        skip = i;
                        return nextVals.remove(rand.nextInt(nextVals.size()));
                    }catch (NumberFormatException e){
                        System.out.println("Command not recognized.");
                    }
                }
            }else{
                System.out.println("Command not recognized.");
            }
        }

        // All else fails, return something random
        System.err.println("WARNING: Could not get input from user");
        return nextVals.remove(rand.nextInt(nextVals.size()));
    }

    /**
     * Evaluates whether or the board meets the following conditions:
     *  1) Every space is filled
     *  2) Every word is present in the WordList
     *
     *  @return if the goal has been met
     */
    private boolean isGoal(){
        ArrayList<String> results;
        String currWord = "";
        int row, col;
        char c;
        for (PointOfInterest poi : pointsOfInterest){
            row = poi.getRow();
            col = poi.getCol();
            switch (poi.d){
                case ACROSS:
                    while (col < BOARD_SIZE) {
                        c = board[row][col].getChar();
                        if (c == BLANK){ return false; }
                        currWord += c;
                        col++;
                    }
                    break;
                case DOWN:
                    while (row < BOARD_SIZE){
                        c = board[row][col].getChar();
                        if (c == BLANK){ return false; }
                        currWord += c;
                        row++;
                    }
            }
            if (currWord.isEmpty()){
                throw new InputMismatchException("Check for an error in isGoal()");
            }
            currWord += "\\b";
            results = wordFinder.findMatches(currWord);
            if (results.isEmpty()){ return false; }
        }

        return true;
    }

    private String boardToString(){
        String rtn = "";
        for (int row = 0; row < BOARD_SIZE; row++){
            for (int col = 0; col < BOARD_SIZE; col++){
                rtn += board[row][col].getChar();
            }
            rtn += "\n";
        }
        return rtn;
    }

    /** Enum for holding direction. */
    private enum Direction{ ACROSS, DOWN }

    /**
     * Simple class for keeping track of Spaces where words begin, as those
     * spaces will be double-counted. */
    private class PointOfInterest extends Space{
        Space s;
        Direction d;

        PointOfInterest(Space s, final Direction d){
            super(s.getRow(), s.getCol());
            this.s = s;
            this.d = d;
        }

    }


}
