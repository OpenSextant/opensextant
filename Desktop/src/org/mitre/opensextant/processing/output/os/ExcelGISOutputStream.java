package org.mitre.opensextant.processing.output.os;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.mitre.giscore.events.Feature;
import org.mitre.giscore.events.IGISObject;
import org.mitre.giscore.events.Row;
import org.mitre.giscore.events.Schema;
import org.mitre.giscore.events.SimpleField;
import org.mitre.giscore.events.SimpleField.Type;
import org.mitre.giscore.output.IGISOutputStream;
import org.mitre.giscore.output.StreamVisitorBase;
import org.mitre.giscore.utils.SafeDateFormat;

//import org.apache.poi.ss.usermodel.Row;

public class ExcelGISOutputStream extends StreamVisitorBase implements IGISOutputStream {

    private static final String ISO_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private SafeDateFormat dateFormatter;


    @SuppressWarnings("serial")
    public static List<String> IDENTIFIER_FIELDS = new ArrayList<String>() {
        {
            add("id");
            add("matchtext");
            add("context");
            add("filename");
            add("filepath");
            add("textpath");
            add("feat_class");
            add("feat_code");
            add("start");
            add("end");
        }
    };

    private SXSSFWorkbook workbook;
    private int rowNum = 0;
    private Sheet sheet;
    private File file;
    private Schema schema;
    private boolean writtenRow = false;
    private boolean isIdentifiers;

    public ExcelGISOutputStream(File xls, String worksheet, boolean isIdentifiers) {
        this.file = xls;
        this.workbook = new SXSSFWorkbook();
        this.isIdentifiers = isIdentifiers;
        this.sheet = workbook.createSheet(worksheet);

    }

    @Override
    public void close() throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.flush();
        out.close();
    }

    @Override
    public void write(IGISObject gisData) {

        gisData.accept(this);

    }

    @Override
    public void visit(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("row should never be null");
        }
        writtenRow = true;

        // Create a new row in the sheet:
        org.apache.poi.ss.usermodel.Row xlsRow = sheet.createRow((short) ++rowNum);

        if (schema != null && row.getSchema() != null) {
            URI schemauri = row.getSchema();
            if (schemauri == null || !schemauri.equals(schema.getId())) {
                throw new RuntimeException("Row schema doesn't match schema given");
            }
            try {
                int index = 0;
                for (String fieldname : schema.getKeys()) {
                    if (!isIdentifiers || IDENTIFIER_FIELDS.contains(fieldname)) {
                        SimpleField field = schema.get(fieldname);
                        addCell(index, row, field, xlsRow);
                        index++;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                int index = 0;
                for (SimpleField field : row.getFields()) {
                    addCell(index, row, field, xlsRow);
                    index++;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void visit(Feature feature) {
        visit((Row) feature);
    }

    /**
     * Gets the string value for a field
     * 
     * @param index
     * 
     * @param row
     *            Row containing field to be written
     * @param first
     *            boolean indicating if this is the first field in row
     * @param field
     *            SimpleField to be written
     * @param xlsRow
     * @return data object value for this field
     * @throws IOException
     *             if an IO error occurs
     */
    private void addCell(int index, Row row, SimpleField field, org.apache.poi.ss.usermodel.Row xlsRow) throws IOException {
		Cell cell = xlsRow.createCell(index); 
        Object value = row.getData(field);
        String outputString = formatValue(field.getType(), value);

       	cell.setCellValue(outputString); 

    }

    // Thread-safe date formatter helper method
    private SafeDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new SafeDateFormat(ISO_DATE_FMT);
        }
        return dateFormatter;
    }

    /**
     * Format a value according to the type, defaults to using toString.
     * 
     * @param type
     *            the type, assumed not <code>null</code>
     * @param data
     *            the data, may be a number of types, but must be coercible to
     *            the given type
     * @return a formatted value
     * @throws IllegalArgumentException
     *             if values cannot be formatted using specified data type.
     */
    private String formatValue(Type type, Object data) {
        if (data == null) {
            return "";
        } else if (Type.DATE.equals(type)) {
            Object val = data;
            if (val instanceof Date) {
                return getDateFormatter().format((Date) val);
            } else {
                return val.toString();
            }
        } else if (Type.DOUBLE.equals(type) || Type.FLOAT.equals(type)) {
            if (data instanceof String) {
                return (String) data;
            }

            if (data instanceof Number) {
                return String.valueOf(data);
            } else {
                throw new IllegalArgumentException("Data that cannot be coerced to float: " + data);
            }
        } else if (Type.INT.equals(type) || Type.SHORT.equals(type) || Type.UINT.equals(type) || Type.USHORT.equals(type)
                || Type.LONG.equals(type)) {
            if (data instanceof String) {
                return (String) data;
            }

            if (data instanceof Number) {
                return String.valueOf(data);
            } else {
                throw new IllegalArgumentException("Data that cannot be coerced to int: " + data);
            }
        } else {
            return data.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mitre.giscore.output.StreamVisitorBase#visit(org.mitre.giscore.events
     * .Schema)
     */
    @Override
    public void visit(Schema s) {
        if (writtenRow) {
            throw new RuntimeException("Can't set the schema after a row has been written");
        }
        if (schema != null) {
            throw new RuntimeException("Can't set the schema after a schema has already been set");
        }
        schema = s;

        // Create the column headings
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum);

        int index = 0;

        for (String field : schema.getKeys()) {
            if (!isIdentifiers || IDENTIFIER_FIELDS.contains(field)) {
                headerRow.createCell(index).setCellValue(new HSSFRichTextString(field));
                index++;
            }
        }
    }

}
