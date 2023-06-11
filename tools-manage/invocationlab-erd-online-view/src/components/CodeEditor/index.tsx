import React, { Ref, useEffect, useImperativeHandle, useRef } from 'react';
import AceEditor from "react-ace";
import "./index.less";
import 'ace-builds/src-noconflict/mode-sql';
// import 'ace-builds/src-noconflict/mode-json';
import "ace-builds/src-noconflict/mode-mysql";
// import 'ace-builds/src-noconflict/mode-pgsql';
// import 'ace-builds/src-noconflict/mode-sqlserver';
// import "ace-builds/src-noconflict/mode-java";
// import 'ace-builds/src-noconflict/ext-searchbox';
import "ace-builds/src-noconflict/ext-language_tools";
import { addCompleter } from 'ace-builds/src-noconflict/ext-language_tools';


import 'ace-builds/src-noconflict/theme-terminal';
import 'ace-builds/src-noconflict/theme-xcode';
import { Ace } from "ace-builds";
import { IAceOptions, ICommand, IEditorProps, IMarker } from "react-ace/src/types";

import Parser from './parser/sqlParser.js';

export type CodeEditorProps = {
  selectDB: string,
  tables?: any[];
  onRef?: Ref<any>;
  name?: string;
  style?: React.CSSProperties;
  /** For available modes see https://github.com/thlorenz/brace/tree/master/mode */
  mode: string | object;
  /** For available themes see https://github.com/thlorenz/brace/tree/master/theme */
  theme?: string;
  height?: string;
  width?: string;
  className?: string;
  fontSize?: number | string;
  showGutter?: boolean;
  showPrintMargin?: boolean;
  highlightActiveLine?: boolean;
  focus?: boolean;
  cursorStart?: number;
  wrapEnabled?: boolean;
  readOnly?: boolean;
  minLines?: number;
  maxLines?: number;
  navigateToFileEnd?: boolean;
  debounceChangePeriod?: number;
  enableBasicAutocompletion?: boolean | string[];
  enableLiveAutocompletion?: boolean | string[];
  tabSize?: number;
  value?: string;
  placeholder?: string;
  defaultValue?: string;
  scrollMargin?: number[];
  enableSnippets?: boolean;
  onSelectionChange?: (value: any, event?: any) => void;
  onCursorChange?: (value: any, event?: any) => void;
  onInput?: (event?: any) => void;
  onLoad?: (editor: Ace.Editor) => void;
  onValidate?: (annotations: Ace.Annotation[]) => void;
  onChange?: (value: string, event?: any) => void;
  onSelection?: (selectedText: string, event?: any) => void;
  onCopy?: (value: string) => void;
  onPaste?: (value: string) => void;
  onFocus?: (event: any, editor?: Ace.Editor) => void;
  onBlur?: (event: any, editor?: Ace.Editor) => void;
  onScroll?: (editor: IEditorProps) => void;
  editorProps?: IEditorProps;
  setOptions?: IAceOptions;
  keyboardHandler?: string;
  commands?: ICommand[];
  annotations?: Ace.Annotation[];
  markers?: IMarker[];
};

const dbTablesMap: Map<string, any> = new Map()
const dbTableFieldsMap: Map<string, any> = new Map()

const CodeEditor: React.FC<CodeEditorProps> = (props) => {
  const { selectDB, mode, height, width, name, placeholder, value, theme, onChange, tables, } = props;

  const editorRef = useRef(null);

  const initDbTablesMapAndDbTableFieldsMap = () => {
    let tmpTables = tables || []
    dbTableFieldsMap.set(selectDB, {})
    let list: any[] = tmpTables
      .map(table => {
        let tb = {
          name: table.title,
          value: table.title,
          meta: table.chnname,
        }
        dbTableFieldsMap.get(selectDB)[table.title] = table.fields
          .map((field: any) => {
            return {
              name: field.name,
              value: field.name,
              meta: `${field.chnname}(${table.title})`,
              remarks: field.chnname,
              dataType: field.dataType,
              notNull: field.notNull,
              autoIncrement: field.autoIncrement,
              defaultValue: field.defaultValue,
              pk: field.pk,
            }
          })
        return tb;
      })
    dbTablesMap.set(selectDB, list)
    // console.log("1999", dbTablesMap.get(selectDB), dbTableFieldsMap.get(selectDB));
  }

  const resolveSql = (sql: string): any[] => {
    let tips: any[] = []
    // console.log("2000", sql);
    try {
      var result = Parser.parse(sql);
      let entries = Object.entries(result[0].source)
      if (result[0].joinmap) {
        entries.push(...Object.entries(result[0].joinmap))
      }
      entries.forEach(([alias, obj]) => {
        let tbName: string = obj.source
        let fields = dbTableFieldsMap.get(selectDB)[tbName]
        if (fields) {
          tips.push(...fields)
        }
        if (alias !== tbName) {
          tips.push({ name: alias, value: alias, meta: tbName })
        }
      })
    } catch (e) {
      console.warn(e);
    }
    return tips
  }

  useImperativeHandle(props.onRef, () => ({
    // changeVal 就是暴露给父组件的方法
    selectLine: () => {
      // @ts-ignore
      return editorRef.current.editor.selection.selectLine();
    },
    getEditor: () => {
      // @ts-ignore
      return editorRef.current.editor
    },
    getSelectValue: () => {
      //console.log(130, editorRef.current)
      // @ts-ignore
      return editorRef.current.editor.getSelectedText();
    },
    setSelectValue: (value: string) => {
      //console.log(130, editorRef.current)
      // @ts-ignore
      return editorRef.current.editor.insert(value);
    },
    getDbTableFieldsMap: (tableName: string) => {
      let mapObj = {}
      let fields = dbTableFieldsMap.get(selectDB)[tableName]
      if (!fields) {
        return mapObj
      }
      for (let i = 0; i < fields.length; i++) {
        const field = fields[i];
        mapObj[field.name] = field
      }
      return mapObj;
    },
  }));

  if (!dbTablesMap.get(selectDB)) {
    initDbTablesMapAndDbTableFieldsMap()
  }

  useEffect(() => {
    addCompleter({
      getCompletions: (editor: any, session: any, pos: any, prefix: any, callback: any) => {
        let sql: string = session.getTextRange({
          start: { row: pos.row, column: 0 },
          end: { row: pos.row, column: 2000 }
        }).toLowerCase()
        let tips = []
        if (sql.includes("select") && sql.includes("from")) {
          tips = resolveSql(sql)
          callback(null, dbTablesMap.get(selectDB).concat(tips));
          return
        }
        sql = session.getTextRange({
          start: { row: 0, column: 0 },
          end: { row: 100, column: 2000 }
        })
        sql = sql.replace(/[\r\n]/g, " ").replace(/\s{2,}/g, " ")
        tips = resolveSql(sql)
        callback(null, dbTablesMap.get(selectDB).concat(tips));
      }
    });
  }, [])

  return (<>
    <AceEditor
      ref={editorRef}
      width={width || '100%'}
      height={height || '180px'}
      mode={mode || 'sql'}
      theme={theme || 'xcode'}
      placeholder={placeholder || ''}
      onChange={onChange}
      name={name || 'ace-editor'}
      value={value}
      editorProps={{ $blockScrolling: true }}
      fontSize='14px'
      showGutter={true}
      highlightActiveLine={true}
      showPrintMargin={false}
      setOptions={{
        // 基础的自动完成
        enableBasicAutocompletion: true,
        // 实时自动完成
        enableLiveAutocompletion: true,
        // 代码块
        enableSnippets: false,
        // 显示行号
        showLineNumbers: true,
        // 用户输入的sql语句，自动换行
        wrap: false

      }}
    />
  </>);
}

export default React.memo(CodeEditor);

