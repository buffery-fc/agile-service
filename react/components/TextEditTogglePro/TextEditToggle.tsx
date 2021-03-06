import React, {
  useState, useRef, cloneElement, useEffect, Fragment,
} from 'react';
import classNames from 'classnames';
import styles from './TextEditToggle.less';

interface RenderProps {
  value: any
  editing: boolean
}
interface Props {
  disabled?: boolean
  editor: () => JSX.Element
  editorExtraContent?: () => JSX.Element
  children: ({ value, editing }: RenderProps) => JSX.Element | JSX.Element
  onSubmit: (data: any) => void
  initValue: any
}

const TextEditToggle: React.FC<Props> = ({
  disabled, editor, editorExtraContent, children: text, onSubmit, initValue,
}) => {
  const [editing, setEditing] = useState(false);
  const dataRef = useRef(initValue);
  const editorRef = useRef<JSX.Element>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    dataRef.current = initValue;
  }, [initValue]);
  useEffect(() => {
    // 自动聚焦
    if (editing && editorRef.current) {
      // @ts-ignore
      editorRef.current.focus();
    }
  });
  const hideEditor = () => {
    if (editing) {
      setEditing(false);
    }
  };
  const showEditor = () => {
    if (!editing) {
      setEditing(true);
    }
  };
  const handleChange = (value: any) => {
    dataRef.current = value;
  };
  const handleEditorBlur = async () => {
    hideEditor();
    if (dataRef.current !== initValue) {
      await onSubmit(dataRef.current);
    }
  };
  const renderEditor = () => {
    const editorElement = typeof editor === 'function' ? editor() : editor;
    const extraContent = typeof editorExtraContent === 'function' ? editorExtraContent() : editorExtraContent;
    const editorProps: any = {
      // tabIndex: -1,
      defaultValue: initValue,
      onChange: handleChange,
      onBlur: handleEditorBlur,
      ref: editorRef,
      // autoFocus: true,
    };
    if (containerRef.current) {
      editorProps.style = {
        width: containerRef.current.getBoundingClientRect().width,
        height: containerRef.current.getBoundingClientRect().height,
      };
    }
    return (
      <Fragment>
        {cloneElement(editorElement, editorProps)}
        {extraContent}
      </Fragment>
    );
  };
  const renderText = () => {
    const textElement = typeof text === 'function' ? text({ value: dataRef.current, editing }) : text;
    return textElement;
  };
  const getCellRenderer = () => (
    <Fragment>
      {/* 编辑器在没编辑的时候也会渲染，目的是提前加载数据 */}
      {!disabled && (
        <div className={classNames(styles.editor, {
          [styles.hidden]: !editing,
        })}
        >
          {renderEditor()}
        </div>
      )}
      <div className={classNames(styles.text, {
        [styles.hidden]: editing,
      })}
      >
        {renderText()}
      </div>
    </Fragment>
  );
  const handleFocus = () => {
    if (!disabled) {
      showEditor();
    }
  };
  return (
    <div
      ref={containerRef}
      className={classNames(
        styles.container,
        { [styles.disabled]: disabled },
      )}
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
      tabIndex={0}
      onFocus={handleFocus}
    >
      {getCellRenderer()}
    </div>
  );
};

export default TextEditToggle;
