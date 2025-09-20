package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.Invoice.InvoiceDetailResponse;
import com.Petcare.Petcare.DTOs.Invoice.InvoiceItem.InvoiceItemResponse;
import com.Petcare.Petcare.Services.PdfGenerationService;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationServiceImplement implements PdfGenerationService {

    @Override
    public byte[] generateInvoicePdf(InvoiceDetailResponse invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(30, 30, 30, 30);

            // --- 1. Encabezado del Documento ---
            // Aquí podrías añadir un logo si tuvieras la imagen
            // Image logo = new Image(ImageDataFactory.create("path/to/logo.png"));
            // document.add(logo);

            document.add(new Paragraph("Factura Petcare")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(22));

            document.add(new Paragraph("Servicios de Cuidado de Mascotas")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10).setMarginBottom(30));

            // --- 2. Información de la Factura y Cliente ---
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();

            // Columna Izquierda: Datos del Cliente
            Cell clientCell = new Cell().setPadding(5).setBorder(null);
            clientCell.add(new Paragraph("Facturado a:").setBold());
            clientCell.add(new Paragraph(invoice.getAccount().getAccountNumber()));
            // Aquí podrías añadir la dirección de la cuenta si la tuvieras en el DTO
            infoTable.addCell(clientCell);

            // Columna Derecha: Datos de la Factura
            Cell invoiceCell = new Cell().setPadding(5).setBorder(null).setTextAlignment(TextAlignment.RIGHT);
            invoiceCell.add(new Paragraph("Factura #: " + invoice.getInvoiceNumber()).setBold());
            invoiceCell.add(new Paragraph("Fecha de Emisión: " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            invoiceCell.add(new Paragraph("Fecha de Vencimiento: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            infoTable.addCell(invoiceCell);

            document.add(infoTable);
            document.add(new Paragraph("\n\n"));

            // --- 3. Tabla de Ítems de la Factura ---
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{60, 15, 25})).useAllAvailableWidth();
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold().setTextAlignment(TextAlignment.CENTER)));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.RIGHT)));

            for (InvoiceItemResponse item : invoice.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getDescription())));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))).setTextAlignment(TextAlignment.CENTER));
                itemsTable.addCell(new Cell().add(new Paragraph("$" + item.getLineTotal().toString())).setTextAlignment(TextAlignment.RIGHT));
            }
            document.add(itemsTable);

            // --- 4. Totales de la Factura ---
            document.add(new Paragraph("\n"));
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{75, 25})).useAllAvailableWidth();
            totalsTable.addCell(new Cell().add(new Paragraph("Subtotal:")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + invoice.getSubtotal())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph("Comisión Plataforma:")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + invoice.getPlatformFee())).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph("Total a Pagar:").setBold().setFontSize(14)).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + invoice.getTotalAmount()).setBold().setFontSize(14)).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

            document.add(totalsTable);

            // --- 5. Pie de Página ---
            document.add(new Paragraph("\n\n\n"));
            document.add(new Paragraph("Gracias por confiar en Petcare.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setFontSize(10));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la factura", e);
        }

        return baos.toByteArray();
    }
}