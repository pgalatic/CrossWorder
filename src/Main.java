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

        try {
            Board b = new Board();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
