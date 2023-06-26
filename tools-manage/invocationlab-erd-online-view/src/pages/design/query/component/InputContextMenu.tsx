import React, { useState, useRef, useEffect, } from 'react';
import { Input, } from 'antd';
import ContextMenu from './ContextMenu';

const InputContextMenu: React.FC<{
  value?: string;
  onChange?: (value: string) => void;
}> = ({ value, onChange }) => {
  const [inputValue, setInputValue] = useState<string>();
  const contextMenuRef = useRef<any>(null);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.target.value);
    onChange?.(e.target.value)
  };

  useEffect(() => {
    if (value === null) {
      setInputValue('<null>');
    } else if (value === undefined) {
      setInputValue('');
    } else {
      setInputValue(value);
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

  return (
    <>
      <ContextMenu onRef={contextMenuRef} onMenuItemClick={menuItemClick} />
      <Input
        type="text"
        size="small"
        value={inputValue}
        onChange={handleInputChange}
        onContextMenu={cellContextMenu}
      />
    </>
  );

};

export default React.memo(InputContextMenu)