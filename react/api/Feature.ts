import { axios } from '@choerodon/boot';
import { getProjectId, getOrganizationId } from '@/utils/common';

interface UFeatureColor {
  colorCode: string,
  issueId: number,
  objectVersionNumber: number,
}
class FeatureApi {
  get prefix() {
    return `/agile/v1/projects/${getProjectId()}`;
  }

  /**
   * 在子项目查询项目群的所有特性
   * @param featureIds 接口额外要返回的特性
   * @param param 搜索
   * @param page 第几页
   */
  queryAllInSubProject(featureIds: number[], param: string, page: number = 1) {
    return axios.post(
      `${this.prefix}/issues/feature/all`,
      featureIds || [],
      {
        params: {
          organizationId: getOrganizationId(),
          page,
          size: 10,
          param,
        },
      },
    );
  }

  /**
   * 在子项目根据piId查询项目群的特性
   * @param piId 不传默认查询活跃PI
   */
  getByPiIdInSubProject(piId?: number, sprintId?: number) {
    return axios.get(
      `${this.prefix}/issues/features`,
      {
        params: {
          organizationId: getOrganizationId(),
          piId,
          sprintId,
        },
      },
    );
  }

  /**
   * 根据史诗ID查询项目群的特性
   * @param epicId 可以不传
   */
  getByEpicId(epicId?: number) {
    return axios.get(
      `${this.prefix}/issues/feature/select_data`,
      {
        params: {
          organizationId: getOrganizationId(),
          epicId,
        },
      },
    );
  }

  /**
   * 查询特性的颜色
   */
  getColors() {
    return axios.get('/agile/v1/lookup_values/feature_color');
  }

  /**
   * 更新特性关联的团队或冲刺
   * @param data 
   */
  updateTeamAndSprint(data: {
    piId?: number,
    deleteSprintIds: number[],
    featureId: number,
    sprintIds: number[],
    teamProjectIds: number[],
    deleteTeamProjectIds: number[],
  }) {
    return axios.post(`${this.prefix}/board_feature/feature_link_project`, data);
  }

  /**
   * 更新特性颜色
   * @param data 
   */
  updateColor(data: UFeatureColor) {
    return axios.put(`${this.prefix}/issues/update_feature`, data);
  }


  /**
   * 根据summary查询史诗下是否有同名特性
   * @param summary 
   * @param epicId 
   */
  hasSameInEpicBySummary(summary: string, epicId: number) {
    return axios.post(`${this.prefix}/issues/check_feature_summary`, {
      epicId,
      summary,
    });
  }

  /**
   * 根据id查询史诗下是否有同名特性
   * @param featureId 
   * @param epicId 
   */
  hasSameInEpicById(featureId: number, epicId: number) {
    return axios.post(`${this.prefix}/issues/check_feature_summary`, {
      epicId,
      featureIds: [featureId],
    });
  }


  /**
   * 分页查询特性列表
   * @param page 
   * @param size 
   * @param issueId 
   * @param content 
   */
  loadFeaturesInLink(page: number = 1, size: number = 10, issueId: number, content: string) {
    return axios({
      method: 'get',
      url: `${this.prefix}/issues/agile/feature`,
      params: {
        page,
        size,
        self: false,
        issueId,
        content,
      },
    });
    // if (issueId && content) {
    //   return axios.get(`/agile/v1/projects/${projectId}/issues/agile/feature?issueId=${issueId}&self=false&content=${content}&page=${page}&size=${size}`);
    // } else if (issueId && !content) {
    //   return axios.get(`/agile/v1/projects/${projectId}/issues/agile/feature?issueId=${issueId}&self=false&page=${page}&size=${size}`);
    // } else if (!issueId && content) {
    //   return axios.get(`/agile/v1/projects/${projectId}/issues/agile/feature?self=false&content=${content}&page=${page}&size=${size}`);
    // } else {
    //   return axios.get(`/agile/v1/projects/${projectId}/issues/agile/feature?self=false&page=${page}&size=${size}`);
    // }
  }

  /**
   * 查询特性下拆分的故事
   * @param issueId 
   */
  getSplitStory(issueId: number) {
    return axios({
      method: 'post',
      url: `${this.prefix}/issues/list_story_by_feature_id`,
      params: {
        issueId,
      },
    });
  }

  /**
   * 将批量的issue加入到特性中
   * @param featureId 
   * @param issueIds 
   */
  addIssues(featureId: number, issueIds: Array<number>) {
    return axios.post(`${this.prefix}/issues/to_feature/${featureId}`, issueIds);
  }
}

const featureApi = new FeatureApi();
export { featureApi };
