import React, { useState, useRef, useEffect, Ref, useImperativeHandle } from 'react';
import { Menu, MenuProps, Card, Input, Button } from 'antd';
import { ContainerOutlined, SwapOutlined } from '@ant-design/icons';
import request from "../../../../utils/request";
import * as cache from "@/utils/cache";
import { CONSTANT } from "@/utils/constant";
import './AesContextMenu.less';
import * as QueryResultUtils from './QueryResultUtils';

export type QueryTableContextMenuProps = {
    onRef?: Ref<any>;
};

type MenuItem = Required<MenuProps>['items'][number];

const { TextArea } = Input;

const QueryTableContextMenu: React.FC<QueryTableContextMenuProps> = (props) => {

    const [loading, setLoading] = useState(false);
    const [aesSource, setAesSource] = useState('');
    const [aesResult, setAesResult] = useState('');
    const [selectedKeys, setSelectedKeys] = useState<Array<string>>([]);
    const [visible, setVisible] = useState(false);
    const [top, setTop] = useState(0);
    const [left, setLeft] = useState(0);
    const contextRef = useRef(null)

    const existsId = (ele: any, id: string): boolean => {
        if (!ele) {
            return false
        }
        if (ele.id === id) {
            return true
        }
        return existsId(ele.parentNode, id)
    }

    const handleClick = (e: any) => {
        if (existsId(e.target, 'aesMainAreaDiv')) {
            return
        }
        if (e.target?.innerText?.includes('密')) {
            return
        }
        setAesSource('')
        setAesResult('')
        setVisible(false)
    }

    useImperativeHandle(props.onRef, () => ({
        showContext: (e: any) => {
            setAesSource(e.target.innerText)
            menuItemClick({ key: 'decrypt', value: e.target.innerText })
            setLeft(e.clientX + 12)
            setTop(e.clientY - 120)
            setVisible(true)
        },
    }));

    useEffect(() => {
        window.addEventListener('click', handleClick)
        return () => {
            window.removeEventListener('click', handleClick)
        }
    }, [])

    function getItem(label: React.ReactNode, key: React.Key, icon?: React.ReactNode, children?: MenuItem[], type?: 'group'): MenuItem {
        return { key, icon, children, label, type, } as MenuItem;
    }

    const items: MenuItem[] = [
        getItem('加密', 'encrypt', <ContainerOutlined />),
        getItem('解密', 'decrypt', <ContainerOutlined />),
    ];

    const menuItemClick = (e: any) => {
        setLoading(true)
        setSelectedKeys([e.key])
        
        request.post('/ncnb/queryInfo/aes', {
            data: {
                projectId: cache.getItem(CONSTANT.PROJECT_ID),
                opType: e.key,
                value: e.value || aesSource
            }
        }).then(res => {
            setLoading(false)
            if (!res || !res.data) {
                return;
            }
            setAesResult(res.data)
        })
    }

    return (visible &&
        (<div id={"aesMainAreaDiv"} ref={contextRef}
            style={{ width: "200px", position: "fixed", zIndex: 10000, top: top + 'px', left: left + 'px', border: '1px solid lightgray' }}>
            <Menu
                mode="horizontal"
                theme="light"
                selectedKeys={selectedKeys}
                items={items}
                onClick={menuItemClick}
            />
            <Card loading={loading} bordered={false} style={{ height: 190, textAlign: 'center' }}>
                <TextArea rows={3} value={aesSource} onChange={e => setAesSource(e.target.value)}
                    onDoubleClick={() => { QueryResultUtils.copyValue(aesSource) }} />
                <Button icon={<SwapOutlined />} style={{ transform: 'rotate(90deg)' }} onClick={() => {
                    setAesSource(aesResult)
                    setAesResult(aesSource)
                }} />
                <TextArea rows={3} value={aesResult} readOnly={true}
                    onDoubleClick={() => { QueryResultUtils.copyValue(aesResult) }} />
            </Card>
        </div>)
    )
}
export default React.memo(QueryTableContextMenu)