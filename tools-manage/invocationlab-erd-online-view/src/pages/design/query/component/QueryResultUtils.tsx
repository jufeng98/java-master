import { message } from "antd";
import { MouseEvent } from "react";
import { Resizable } from 'react-resizable';

export const copyValue = (val: string) => {
    const ele = document.createElement('textarea');
    ele.style.position = 'absolute';
    ele.style.left = '-9999px';
    ele.style.top = `-9999px`;
    document.body.appendChild(ele);
    ele.value = val;
    ele.select();
    document.execCommand('copy');
    document.body.removeChild(ele);
    message.success('复制成功')
}

export const handleTitle = (columnName: string, field: any) => {
    let title: string = ""
    if (columnName !== 'key') {
        if (field?.pk) {
            title = "*" + columnName + "(主键)"
        } else if (field?.notNull) {
            title = "*" + columnName
        } else {
            title = columnName
        }
    } else {
        title = columnName + '(虚拟列请勿修改)'
    }
    return title
}

export const handleTooltip = (field: any) => {
    let tooltip: string = ""
    if (field) {
        if (field.pk) {
            tooltip = `${field.remarks} --- ` + (field.autoIncrement ? '(自增)' : '(非自增)') + "(" + field.dataType + ")"
        } else {
            tooltip = `${field.remarks}` + " --- " + "(" + field.dataType + ")" + (field.defaultValue ? `(默认值:${field.defaultValue})` : '')
        }
    }
    return tooltip
}

export const getSuitField = (columnName: string, tableColumns: any, fieldMapObj: any) => {
    let field
    let tmp = tableColumns[columnName]
    if (tmp) {
        field = {
            name: tmp.name,
            value: tmp.name,
            remarks: tmp.remarks,
            dataType: tmp.typeName,
            notNull: tmp.isNullable === 'NO',
            autoIncrement: tmp.isAutoincrement === 'YES',
            defaultValue: tmp.def,
            pk: tmp.primaryKey,
        }
    } else {
        field = fieldMapObj[columnName]
    }
    return field
}

export const components = {
    header: {
        cell: (props: any) => {
            const { onResize, width, ...restProps } = props
            if (width === undefined) {
                return <th {...restProps}></th>;
            }
            return (
                <Resizable width={width} height={0} onResize={onResize}>
                    <th {...restProps}></th>
                </Resizable>
            );
        }
    }
}

export const onHeaderCell = (column: any, index: number, setColumns: Function) => {
    let obj = {
        width: column.width,
        // @ts-ignore
        onResize: (_: MouseEvent, { size }) => {
            setColumns((columns: any[]) => {
                const newColumns = [...columns]
                newColumns[index] = {
                    ...newColumns[index],
                    width: size.width,
                };
                return newColumns
            })
        }
    }
    return obj;
}
