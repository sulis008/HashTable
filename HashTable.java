// Written by Alexander Sulistyo (sulis008)
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class HashTable<T>{
    NGen<T>[] hashTable;
    String type;
    public HashTable(int size, String type) {
        hashTable = new NGen[size];
        this.type = type;
    }
    // My idea for this first hash function was to assign indexes based on the first character
    // of the item.toString(). It worked, but certainly not well. All it did was organize the
    // tokens alphabetically, which at first, I did not expect.
    public int hash1(T item) {
        return item.toString().charAt(0) % hashTable.length;
    }
    // For my second attempt, I wanted to use more than just the first letter to make it a bit
    // more personalized to the given token. I thought two and three letter implementations
    // would allow the tokens to be split up evenly, but I still, was wrong. The performance of this hash
    // function is a bit better than the first, but not my much.
    public int hash2(T item) {
        String token = item.toString();
        if (token.length() >= 3) {
            return (token.charAt(0)+token.charAt(token.length()/2)+token.charAt(token.length()-1)) % hashTable.length;
        } else if (token.length() == 2) {
            return (token.charAt(0)+token.charAt(1)) % hashTable.length;
        } else return token.charAt(0) % hashTable.length;
    }
    // For the third version of my hash function, I figured I could simply find a way to use
    // each character within each token, giving different words different hash codes. This
    // worked a lot better than the previous two versions, but it still wasn't that great.
    // For example, the words 'stop' and 'post' would give the same hash code because they
    // contain the exact same letters. Fighting for something better.
    public int hash3(T item) {
        int hash = 0;
        for (int i = 0; i < item.toString().length(); i++) {
            hash += item.toString().charAt(i);
        }
        return hash % hashTable.length;
    }
    // finalHash is the hash function that I decided to go with for general AND specific data types.
    // It took a lot of experimenting, but I think I found a sweet spot with the prime numbers 7 and 11.
    // Each of the provided .txt files work well with this hash function (the longest chain < 7).
    // This function is different from and better than my previous versions because it also includes prime numbers
    // within the calculation, giving it a better chance of being random/different from previous tokens
    // that have been added. Finally, something I can be decently proud of.
    public int finalHash(T item) {
        int hash = 7;
        for (int i = 0; i < item.toString().length(); i++) {
            hash = hash * 11 + item.toString().charAt(i);
        }
        return Math.abs(hash % hashTable.length);
    }
    public void add(T item) {
        // Hash item
        // Both the general and specific data types work well with my finalHash method,
        // so both cases will be implemented as such. Shown below:
        int index = 0;
        if (type.equalsIgnoreCase("general")) {
            index = finalHash(item); // finalHash
        } else if (type.equalsIgnoreCase("specific")) {
            index = finalHash(item); // finalHash
        } else {
            System.out.println("Invalid hashing type. Please update to:\ngeneral\nor\nspecific");
            System.exit(1);
        }
        // Add item
        // Empty index/spot, so simply add it.
        if (hashTable[index] == null) hashTable[index] = new NGen<>(item, null);
        else {
            // COLLISION
            // Add to the end of the chain.
            boolean duplicates = false;
            NGen<T> ptr = hashTable[index];
            while (ptr.getNext() != null) {
                if (ptr.getData().equals(item)) {
                    duplicates = true;
                    break;
                }
                ptr = ptr.getNext();
            }
            if (!ptr.getData().equals(item) && !duplicates) ptr.setNext(new NGen<>(item, null));
        }
    }

    // ** Already implemented -- no need to change **
    // Adds all words from a given file to the hash table using the add(T item) method above
    @SuppressWarnings("unchecked")
    public void addWordsFromFile(String fileName) {
        Scanner fileScanner = null;
        String word;
        try {
            fileScanner = new Scanner(new File(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File: " + fileName + " not found.");
            System.exit(1);
        }
        while (fileScanner.hasNext()) {
            word = fileScanner.next();
            word = word.replaceAll("\\p{Punct}", ""); // removes punctuation
            this.add((T) word);
        }
    }
    public void display() {
        // See 'average collision length' implementation explanation below
        // Calculated differently than explained in the write-up,
        // but should still be graded as correct.
        int uniqueWords = 0; // Number of non-null spots
        int emptySpots = 0;
        // Non-empty spots = Table Length - Empty Spots
        double collisionSum = 0;
        double totalCollisions = 0; // Every chain >= 2, because with chain == 1,
                                    // nothing has 'collided' yet.
        int longestChain = 0;
        int counter = 0;

        for (int i = 0; i < hashTable.length; i++) {
            if (hashTable[i] == null) {
                System.out.println("Element " + i + ": null");
                emptySpots++;
            } else {
                // At LEAST one item at given index, loop through and print
                String print = "Element "+i+": ";
                for (NGen<T> ptr = hashTable[i]; ptr != null; ptr = ptr.getNext()) {
                    counter++;
                    uniqueWords++;
                    if (ptr.getNext() == null) print += ptr.getData();
                    else print += ptr.getData() + ", ";
                }
                if (counter > 1) {
                    // More than 1 item in the list, therefore a collision has happened here.
                    collisionSum += counter;
                    totalCollisions++;
                    // Update longestChain in table accordingly:
                    if (counter > longestChain) longestChain = counter;
                }
                // Important to reset counter so collided indexes are counted properly
                counter = 0;
                System.out.println(print);
            }
        }
        System.out.println("---------------------------------------------");
        System.out.println("# Unique Words: "+uniqueWords);
        System.out.println("# Empty Spots: "+emptySpots);
        System.out.println("# Non-Empty Spots: "+(hashTable.length-emptySpots));
        // Not using given calculation of average collision length in write up below:
//        System.out.println("Average Collision Length: "+(double)(uniqueWords)/(hashTable.length-emptySpots));
        // To be clear here, I decided to only count something as a collision when there is at least 2 items at one
        // index. I assumed that if there was only one item at a given index, it would not count because nothing has been
        // 'collided'. Therefore, only collisions of 2 or greater were counted into the calculation of the average.
        System.out.println("Average Collision Length: "+(collisionSum/totalCollisions));
        System.out.println("Longest Chain: "+longestChain);
        System.out.println("---------------------------------------------");
    }
    public static void main(String[] args) {
        // gettysburg.txt and keywords.txt are implemented below, as instructed in the write-up.
        HashTable<String> general = new HashTable<>(150, "general");
        HashTable<String> specific = new HashTable<>(200, "specific");
        general.addWordsFromFile("gettysburg.txt");
        general.display();
        specific.addWordsFromFile("keywords.txt");
        specific.display();
    }
}
