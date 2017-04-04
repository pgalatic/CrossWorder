/**
 * Driver for CrossWorder.
 *
 * @author paul g
 */
public class Main {

    public static void main(String args[]){
        if (args.length > 0){
            System.err.println("Usage: java CrossWorder");
        }

        String filename = "resources/board2.txt";

        Board b = new Board(filename);
        b.init();
        b.backtrack();

        System.out.println("...Finished.");
    }
}
