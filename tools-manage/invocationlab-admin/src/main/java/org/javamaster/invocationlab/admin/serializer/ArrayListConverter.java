package org.javamaster.invocationlab.admin.serializer;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.bson.Document;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ArrayListConverter implements Converter<ArrayList<Document>> {
    public static ArrayListConverter INSTANCE = new ArrayListConverter();

    @Override
    public Class<?> supportJavaTypeKey() {
        return ArrayList.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public ArrayList<Document> convertToJavaData(CellData cellData, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellData<String> convertToExcelData(ArrayList<Document> documents, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        String str = documents.stream()
                .map(Document::toJson)
                .collect(Collectors.joining(",", "[", "]"));
        return new CellData<>(str);
    }
}
