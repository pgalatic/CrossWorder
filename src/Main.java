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

        String filename = "../resources/board0.txt";

        Board b = new Board(filename);
		System.out.println("Initializing board...");
        b.init();
		System.out.println("Starting to backtrack...");
        b.backtrack();

		System.out.println("RESULT:");
		System.out.println(b.boardToString());
        System.out.println("...Finished.");
    }
}
