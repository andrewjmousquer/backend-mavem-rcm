package com.portal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import com.portal.exceptions.AppException;

public class FileUtils {

	public static InputStream getFileInputStream(String path) throws AppException {
		try {
			File initialFile = new File(path);
			if(initialFile.isFile()) {
				InputStream targetStream = new FileInputStream(initialFile);
				return targetStream;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static byte[] getFileBytes(String path) throws AppException {
		try {
			File initialFile = new File(path);
			if(initialFile.isFile()) {
				InputStream targetStream = new FileInputStream(initialFile); 
		    	return IOUtils.toByteArray(targetStream);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String getMediaType(String file) {
		if(file.contains(".jpg") || file.contains(".jpeg")) {
			return MediaType.IMAGE_JPEG_VALUE;
		} else if(file.contains(".png")) {
			return MediaType.IMAGE_PNG_VALUE;
		} else if(file.contains(".gif")) {
			return MediaType.IMAGE_GIF_VALUE;
		} else if(file.contains(".pdf")) {
			return MediaType.APPLICATION_PDF_VALUE;
		}
		
		return null;
	} 
}
