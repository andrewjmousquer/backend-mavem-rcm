package com.portal.service.imp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SaleModel;
import com.portal.service.IHistoryService;
import com.portal.service.ISaleService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class HistoryService implements IHistoryService {
	
	@Autowired
	private ISaleService saleService;

	@Autowired
	private MessageSource messageSource;
	
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);	
    
	@Override
	public List<SaleModel> searchExport(SaleModel model, UserProfileDTO userProfile) throws AppException, BusException {
		try {		
			return this.saleService.search(model);
		} catch (Exception e) {
			throw new AppException( e );
		}
	}

	@Override
	public Long getTotalRecords(SaleModel model) throws AppException, BusException {
		try {
			return this.saleService.getTotalRecords(model);
		} catch (Exception e) {
			throw new AppException( e );
		}
	}

	@Override
	public byte[] generateExportExcel(SaleModel dto) throws AppException, BusException {
		List<SaleModel> list = this.saleService.search(dto);
		
        Workbook workbook = new XSSFWorkbook();

        CreationHelper createHelper = workbook.getCreationHelper();

        Sheet sheet = workbook.createSheet("Vendas");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLUE_GREY.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        String[] columns = {"Data", "Cliente", "Contato", "Tipo de Pagamento", "Valor", "Entrada", "Parcelas", "Taxa", "Vendedor"};
        
        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        int rowNum = 1;
        for(SaleModel sale: list) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            
            Cell dateCell = row.createCell(cellNum++);
            dateCell.setCellValue(sale.getDate());
            dateCell.setCellStyle(dateCellStyle);
            
            row.createCell(cellNum++).setCellValue(sale.getCustomer());
            row.createCell(cellNum++).setCellValue(sale.getContact());
            row.createCell(cellNum++).setCellValue(sale.getPaymentType());
            row.createCell(cellNum++).setCellValue(sale.getValue().doubleValue());
            row.createCell(cellNum++).setCellValue(sale.getFirstPayment().doubleValue());
            row.createCell(cellNum++).setCellValue(sale.getPortion());
            row.createCell(cellNum++).setCellValue(sale.getTax().doubleValue());
            row.createCell(cellNum++).setCellValue(sale.getUser().getPerson().getName());
        }

        for(int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try {
            workbook.write(bos);
            workbook.close();
            bos.close();
        } catch(IOException ex) {
        	logger.error("Erro ao gerar planilha para exportação: {}", ex.getMessage());
        	throw new AppException(messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
        }
        
        return bos.toByteArray();
	}
	
	
	@Override
	public byte[] generateExportPdf(SaleModel model) throws AppException, BusException {
		try {
			List<SaleModel> list = this.saleService.search(model);
			
			Document document = new Document(PageSize.A4);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, byteArrayOutputStream);
	
			document.open();
			
	        String[] columns = {"Data", "Cliente", "Contato", "Tipo de Pagamento", "Valor", "Entrada", "Parcelas", "Taxa", "Vendedor"};
			
			com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
			Paragraph paragraph = new Paragraph("Carbon - Vendas", titleFont);
			paragraph.setSpacingAfter(20);

			document.add(paragraph);
			
			PdfPTable table = new PdfPTable(columns.length);
			table.setWidthPercentage(100);
			table.setWidths(new int[] {10, 20, 10, 10, 10, 10, 10, 10, 10});
			Stream.of(columns)
		      .forEach(columnTitle -> {
		    	com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, new BaseColor(255, 255, 255));
		        PdfPCell header = new PdfPCell();
		        header.setBackgroundColor(new BaseColor(40, 127, 186));
		        header.setBorder(0);
		        header.setPhrase(new Phrase(columnTitle, font));
		        header.setPadding(5);
		        table.addCell(header);
		    });
			
			table.setHeaderRows(1);
			
			int index = 0;
			for(SaleModel sale : list) {
				com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA, 6);
				BaseColor baseColor = new BaseColor(255, 255, 255);
				if(index % 2 == 0) {
					baseColor = new BaseColor(237, 237, 237);
				}
				if(sale.getDate() != null) {					
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					PdfPCell cell = new PdfPCell(new Phrase(simpleDateFormat.format(sale.getDate().getTime()), font));
					cell.setBorderWidth(0);
					cell.setBackgroundColor(baseColor);
					cell.setVerticalAlignment(Element.ALIGN_CENTER);
			        table.addCell(cell);
				} else {
					PdfPCell cell = new PdfPCell(new Phrase("", font));
					cell.setBorderWidth(0);
					cell.setBackgroundColor(baseColor);
					cell.setVerticalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);
				}
				PdfPCell cell = new PdfPCell(new Phrase(sale.getCustomer(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getContact(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getPaymentType(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getValue().toString(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getFirstPayment().toString(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getPortion().toString(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getTax().toString() , font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        
		        cell = new PdfPCell(new Phrase(sale.getUser().getPerson().getName(), font));
				cell.setBorderWidth(0);
				cell.setBackgroundColor(baseColor);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
		        table.addCell(cell);
		        index++;
		    }
			
			document.add(table);
	
			document.close();
	        
	        return byteArrayOutputStream.toByteArray();
		} catch(Exception ex) {
			throw new AppException(messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
