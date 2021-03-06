import { axios } from '@choerodon/boot';
import { getProjectId } from '@/utils/common';

interface VersionCreateVO {
  name: string,
  projectId: number,
  description?: string,
  expectReleaseDate?: string | null,
  startDate?: string | null,
}

interface UVersionVO extends VersionCreateVO {
  objectVersionNumber: number,
  // versionId:number,
}
interface PublishVersion {
  projectId: number,
  versionId: number,
  releaseDate: string,
}
interface AdvancedSearch {
  advancedSearchArgs?: object,
  searchArgs?: object,
  content?: string,
}
interface DragVersionData {
  afterSequence?: number | null,
  beforeSequence?: number | null,
  objectVersionNumber: number,
  versionId: number
}
class VersionApi {
  get prefix() {
    return `/agile/v1/projects/${getProjectId()}`;
  }


  /**
   * 根据版本id查询版本详情及issue统计信息
   * @param versionId 
   */
  load(versionId: number) {
    return axios.get(`${this.prefix}/product_version/${versionId}`);
  }

  /**
   * 查询全部版本
   */
  loadAll() {
    return axios.get(`${this.prefix}/product_version`);
  }

  /**
   * 查找此项目下版本信息列表
   * @param page 
   * @param size 
   * @param filters 
   * 
   */
  loadVersionList(page: number = 1, size: number = 20, filters: AdvancedSearch) {
    return axios({
      method: 'post',
      url: `${this.prefix}/product_version/versions`,
      data: filters,
      params: {
        page,
        size,
      },
    });
  }

  /**
   * 根据状态查询版本名
   * @param statusArr 
   */
  loadNamesByStatus(statusArr: Array<string> = []) {
    return axios.post(`${this.prefix}/product_version/names`, statusArr);
  }

  /**
   * 删除版本前查询使用， 查询除删除外的所有版本名、删除的版本issue
   * @param versionId 待删除的版本id
   */
  loadNamesAndIssueBeforeDel(versionId: number) {
    return axios.get(`${this.prefix}/product_version/${versionId}/names`);
  }

  /**
   * 根据版本id查询的版本详细信息 用于查询规划中的版本
   * @param versionId 
   */
  loadPublicVersionDetail(versionId: number) {
    return axios.get(`${this.prefix}/product_version/${versionId}/plan_names`);
  }

  /**
   * 创建版本
   * @param versionCreateVO 
   */
  create(versionCreateVO: VersionCreateVO) {
    return axios.post(`${this.prefix}/product_version`, versionCreateVO);
  }

  /**
   * 将版本归档
   * @param versionId 
   */
  archived(versionId: number) {
    return axios.post(`${this.prefix}/product_version/${versionId}/archived`);
  }

  /**
   * 撤销归档版本
   * @param versionId 
   */
  revokeArchived(versionId: number) {
    return axios.post(`${this.prefix}/product_version/${versionId}/revoke_archived`);
  }

  /**
   * 发布版本
   * @param publishVersion 
   */
  publish(publishVersion: PublishVersion) {
    return axios.post(`${this.prefix}/product_version/release`, publishVersion);
  }

  /**
   * 撤销发布版本
   * @param versionId 
   */
  revokePublish(versionId: number) {
    return axios.post(`${this.prefix}/product_version/${versionId}/revoke_release`);
  }


  /**
   * 检查版本名是否重复
   * @param name 
   */
  checkName(name: string) {
    return axios({
      method: 'get',
      url: `${this.prefix}/product_version/check`,
      params: {
        name,
      },
    });
  }

  /**
   * 更新版本信息
   * @param versionId 
   * @param data 
   */
  update(versionId: number, data: UVersionVO) {
    return axios.put(`${this.prefix}/product_version/update/${versionId}`, data);
  }

  /**
   * 根据版本id删除版本
   * @param versionId 
   * @param targetVersionId 要移动到的目标版本id 
   */
  delete(versionId: number, targetVersionId?: number) {
    return axios({
      method: 'delete',
      url: `${this.prefix}/product_version/delete/${versionId}`,
      params: {
        targetVersionId,
      },
    });
  }

  /**
   * 拖拽版本
   * @param dragVersionData 
   */
  drag(dragVersionData: DragVersionData) {
    return axios.put(`${this.prefix}/product_version/drag`, dragVersionData);
  }

  /**
   * 将批量的issue加入到版本中
   * @param versionId 版本id
   * @param issueIds 
   */
  addIssues(versionId: number, issueIds: Array<number>) {
    return axios.post(`${this.prefix}/issues/to_version/${versionId}`, issueIds);
  }
}

const versionApi = new VersionApi();

export { versionApi };
