package eu.fbk.dh.stemmer.snowball;

import java.lang.reflect.Method;

/**
 * <p>Among class.</p>
 *
 * @author giovannimoretti
 * @version $Id: $Id
 */
public class Among {
    /**
     * <p>Constructor for Among.</p>
     *
     * @param s a {@link java.lang.String} object.
     * @param substring_i a int.
     * @param result a int.
     * @param methodname a {@link java.lang.String} object.
     * @param methodobject a {@link eu.fbk.dh.stemmer.snowball.SnowballProgram} object.
     */
    public Among (String s, int substring_i, int result,
		  String methodname, SnowballProgram methodobject) {
        this.s_size = s.length();
        this.s = s.toCharArray();
        this.substring_i = substring_i;
	this.result = result;
	this.methodobject = methodobject;
	if (methodname.length() == 0) {
	    this.method = null;
	} else {
	    try {
		this.method = methodobject.getClass().
		getDeclaredMethod(methodname, new Class[0]);
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    public final int s_size; /* search string */
    public final char[] s; /* search string */
    public final int substring_i; /* index to longest matching substring */
    public final int result; /* result of the lookup */
    public final Method method; /* method to use if substring matches */
    public final SnowballProgram methodobject; /* object to invoke method on */
};
