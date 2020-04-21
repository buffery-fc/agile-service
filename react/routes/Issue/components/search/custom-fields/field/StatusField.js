import React from 'react';
import { observer } from 'mobx-react-lite';
import { unionBy } from 'lodash';
import SelectFocusLoad from '@/components/SelectFocusLoad';
import { configTheme } from '@/common/utils';

let issueStatus = [];
function StatusField({ field, value, onChange }) {
  return (
    <SelectFocusLoad
      {...configTheme({
        list: issueStatus,
        textField: 'name',
        valueFiled: 'id',
      })}
      type="issue_status"
      showCheckAll={false}
      loadWhenMount
      style={{ width: 82, margin: '0 5px' }}
      mode="multiple"
      allowClear
      dropdownMatchSelectWidth={false}
      placeholder="状态"
      saveList={(v) => { issueStatus = unionBy(issueStatus, v, 'id'); }}
      filter={false}
      onChange={onChange}
      value={value}
      getPopupContainer={triggerNode => triggerNode.parentNode}
    />
  );
}
export default observer(StatusField);