package eu.fbk.dh.kd.lib;


import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Util Class of the tool.
 *
 * @author Giovanni Moretti - DH Group FBK.
 * @version $Id: $Id
 */
class KD_utils {
	/**
	 * <p>findArray.</p>
	 *
	 * @param array a {@link java.util.ArrayList} object.
	 * @param subArray a {@link java.util.ArrayList} object.
	 * @return a int.
	 */
	public static int findArray(ArrayList<String> array, ArrayList<String> subArray) {
		return Collections.indexOfSubList(array, subArray);
	}
	
	/**
	 * <p>md5.</p>
	 *
	 * @param file_path a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String md5(String file_path){
	    StringBuffer sb = new StringBuffer("");
		 try{
		    MessageDigest md = MessageDigest.getInstance("MD5");
		    FileInputStream fis = new FileInputStream(file_path);
		    byte[] dataBytes = new byte[1024];
		 
		    int nread = 0; 
		 
		    while ((nread = fis.read(dataBytes)) != -1) {
		      md.update(dataBytes, 0, nread);
		    };
		 
		    byte[] mdbytes = md.digest();
		 
		    //convert the byte to hex format
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    fis.close();
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return  sb.toString();
	}

	/**
	 * <p>countUppercase.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int countUppercase(String s){
		int uppercases = 0;
		for (int k = 0; k < s.length(); k++) {
			if (Character.isUpperCase(s.charAt(k))) uppercases++;

		}
		return uppercases;
	}




}
