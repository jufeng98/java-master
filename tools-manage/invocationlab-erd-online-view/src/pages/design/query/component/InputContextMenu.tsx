import React, { useState, useRef, useEffect, } from 'react';
import { DatePicker, Input, TimePicker, } from 'antd';
import ContextMenu from './ContextMenu';
import dayjs from 'dayjs';
import advancedFormat from 'dayjs/plugin/advancedFormat'
import customParseFormat from 'dayjs/plugin/customParseFormat'
import localeData from 'dayjs/plugin/localeData'
import weekday from 'dayjs/plugin/weekday'
import weekOfYear from 'dayjs/plugin/weekOfYear'
import weekYear from 'dayjs/plugin/weekYear'

dayjs.extend(customParseFormat)
dayjs.extend(advancedFormat)
dayjs.extend(weekday)
dayjs.extend(localeData)
dayjs.extend(weekOfYear)
dayjs.extend(weekYear)

const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss'
const dateFormat = 'YYYY-MM-DD'
const timeFormat = 'HH:mm:ss'

const dateTimeReg = /^[1-2]\d{3}-[0-1]{0,1}\d-[0-3]{0,1}\d \d{2}:\d{2}:\d{2}$/
const dateReg = /^[1-2]\d{3}-[0-1]{0,1}\d-[0-3]{0,1}\d$/
const timeReg = /^\d{2}:\d{2}:\d{2}$/

const InputContextMenu: React.FC<{
  value?: string;
  onChange?: (value: string) => void;
  cellValue: string | null | undefined;
}> = ({ onChange, cellValue }) => {
  const [inputValue, setInputValue] = useState<string>()
  const contextMenuRef = useRef<any>(null)

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
    onChange?.(e.target.value)
  };

  useEffect(() => {
    if (cellValue === null) {
      setInputValue('<null>');
    } else if (cellValue === undefined) {
      setInputValue('');
    } else {
      setInputValue(cellValue);
    }
  }, [])

  const cellContextMenu = (e: React.MouseEvent<HTMLInputElement>) => {
    contextMenuRef?.current?.showContext(e)
    e.preventDefault()
  }

  const menuItemClick = (value: string) => {
    setInputValue(value);
    onChange?.(value)
  }

  const onDateTimePickerChange = (date: any) => {
    menuItemClick(date.format(dateTimeFormat))
  }
  const onDatePickerChange = (date: any) => {
    menuItemClick(date.format(dateFormat))
  }
  const onTimePickerChange = (date: any) => {
    menuItemClick(date.format(timeFormat))
  }

  let content
  if (inputValue && dateTimeReg.test(inputValue)) {
    content = <DatePicker value={dayjs(inputValue, dateTimeFormat)} onChange={onDateTimePickerChange} changeOnBlur={true}
      showTime={{ format: dateTimeFormat }} format={dateTimeFormat} allowClear={false} onContextMenu={cellContextMenu} />
  } else if (inputValue && dateReg.test(inputValue)) {
    content = <DatePicker value={dayjs(inputValue, dateFormat)} allowClear={false}
      onChange={onDatePickerChange} onContextMenu={cellContextMenu} />
  } else if (inputValue && timeReg.test(inputValue)) {
    content = <TimePicker value={dayjs(inputValue, timeFormat)} allowClear={false}
      onChange={onTimePickerChange} onContextMenu={cellContextMenu} />
  } else {
    content = <Input
      type="text"
      size="small"
      value={inputValue}
      onChange={handleInputChange}
      onContextMenu={cellContextMenu}
    />
  }

  return (
    <>
      <ContextMenu onRef={contextMenuRef} onMenuItemClick={menuItemClick} />
      {content}
    </>
  );

};

export default React.memo(InputContextMenu)
