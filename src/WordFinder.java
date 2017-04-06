import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code utilizing methods found on www.java-tips.org to apply regular
 * expressions to a file.
 *
 * @author java-tips.org
 * @author paul g
 */
public class WordFinder {

    private static final String FILE = "../resources/ospd.txt";
    private static final int NUM_RETURN_VALUES = 100;

    CharSequence fileContents;

    /** Creates fileContents using the default FILE. */
    public WordFinder(){
        try{
            this.fileContents = toCharSequence(FILE);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /** Creates fileContents using a provided String. */
    public WordFinder(String fileContents){
        try{
            this.fileContents = toCharSequence(fileContents);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Finds and returns all the matches of regex to fileContents.
     */
    public ArrayList<String> findMatches(String regex){

        ArrayList<String> matches = new ArrayList<>();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(fileContents);

        while (m.find()){
            matches.add(m.group());
        }

        return matches;

    }

    /**
     * Converts the contents of a file into a CharSequence.
     */
    private CharSequence toCharSequence(String filename) throws IOException {
        FileInputStream input = new FileInputStream(filename);
        FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
        return Charset.forName("8859_1").newDecoder().decode(bbuf);
    }


}
