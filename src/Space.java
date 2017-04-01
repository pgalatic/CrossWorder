import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Represents a space on the crossword grid.
 *
 * @author paul g
 */
public class Space {

    private final int row;
    private final int col;

    private Stack<Character> mem;
    private char curr;

    public Space(final int row, final int col){
        this.row = row;
        this.col = col;
        this.mem = new Stack<>();
        this.curr = ' ';
    }

    public void push(final char c){
        mem.push(c);
        curr = c;
    }

    public void pop(){
        try {
            curr = mem.pop();
        }catch (EmptyStackException e){
            curr = ' ';
        }
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    public int[] getCoords(){
        return new int[]{row, col};
    }

}
