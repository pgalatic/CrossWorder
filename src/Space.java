import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Represents a space on the crossword grid.
 *
 * @author paul g
 */
public class Space {

    private static final char BLANK = '_';
    private final char initial;

    private final int row;
    private final int col;

    private Stack<Character> mem;
    private char curr;

    public Space(final int row, final int col){
        this.row = row;
        this.col = col;
        this.mem = new Stack<>();
        this.curr = BLANK;
        this.initial = BLANK;
    }

    public Space(final int row, final int col, final char initial){
        this.row = row;
        this.col = col;
        this.mem = new Stack<>();
        this.curr = initial;
        this.initial = initial;
    }

    public void push(final char c){
        mem.push(c);
        curr = c;
    }

    public void pop(){
        curr = initial;
        if (!mem.isEmpty()){
            mem.pop();
            if (mem.isEmpty()){
                curr = initial;
            }else{
                curr = mem.peek();
            }
        }
    }

    public char getChar(){
        return curr;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return col;
    }

    @Override
    public String toString(){
        return String.format("%s - (%d, %d)", curr, row, col);
    }

}
