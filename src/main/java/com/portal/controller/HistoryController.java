package com.portal.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SaleModel;
import com.portal.service.IHistoryService;

@RestController
@RequestMapping("/protected/history")
@CrossOrigin(origins = "*")
public class HistoryController extends BaseController {

	@Autowired
	private IHistoryService service;
		
	/**
	 * Retorna lista filtrada de transações para exportação.
	 *
	 * @return ResponseEntity<Response<SaleModel>>
	 * @throws BusException
	 * @throws AppException
	 */
	@PostMapping(value = "/search")
	public ResponseEntity<List<SaleModel>> search(@RequestBody SaleModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.searchExport(model, this.getUserProfile()));
	}

	/**
	 * Retorna a quantidade de transações de acordo com os filtros.
	 *
	 * @return ResponseEntity<Long>
	 * @throws BusException
	 * @throws AppException
	 */
	@PostMapping(value = "/totalRecords")
	public ResponseEntity<Long> getTotalRecords(@RequestBody SaleModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getTotalRecords(model));
	}
	
	@PostMapping(value = "/exportExcel")
	public ResponseEntity<byte[]> exportExcel(HttpServletResponse response, @RequestBody SaleModel model) throws Exception {
		byte[] data = this.service.generateExportExcel(model);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if(data != null) {
			out = new ByteArrayOutputStream(data.length);
		    out.write(data, 0, data.length);
	    }

	    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	    response.addHeader("Content-Disposition", "attachment;");

	    OutputStream os;
	    try {
	        os = response.getOutputStream();
	        out.writeTo(os);
	        os.flush();
	        os.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    HttpHeaders respHeaders = new HttpHeaders();
	    if(data != null) {
	    	respHeaders.setContentLength(data.length);
	    }
	    respHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	    respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;");
		
		return ResponseEntity.ok(data);
	}
	
	@PostMapping(value = "/exportPdf")
	public ResponseEntity<byte[]> exportPdf(HttpServletResponse response, @RequestBody SaleModel model) throws Exception {
		byte[] data = this.service.generateExportPdf(model);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if(data != null) {
			out = new ByteArrayOutputStream(data.length);
		    out.write(data, 0, data.length);
	    }

	    response.setContentType("application/pdf");
	    response.addHeader("Content-Disposition", "attachment;");

	    OutputStream os;
	    try {
	        os = response.getOutputStream();
	        out.writeTo(os);
	        os.flush();
	        os.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    HttpHeaders respHeaders = new HttpHeaders();
	    if(data != null) {
	    	respHeaders.setContentLength(data.length);
	    }
	    respHeaders.setContentType(MediaType.parseMediaType("application/pdf"));
	    respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;");
		
		return ResponseEntity.ok(data);
	}

}
