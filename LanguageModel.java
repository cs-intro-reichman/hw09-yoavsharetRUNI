import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        // Reads just enough characters to form the first window
        for(int i = 0; i < this.windowLength; i++){
            window += in.readChar();
        }
        List probs;
        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();
            // Checks if the window is already in the map
            if(this.CharDataMap.get(window) != null){
                probs = this.CharDataMap.get(window);
            } else {
                 // The window was not found in the map
                // Creates a new empty list, and adds (window,list) to the map
                probs = new List();
                this.CharDataMap.put(window, probs);
            }

             // Calculates the counts of the current character.
            probs.update(c);
            
            // Advances the window: adds c to the window’s end, and deletes the
            // window's first character.
            window = window.substring(1) + c;
        }
        // The entire file has been processed, and all the characters have been counted.
        // Proceeds to compute and set the p and cp fields of all the CharData objects
        // in each linked list in the map.
        for (List l : this.CharDataMap.values()){
            this.calculateProbabilities(l);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		// calc total num of chars
        int numOfChars = 0;
        ListIterator it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData curr = it.next();
            numOfChars += curr.count;
        }
        
        // populate p field
        it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData curr = it.next();
            curr.p = curr.count/(double)numOfChars;
        }
        
        // populate cp field
        CharData prev = probs.get(0);
        prev.cp = prev.p;
        it = probs.listIterator(1);
        while (it.hasNext()) {
            CharData curr = it.next();
            curr.cp = prev.cp + curr.p;
            prev = curr;
        }
        // CharData[] probsArray = probs.toArray();
        // CharData prev = probsArray[probsArray.length - 1];
        // prev.cp = prev.p;
        // for(int i = (probsArray.length -2); i>=0; i--){
        //     // the Char are inserted in reverse order
        //     CharData curr = probsArray[i];
        //     curr.cp = prev.cp + curr.p;
        //     prev = curr;
        // }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		double r = this.randomGenerator.nextDouble();
        ListIterator it = probs.listIterator(0);
        while (it.hasNext()) {
            CharData curr = it.next();
            if(curr.cp >= r){
                return curr.chr;
            }
        }
        return 'X'; // compilation requirement
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if(initialText.length() < this.windowLength){
            return initialText;
        }
        StringBuilder output = new StringBuilder(initialText);
        String window = output.substring(output.length() - this.windowLength);
        for (int i = 0; i < textLength; i++) {
            List probs = this.CharDataMap.get(window);
            if(probs == null){
                return output.toString();
            }
            char c = this.getRandomChar(probs);
            output.append(c);
            window = window.substring(1) + c;
        }
        return output.toString();
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        // LanguageModel languageModel = new LanguageModel(7,20);
        // languageModel.train("originofspecies.txt");
        // String generatedText = languageModel.generate("Natural", 172);
        // String expectedGeneratedText = "Natural selection, how is it possible, generally much changed\n"+
        // "simultaneous rotation, when the importance of Batrachians, 393.\n"+
        // "  Batrachians (frogs, toads, newts) have to modified ";
        // System.err.println(generatedText);

        // boolean res = stringEqualsNoSpaces(generatedText, expectedGeneratedText);
        // if (!res){
        //     System.out.println("Expected: " + expectedGeneratedText);
        //     System.out.println("Actual: " + generatedText);
        //     System.out.println("FAIL with windowLength = 7, seed = 20, initialText = Natural, textLength = 172");
        // }
        // return res;


        // MAIN //
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration){
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);   
        }
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
