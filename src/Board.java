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
    private static Scanner fileInput;
    private Space[][] board;
    private HashSet<String> totalWords;
    private HashSet<PointOfInterest> pointsOfInterest;
    private HashSet<PointOfInterest> poiIterator;
    private Stack<PointOfInterest> memoryStack;
    private static String filename;
    private boolean waitUser = true; // wait for user input or not
    private char[] line;
    private int skip = 0;

    public Board(String filename){

        this.filename = filename;
        File input;

        // reading from sample file
        input = new File(filename);
        try {
            fileInput = new Scanner(input);
        }catch (IOException e){
            e.printStackTrace();
        }

        // initialize important state
        line = fileInput.nextLine().toCharArray();
        BOARD_SIZE = line.length;
        board = new Space[BOARD_SIZE][BOARD_SIZE];
        totalWords = new HashSet<>();
        pointsOfInterest = new HashSet<>();
        poiIterator = new HashSet<>();
        memoryStack = new Stack<>();
    }

    private void buildBoard(){

        System.out.println("BOARD CONSTRUCTION YIELDS:");
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
                        board[row][col] = new Space(row, col, Character.toLowerCase(c));
                }
                System.out.print(c);
            }
            System.out.println();
            // advance at the end, since we clip one line at the start
            try {
                line = fileInput.nextLine().toCharArray();
            }catch (NoSuchElementException e){
                break;
            }
        }
        System.out.println("---------------------------");
    }

    /**
     * Utility code to determine whether or not to add a Space to the
     * pointsOfInterest array, which is a HashSet of words that mark the
     * 'start' of a word in the crossword puzzle.
     *
     * PointOfInterest objects are added with both the ACROSS and DOWN
     * parameters, to make backtracking easier. */
    private void buildPointsOfInterest() {
        Space currSpace;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                currSpace = board[row][col];
                if (currSpace == null) {
                    continue;
                }
                if (row == 0 && col == 0) {
                    pointsOfInterest.add(new PointOfInterest(currSpace, Direction.ACROSS));
                    pointsOfInterest.add(new PointOfInterest(currSpace, Direction.DOWN));
                } else if (row > 0 && col == 0) {
                    pointsOfInterest.add(new PointOfInterest(currSpace, Direction.ACROSS));
                } else if (row == 0 && col > 0) {
                    pointsOfInterest.add(new PointOfInterest(currSpace, Direction.DOWN));
                } else {
                    if (board[row][col - 1] == null) {
                        pointsOfInterest.add(new PointOfInterest(currSpace, Direction.ACROSS));
                    } else if (board[row - 1][col] == null) {
                        pointsOfInterest.add(new PointOfInterest(currSpace, Direction.DOWN));
                    }
                }
            }
        }
    }



    /** Initializes the board. */
    public void init(){
        buildBoard();
        buildPointsOfInterest();
    }

    /**
	 * Backtracker: Finds the first valid goal configuration, then returns.
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
        if (isGoal()){
            return this;
        }

        // Find which place we want to insert into.
        PointOfInterest poi = getMinimumRemaining();
		if (poi == null){ return null; }

		// From there, get the next values.
		ArrayList<String> nextVals = poi.getPossibleValues();

        while (!nextVals.isEmpty()){
            String nextVal = promptUser(poi, nextVals);
            insertValue(poi, nextVal);
            memoryStack.push(poi);
            if (!(backtrack() == null)){ return this; }
			//System.out.println("FAILED... BACKTRACKING.");
            System.out.println(boardToString());
            nextVals.remove(nextVal);
            poi = memoryStack.pop();
            rollback(poi);
        }

        return null;

    }

	/**
	 * Finds and returns the Point of Interest with the fewest number of 
	 * remaining possibilities. It iterates through all the PointsOfInterest,
	 * builds regexes based on their contents, then attaches an updated list
	 * to a PointOfInterest and returns it.
	 */
	private PointOfInterest getMinimumRemaining(){
		ArrayList<String> currVals;
		PointOfInterest rtn = null;
		String regex;
		int currMax = Integer.MAX_VALUE;
		int currSize;
		for (PointOfInterest poi : pointsOfInterest){
			regex = buildRegex(poi);
			currVals = wordFinder.findMatches(regex);
			currSize = currVals.size();
			if (currSize > 1 && currSize < currMax){ // includes current term
				rtn = poi;
				rtn.setPossibleValues(currVals);
			}
            if (currSize == 2){ break; } // can't get lower than this
		}
		return rtn;
	}
    /**
     * Builds a regular expression representing all words whose start position
     * and direction match the position of interest, taking into account the
     * current state of the board.
     */

    private String buildRegex(PointOfInterest poi){
        StringBuilder regex = new StringBuilder();
        regex.append("\\b"); // so that we can match at the start of any line
        switch (poi.d){
            case ACROSS:
                int row = poi.s.getRow();
                for (int col = poi.s.getCol(); col < BOARD_SIZE && board[row][col] != null; col++){
                    char curr = board[row][col].getChar();
                    if (curr != BLANK){
                        regex.append(curr);
                    }else{
                        regex.append(NULL_SPACE);
                    }
                }
                break;
            case DOWN:
                int column = poi.s.getCol();
                for (int r = poi.s.getRow(); r < BOARD_SIZE && board[r][column] != null; r++){
                    char curr = board[r][column].getChar();
                    if (curr != BLANK){
                        regex.append(curr);
                    }else{
                        regex.append(NULL_SPACE);
                    }
                }
                break;
        }
        regex.append("\\b"); // so that we don't match both "car" and "cargo"
        return regex.toString();
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
        Space s;
        char[] word = nextVal.toCharArray();
        int row = poi.s.getRow();
        int col = poi.s.getCol();
        switch (poi.d){
            case ACROSS:
                for (int i = 0; i + col < BOARD_SIZE && i < word.length; i++){
                    s = board[row][col + i];
                    if (s == null){ break; }
                    s.push(word[i]);
                }
                break;
            case DOWN:
                for (int i = 0; i + row < BOARD_SIZE && i < word.length; i++){
                    s = board[row + i][col];
                    if (s == null){ break; }
                    s.push(word[i]);
                }
                break;
        }
        totalWords.add(nextVal);
    }

    /**
     * Peels a letter off each of the spaces associated with a point of
     * interest.
     *
     * @param poi: the point of interest to be rolled back
     */
    private void rollback(PointOfInterest poi){
        int row = poi.s.getRow();
        int col = poi.s.getCol();
        Space s;
        switch (poi.d){
            case ACROSS:
                for (int i = 0; i + col < BOARD_SIZE; i++){
                    s = board[row][col + i];
                    if (s == null){ break; }
                    s.pop();
                }
                break;
            case DOWN:
                for (int i = 0; i + row < BOARD_SIZE; i++){
                    s = board[row + i][col];
                    if (s == null){ break; }
                    s.pop();
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
        
		System.out.println(boardToString());
		String[] command;
        boolean commandRecognized = false;

        System.out.println(String.format("INSERT: (%d, %d)", poi.s.getRow(), poi.s.getCol()));

        int listSize = nextVals.size();
        if (listSize == 1){ return nextVals.get(0); }
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

        System.out.println("Options:");
        System.out.println("\t1) Enter the number of the word you'd like to choose");
        System.out.println("\t2) Enter \"r\" to choose a random word from the total list");
        System.out.println("\t3) Enter \"step n\" to step n times");
        System.out.println("\t4) Enter \"-1\" to turn off prompting");

        while (!commandRecognized) {
            System.out.print("\n> ");
            command = userInput.next().split(" ");
            if (command.length == 1){
                if (command[0].equals("r")){
                    return nextVals.remove(rand.nextInt(nextVals.size()));
                }else {
                    char[] curr = command[0].toCharArray();
                    char c;
                    if (curr.length > 1){
                        if (curr[0] == '-'){
                            waitUser = false;
                            return nextVals.remove(rand.nextInt(nextVals.size()));
                        }
                        System.out.println("Command not recognized.");
                        continue;
                    }else{
                        c = curr[0];
                    }
                    if (Character.isDigit(c)){
                        int i = Character.getNumericValue(c);
                        switch (i){
                            default:
                                if (i < listSize) {
                                    return nextVals.remove(i);
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
     *  Public only for testing purposes.
     *
     *  @return if the goal has been met
     */
    public boolean isGoal(){
        ArrayList<String> results;
        String currWord = "";
        int row, col;
        char c;
        for (PointOfInterest poi : pointsOfInterest){
            row = poi.s.getRow();
            col = poi.s.getCol();
            switch (poi.d){
                case ACROSS:
                    while (col < BOARD_SIZE && board[row][col] != null) {
                        c = board[row][col].getChar();
                        if (c == BLANK){ return false; }
                        currWord += c;
                        col++;
                    }
                    break;
                case DOWN:
                    while (row < BOARD_SIZE && board[row][col] != null){
                        c = board[row][col].getChar();
                        if (c == BLANK){ return false; }
                        currWord += c;
                        row++;
                    }
            }
            if (    !totalWords.contains(currWord) &&
                    wordFinder.findMatches("\\b" + currWord + "\\b").isEmpty())
            { return false; }
            currWord = "";
        }

        return true;
    }

   	public String boardToString(){
        Space s;
        String rtn = "";
        for (int row = 0; row < BOARD_SIZE; row++){
            for (int col = 0; col < BOARD_SIZE; col++){
                s = board[row][col];
                if (s == null){
                    rtn += NULL_SPACE;
                }else {
                    rtn += board[row][col].getChar();
                }
            }
            rtn += "\n";
        }
        return rtn;
    }

    /** STRICTLY FOR DEBUGGING. */
    public void setRow(int row, Space[] data){
        board[row] = data;
    }

    /** STRICTLY FOR DEBUGGING. */
    public void addPOI(Space s, Direction d){
        PointOfInterest poi = new PointOfInterest(s, d);
        pointsOfInterest.add(poi);
        poiIterator.add(poi);
    }

    /** STRICTLY FOR DEBUGGING. */
    public void addWord(String word){
        totalWords.add(word);
    }

    /** Enum for holding direction. */
    public enum Direction{ ACROSS, DOWN }

    /**
     * Simple class for keeping track of Spaces where words begin, as those
     * spaces will be double-counted. */
    private class PointOfInterest{
        Space s;
        Direction d;
		ArrayList<String> possibleValues;

        PointOfInterest(Space s, final Direction d){
            this.s = s;
            this.d = d;
        }

		public void setPossibleValues(ArrayList<String> vals){
			possibleValues = vals;
		}

		public ArrayList<String> getPossibleValues(){
			return possibleValues;
		}

        @Override
        public String toString(){
            return s.toString() + " : " + d;
        }

    }


}
