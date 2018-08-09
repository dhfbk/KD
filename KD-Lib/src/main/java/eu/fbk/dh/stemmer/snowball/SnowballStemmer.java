
package eu.fbk.dh.stemmer.snowball;

/**
 * <p>Abstract SnowballStemmer class.</p>
 *
 * @author giovannimoretti
 * @version $Id: $Id
 */
public abstract class SnowballStemmer extends SnowballProgram {
    /**
     * <p>stem.</p>
     *
     * @return a boolean.
     */
    public  abstract boolean stem();
}
