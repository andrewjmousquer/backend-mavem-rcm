package com.portal.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.portal.model.SaleModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateFileReport {

	private static XSSFWorkbook workbook;
    private static XSSFSheet sheet;

	public static ByteArrayInputStream salesPdfReport(List<SaleModel> sales) {

		Document document = new Document();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {

			PdfPTable table = new PdfPTable(5);
			table.setWidthPercentage(100);
			table.setWidths(new int[] { 20, 15, 10, 15, 15 });

			Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

			PdfPCell hcell;
			hcell = new PdfPCell(new Phrase("Customer", headFont));
			hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Data/Hora", headFont));
			hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(hcell);
			
			hcell = new PdfPCell(new Phrase("Valor Total", headFont));
			hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(hcell);
			
			hcell = new PdfPCell(new Phrase("Primeira Parcela", headFont));
			hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(hcell);

			hcell = new PdfPCell(new Phrase("Parcelas", headFont));
			hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(hcell);
			
			sales.forEach(sale -> {

				PdfPCell cell;

				cell = new PdfPCell(new Phrase(sale.getCustomer()));
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);

				
//				cell = new PdfPCell(new Phrase(formatDate(sale.getDate())));
//				cell.setPaddingLeft(5);
//				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//				table.addCell(cell);

				cell = new PdfPCell(new Phrase(sale.getValue().toString()));
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(5);
				table.addCell(cell);
				
				cell = new PdfPCell(new Phrase(sale.getFirstPayment().toString()));
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(5);
				table.addCell(cell);
				
				cell = new PdfPCell(new Phrase(sale.getPortion()));
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setPaddingRight(5);
				table.addCell(cell);
			
			});

			PdfWriter.getInstance(document, out);
			document.open();
			document.add(table);

			document.close();

		} catch (DocumentException ex) {
			log.error("Error pdf sale: {0}", ex);
		}

		return new ByteArrayInputStream(out.toByteArray());
	}

	private static String formatDate(LocalDateTime dateTime) {
		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		return formatador.format(dateTime);
	}
	
	private static void writeHeaderLine() {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("Sales");
         
        Row row = sheet.createRow(0);
         
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
         
        createCell(row, 0, "Customer", style);      
        createCell(row, 1, "Data/Hora", style);       
        createCell(row, 2, "Valor Total", style);    
        createCell(row, 3, "Primeira Parcela", style);
        createCell(row, 4, "Parcelas", style);
         
    }
     
    private static void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }
     
    private static void writeDataLines(List<SaleModel> sales) {
        int rowCount = 1;
 
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
                 
        for (SaleModel sale : sales) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
             
            createCell(row, columnCount++, sale.getCustomer(), style);
         //   createCell(row, columnCount++, formatDate(sale.getDate()), style);
            createCell(row, columnCount++, sale.getValue().toString(), style);
            createCell(row, columnCount++, sale.getFirstPayment().toString(), style);
            createCell(row, columnCount++, sale.getPortion(), style);
        };
    }
     
    public static void salesExcelReport(HttpServletResponse response, List<SaleModel> sales) throws IOException {
        writeHeaderLine();
        writeDataLines(sales);
         
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
         
        outputStream.close();
         
    }
}