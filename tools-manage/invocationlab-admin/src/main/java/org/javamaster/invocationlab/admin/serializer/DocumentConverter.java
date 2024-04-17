package org.javamaster.invocationlab.admin.serializer;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.bson.Document;

public class DocumentConverter implements Converter<Document> {
    public static DocumentConverter INSTANCE = new DocumentConverter();
    @Override
    public Class<?> supportJavaTypeKey() {
        return Document.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Document convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CellData<String> convertToExcelData(Document value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new CellData<>(value.toJson());
    }
}
