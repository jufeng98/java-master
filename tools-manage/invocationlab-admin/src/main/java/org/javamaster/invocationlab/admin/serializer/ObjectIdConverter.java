package org.javamaster.invocationlab.admin.serializer;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.bson.types.ObjectId;

public class ObjectIdConverter implements Converter<ObjectId> {
    public static ObjectIdConverter INSTANCE = new ObjectIdConverter();

    @Override
    public Class<ObjectId> supportJavaTypeKey() {
        return ObjectId.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public ObjectId convertToJavaData(CellData cellData, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        return new ObjectId(cellData.getStringValue());
    }

    @Override
    public CellData<String> convertToExcelData(ObjectId objectId, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) {
        return new CellData<>(objectId.toHexString());
    }
}
