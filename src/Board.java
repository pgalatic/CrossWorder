import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

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

    private static final char FREE_SPACE = 'o';
    private static final char NULL_SPACE = '.';

    private final int BOARD_SIZE;

    // STATE
    private Space[][] board;
    private HashSet<Space> pointsOfInterest;

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
                    case FREE_SPACE:
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
         * 'start' of a word in the crossword puzzle. */
        for (int row = 0; row < board.length; row++){
            for (int col = 0; col < board.length; col++) {
                if (row == 0 || col == 0) {
                    pointsOfInterest.add(board[row][col]);
                } else if (board[row - 1][col] == null || board[row][col - 1] == null) {
                    pointsOfInterest.add(board[row][col]);
                }
            }
        }

    }

    public void backtrack(){

        Scanner in = new Scanner(System.in);


    }



}
