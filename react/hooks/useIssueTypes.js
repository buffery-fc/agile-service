import { createContext, useContext } from 'react';
import { stores } from '@choerodon/boot';
import { loadIssueTypes } from '@/api/NewIssueApi';

const { AppState } = stores;
const IssueTypeContext = createContext({
  data: [],
  refresh: async () => {
    const type = AppState.currentMenuType.category === 'PROGRAM' ? 'program' : 'agile';
    const data = await loadIssueTypes(type);
    return data;
  },
});
export { IssueTypeContext };

export default function useIssueTypes() {
  const { data, refresh } = useContext(IssueTypeContext) || {};  
  return [data, refresh];
}
