package com.portal.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MaskFormatter;

import org.springframework.util.StringUtils;

public class PortalStringUtils {

	public static String integerListToString(List<Integer> list){
		if( list != null && !list.isEmpty() ){
			StringBuilder builder = new StringBuilder();
			
			for( Integer i : list ){
				builder.append( i );
				builder.append( "," );
			}
			
			return builder.substring( 0, builder.length()-1 );
		}

		return "";
	}
	
	public static <I> String listToString(List<I> list){
		if( list != null && !list.isEmpty() ){
			StringBuilder builder = new StringBuilder();

			for( I i : list ){
				builder.append( i.toString() );
				builder.append( "," );
			}

			return builder.substring( 0, builder.length()-1 );
		}

		return "";
	}

	public static String isValidString(String string) {
		if(string != null) {
			if(!string.equals("") && !string.equalsIgnoreCase("null")) {
				return string;
			}
		}
		
		return null;
	}
	
	public static String numberFormat(Double value, String format){
		if( value == null || value.isNaN() ) {
			value = 0d;
		}
		
		DecimalFormat df = new DecimalFormat( format );
		return df.format( value ).replaceAll("\\,", ".");
	}
	

	
	public static boolean validateEmail(String str){
		if(StringUtils.isEmpty(str))
			return false;
		Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		Matcher m = p.matcher(str.trim());
		return m.matches();
	}
	
	public static boolean validateFoneNumber(String str){
		if(StringUtils.isEmpty(str)) {
			return false;
		}
		
		Pattern p = Pattern.compile(".((10)|([1-9][1-9]).)\\s9?[6-9][0-9]{3}-[0-9]{4}");
		Matcher m = p.matcher(str);
		
		Pattern p2 = Pattern.compile(".((10)|([1-9][1-9]).)\\s[2-5][0-9]{3}-[0-9]{4}");
		Matcher m2 = p2.matcher(str);
		
		return m.matches() || m2.matches();
	}
	
	/**
	 * Converte de bytes para a unidade mais adequada.
	 * @param bytes
	 * @param si
	 * @return
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    
	    if (bytes < unit) 
	    	return bytes + " B";
	    
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    
	    return String.format("%.0f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static String formatCnpj(String cnpj) {
	    try {
	        MaskFormatter mask = new MaskFormatter("###.###.###/####-##");
	        mask.setValueContainsLiteralCharacters(false);
	        return mask.valueToString(cnpj);
	    } catch (ParseException ex) {
	    }
	    
		return cnpj;
	}
	
	public static String formatCpf(String cpf) {
	    try {
	        MaskFormatter mask = new MaskFormatter("###.###.###-##");
	        mask.setValueContainsLiteralCharacters(false);
	        return mask.valueToString(cpf);
	    } catch (ParseException ex) {
	    }
	    
		return cpf;
	}
	
	public static String extractOnlyNumber(String text) {
		return text.replaceAll("\\D+","");
	}
	
	public static String jiraIntegrationFormat(String text) {
		return text.toUpperCase().replaceAll(" ", "_");
	}
	
}


