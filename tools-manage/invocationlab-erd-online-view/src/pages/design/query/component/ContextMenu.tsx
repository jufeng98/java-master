import React, { useState, useRef, useEffect, Ref, useImperativeHandle } from 'react';
import { Menu, MenuProps } from 'antd';
import { ContainerOutlined } from '@ant-design/icons';
import moment from "moment";

export type QueryTableContextMenuProps = {
    onRef?: Ref<any>;
    onMenuItemClick: Function;
};

type MenuItem = Required<MenuProps>['items'][number];

const QueryTableContextMenu: React.FC<QueryTableContextMenuProps> = (props) => {

    const [visible, setVisible] = useState(false);
    const [top, setTop] = useState(0);
    const [left, setLeft] = useState(0);
    const contextRef = useRef(null)

    const handleClick = () => setVisible(false)

    useImperativeHandle(props.onRef, () => ({
        showContext: (e: MouseEvent) => {
            setLeft(e.clientX + 12)
            setTop(e.clientY - 100)
            setVisible(true)
        },
    }));

    useEffect(() => {
        window.addEventListener('click', handleClick)
        return () => {
            window.removeEventListener('click', handleClick)
        }
    }, [])

    function getItem(
        label: React.ReactNode,
        key: React.Key,
        icon?: React.ReactNode,
        children?: MenuItem[],
        type?: 'group',
    ): MenuItem {
        return {
            key,
            icon,
            children,
            label,
            type,
        } as MenuItem;
    }

    const items: MenuItem[] = [
        getItem('设为NULL', '1', <ContainerOutlined />),
        getItem('设为空字符串', '2', <ContainerOutlined />),
        getItem('设为当前时间(YYYY-MM-DD HH:mm:ss)', '3', <ContainerOutlined />),
        getItem('设为当前时间(YYYY-MM-DD)', '4', <ContainerOutlined />),
        getItem('设为当前时间(HH:mm:ss)', '5', <ContainerOutlined />),
    ];

    const menuItemClick = (e: any) => {
        setVisible(false)
        if (e.key === '1') {
            props.onMenuItemClick('<null>')
        } else if (e.key === '2') {
            props.onMenuItemClick("")
        } else if (e.key === '3') {
            props.onMenuItemClick(moment(new Date()).format("YYYY-MM-DD HH:mm:ss"))
        } else if (e.key === '4') {
            props.onMenuItemClick(moment(new Date()).format("YYYY-MM-DD"))
        } else if (e.key === '5') {
            props.onMenuItemClick(moment(new Date()).format("HH:mm:ss"))
        }
    }

    return (visible &&
        (<div ref={contextRef} style={{ width: "320px", position: "fixed", zIndex: 10000, top: top + 'px', left: left + 'px', border: '1px solid lightgray' }}>
            <Menu
                mode="inline"
                theme="light"
                items={items}
                onClick={menuItemClick}
            />
        </div>)
    )
}
export default React.memo(QueryTableContextMenu)