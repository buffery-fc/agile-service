package io.choerodon.agile.app.service.impl;

import io.choerodon.agile.api.vo.QuickFilterSequenceVO;
import io.choerodon.agile.api.vo.QuickFilterVO;
import io.choerodon.agile.api.vo.QuickFilterSearchVO;
import io.choerodon.agile.api.vo.QuickFilterValueVO;
import io.choerodon.agile.app.service.ObjectSchemeFieldService;
import io.choerodon.agile.app.service.QuickFilterService;
import io.choerodon.agile.infra.dto.ObjectSchemeFieldDTO;
import io.choerodon.agile.infra.dto.QuickFilterDTO;
import io.choerodon.agile.infra.enums.CustomFieldType;
import io.choerodon.agile.infra.mapper.QuickFilterFieldMapper;
import io.choerodon.agile.infra.mapper.QuickFilterMapper;
import io.choerodon.agile.infra.utils.ProjectUtil;
import io.choerodon.core.exception.CommonException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/6/13.
 * Email: fuqianghuang01@gmail.com
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class QuickFilterServiceImpl implements QuickFilterService {

    protected static final String NOT_IN = "not in";
    protected static final String IS_NOT = "is not";
    protected static final String NULL_STR = "null";
    protected static final String IS = "is";

    @Autowired
    private QuickFilterMapper quickFilterMapper;

    @Autowired
    protected QuickFilterFieldMapper quickFilterFieldMapper;

    @Autowired
    private ObjectSchemeFieldService objectSchemeFieldService;

    @Autowired
    private ProjectUtil projectUtil;

    private static final String NOT_FOUND = "error.QuickFilter.notFound";
    @Autowired
    private ModelMapper modelMapper;

    protected void dealCaseComponent(String field, String value, String operation, StringBuilder sqlQuery) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_component_issue_rel )  ");
            } else if (IS_NOT.equals(operation)) {
                sqlQuery.append(" issue_id in ( select issue_id from agile_component_issue_rel )  ");
            }
        } else {
            if (NOT_IN.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_component_issue_rel where component_id in " + value + " ) ");
            } else {
                sqlQuery.append(" issue_id in ( select issue_id from agile_component_issue_rel where " + field + " " + operation + " " + value + " ) ");
            }
        }
    }

    private void dealFixVersion(QuickFilterValueVO quickFilterValueVO, String field, String value, String operation, StringBuilder sqlQuery) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_version_issue_rel where relation_type = 'fix' ) ");
            } else if (IS_NOT.equals(operation)) {
                sqlQuery.append(" issue_id in ( select issue_id from agile_version_issue_rel where relation_type = 'fix' ) ");
            }
        } else {
            if (NOT_IN.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_version_issue_rel where version_id in " + value + " and relation_type = 'fix' ) ");
            } else {
                sqlQuery.append(" issue_id in ( select issue_id from agile_version_issue_rel where " + field + " " + quickFilterValueVO.getOperation() + " " + value + " and relation_type = 'fix' ) ");
            }
        }
    }

    private void dealInfluenceVersion(QuickFilterValueVO quickFilterValueVO, String field, String value, String operation, StringBuilder sqlQuery) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_version_issue_rel where relation_type = 'influence' ) ");
            } else if (IS_NOT.equals(operation)) {
                sqlQuery.append(" issue_id in ( select issue_id from agile_version_issue_rel where relation_type = 'influence' ) ");
            }
        } else {
            if (NOT_IN.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_version_issue_rel where version_id in " + value + " and relation_type = 'influence' ) ");
            } else {
                sqlQuery.append(" issue_id in ( select issue_id from agile_version_issue_rel where " + field + " " + quickFilterValueVO.getOperation() + " " + value + " and relation_type = 'influence' ) ");
            }
        }
    }

    protected void dealCaseVersion(QuickFilterValueVO quickFilterValueVO, String field, String value, String operation, StringBuilder sqlQuery) {
        if ("fix_version".equals(quickFilterValueVO.getFieldCode())) {
            dealFixVersion(quickFilterValueVO, field, value, operation, sqlQuery);
        } else if ("influence_version".equals(quickFilterValueVO.getFieldCode())) {
            dealInfluenceVersion(quickFilterValueVO, field, value, operation, sqlQuery);
        }
    }

    protected void dealCaseLabel(String field, String value, String operation, StringBuilder sqlQuery) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_label_issue_rel ) ");
            } else if (IS_NOT.equals(operation)) {
                sqlQuery.append(" issue_id in ( select issue_id from agile_label_issue_rel ) ");
            }
        } else {
            if (NOT_IN.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_label_issue_rel where label_id in " + value + " ) ");
            } else {
                sqlQuery.append(" issue_id in ( select issue_id from agile_label_issue_rel where " + field + " " + operation + " " + value + " ) ");
            }
        }
    }

    protected void dealCaseSprint(String field, String value, String operation, StringBuilder sqlQuery) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_issue_sprint_rel ) ");
            } else if (IS_NOT.equals(operation)) {
                sqlQuery.append(" issue_id in ( select issue_id from agile_issue_sprint_rel ) ");
            }
        } else {
            if (NOT_IN.equals(operation)) {
                sqlQuery.append(" issue_id not in ( select issue_id from agile_issue_sprint_rel where sprint_id in " + value + " ) ");
            } else {
                sqlQuery.append(" issue_id in ( select issue_id from agile_issue_sprint_rel where " + field + " " + operation + " " + value + " ) ");
            }
        }
    }

    private String getSqlQuery(QuickFilterVO quickFilterVO, Long projectId) {
        Long organizationId = projectUtil.getOrganizationId(projectId);
        List<QuickFilterValueVO> quickFilterValueVOList = quickFilterVO.getQuickFilterValueVOList();
        List<String> relationOperations = quickFilterVO.getRelationOperations();
        Boolean childIncluded = quickFilterVO.getChildIncluded();
        StringBuilder sqlQuery = new StringBuilder();
        int idx = 0;
        for (QuickFilterValueVO quickFilterValueVO : quickFilterValueVOList) {
            Boolean predefined = quickFilterValueVO.getPredefined();
            String fieldCode = quickFilterValueVO.getFieldCode();
            if (ObjectUtils.isEmpty(predefined)) {
                String errorMsg = "error." + fieldCode + ".predefined.null";
                throw new CommonException(errorMsg);
            }
            if (predefined) {
                appendPredefinedFieldSql(sqlQuery, quickFilterValueVO, projectId);
            } else {
                sqlQuery.append(appendCustomFieldSql(quickFilterValueVO, organizationId, projectId));
            }
            int length = relationOperations.size();
            if (idx < length && !relationOperations.get(idx).isEmpty()) {
                sqlQuery.append(relationOperations.get(idx) + " ");
                idx++;
            }
        }
        if (!childIncluded) {
            sqlQuery.append(" and type_code != 'sub_task' ");
        }
        return sqlQuery.toString();
    }

    private String appendCustomFieldSql(QuickFilterValueVO quickFilterValueVO, Long organizationId, Long projectId) {
        String fieldCode = quickFilterValueVO.getFieldCode();
        ObjectSchemeFieldDTO objectSchemeField = objectSchemeFieldService.queryByFieldCode(organizationId, projectId, fieldCode);
        if (ObjectUtils.isEmpty(objectSchemeField)) {
            throw new CommonException("error.custom.field." + fieldCode + ".not.existed");
        }
        Long fieldId = objectSchemeField.getId();
        String value = "'null'".equals(quickFilterValueVO.getValue()) ? NULL_STR : quickFilterValueVO.getValue();
        String operation = quickFilterValueVO.getOperation();
        String customFieldType = quickFilterValueVO.getCustomFieldType();
        CustomFieldType.contains(customFieldType, true);

        String selectSql =
                " select ffv.instance_id from fd_field_value ffv where ffv.project_id = " + projectId
                        + " and ffv.field_id = " + fieldId;
        if (CustomFieldType.isOption(customFieldType)) {
            return getOptionOrNumberSql(value, operation, selectSql, "ffv.option_id");
        } else if (CustomFieldType.isDate(customFieldType)) {
            return getDateSql(value, operation, selectSql);
        } else if (CustomFieldType.isDateHms(customFieldType)) {
            return getDateHmsSql(value, operation, selectSql);
        } else if (CustomFieldType.isNumber(customFieldType)) {
            return getOptionOrNumberSql(value, operation, selectSql, "ffv.number_value");
        } else if (CustomFieldType.isString(customFieldType)) {
            return getStringOrTextSql(value, operation, selectSql, "ffv.string_value");
        } else  {
            //text
            return getStringOrTextSql(value, operation, selectSql, "ffv.text_value");
        }
    }

    private String getStringOrTextSql(String value, String operation, String selectSql, String column) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                StringBuilder build = new StringBuilder(" issue_id not in ( ");
                build.append(selectSql).append(")");
                return build.toString();
            } else if (IS_NOT.equals(operation)) {
                StringBuilder build = new StringBuilder(" issue_id in ( ");
                build.append(selectSql).append(")");
                return build.toString();
            } else {
                return "1=1";
            }
        } else {
            StringBuilder likeSql = new StringBuilder();
            likeSql
                    .append(" ( ")
                    .append(selectSql)
                    .append(" and ")
                    .append(column)
                    .append(" like concat(concat('%', '")
                    .append(value)
                    .append("'), '%')")
                    .append(" ) ");
            if ("not like".equals(operation)) {
                return "issue_id not in " + likeSql.toString();
            } else if ("like".equals(operation)) {
                return "issue_id in " + likeSql.toString();
            } else {
                StringBuilder builder = new StringBuilder(" issue_id in ");
                builder
                        .append(" ( ")
                        .append(selectSql)
                        .append(" and ")
                        .append(column)
                        .append(" ")
                        .append(operation)
                        .append(" '")
                        .append(value)
                        .append("' ) ");
                return builder.toString();

            }
        }
    }

    private String getOptionOrNumberSql(String value, String operation, String selectSql, String column) {
        if (NULL_STR.equals(value)) {
            if (IS.equals(operation)) {
                StringBuilder build = new StringBuilder(" issue_id not in ( ");
                build.append(selectSql).append(")");
                return build.toString();
            } else if (IS_NOT.equals(operation)) {
                StringBuilder build = new StringBuilder(" issue_id in ( ");
                build.append(selectSql).append(")");
                return build.toString();
            } else {
                return "1=1";
            }
        } else {
            if (NOT_IN.equals(operation)) {
                StringBuilder builder = new StringBuilder(" issue_id not in ");
                builder
                        .append(" ( ")
                        .append(selectSql)
                        .append(" and ")
                        .append(column)
                        .append(" in ")
                        .append(value)
                        .append(" ) ");
                return builder.toString();
            } else {
                StringBuilder builder = new StringBuilder(" issue_id in ");
                builder
                        .append(" ( ")
                        .append(selectSql)
                        .append(" and ")
                        .append(column)
                        .append(" ")
                        .append(operation)
                        .append(" ")
                        .append(value)
                        .append(" ) ");
                return builder.toString();
            }
        }
    }

    private String getDateHmsSql(String value, String operation, String selectSql) {
        StringBuilder build = new StringBuilder(" issue_id in ");
        build
                .append(" ( ")
                .append(selectSql)
                .append(" and time(DATE_FORMAT(ffv.date_value, '%H:%i:%s')) ")
                .append(operation)
                .append(" time('")
                .append(value)
                .append("')) ");
        return build.toString();
    }

    private String getDateSql(String value, String operation, String selectSql) {
        StringBuilder build = new StringBuilder(" issue_id in ");
        build
                .append(" ( ")
                .append(selectSql)
                .append(" and unix_timestamp(ffv.date_value) ")
                .append(operation)
                .append(" unix_timestamp('")
                .append(value)
                .append("')) ");
        return build.toString();
    }

    protected void appendPredefinedFieldSql(StringBuilder sqlQuery, QuickFilterValueVO quickFilterValueVO, Long projectId) {
        String value = "'null'".equals(quickFilterValueVO.getValue()) ? NULL_STR : quickFilterValueVO.getValue();
        String operation = quickFilterValueVO.getOperation();
        processPredefinedField(sqlQuery, quickFilterValueVO, value, operation);
    }

    protected void processPredefinedField(StringBuilder sqlQuery, QuickFilterValueVO quickFilterValueVO, String value, String operation) {
        String field = quickFilterFieldMapper.selectByPrimaryKey(quickFilterValueVO.getFieldCode()).getField();
        switch (field) {
            case "component_id":
                dealCaseComponent(field, value, operation, sqlQuery);
                break;
            case "version_id":
                dealCaseVersion(quickFilterValueVO, field, value, operation, sqlQuery);
                break;
            case "label_id":
                dealCaseLabel(field, value, operation, sqlQuery);
                break;
            case "sprint_id":
                dealCaseSprint(field, value, operation, sqlQuery);
                break;
            case "creation_date":
                sqlQuery.append(" unix_timestamp(" + field + ")" + " " + quickFilterValueVO.getOperation() + " " + "unix_timestamp('" + value + "') ");
                break;
            case "last_update_date":
                sqlQuery.append(" unix_timestamp(" + field + ")" + " " + quickFilterValueVO.getOperation() + " " + "unix_timestamp('" + value + "') ");
                break;
            default:
                sqlQuery.append(" " + field + " " + quickFilterValueVO.getOperation() + " " + value + " ");
                break;
        }
    }

    @Override
    public QuickFilterVO create(Long projectId, QuickFilterVO quickFilterVO) {
        if (!projectId.equals(quickFilterVO.getProjectId())) {
            throw new CommonException("error.projectId.notEqual");
        }
        if (checkName(projectId, quickFilterVO.getName())) {
            throw new CommonException("error.quickFilterName.exist");
        }
        String sqlQuery = getSqlQuery(quickFilterVO, projectId);
        QuickFilterDTO quickFilterDTO = modelMapper.map(quickFilterVO, QuickFilterDTO.class);
        quickFilterDTO.setSqlQuery(sqlQuery);
        //设置编号
        Integer sequence = quickFilterMapper.queryMaxSequenceByProject(projectId);
        quickFilterDTO.setSequence(sequence == null ? 0 : sequence + 1);
        if (quickFilterMapper.insert(quickFilterDTO) != 1) {
            throw new CommonException("error.quickFilter.insert");
        }
        return modelMapper.map(quickFilterMapper.selectByPrimaryKey(quickFilterDTO.getFilterId()), QuickFilterVO.class);
    }

    private Boolean checkNameUpdate(Long projectId, Long filterId, String quickFilterName) {
        QuickFilterDTO quickFilterDTO = quickFilterMapper.selectByPrimaryKey(filterId);
        if (quickFilterName.equals(quickFilterDTO.getName())) {
            return false;
        }
        QuickFilterDTO check = new QuickFilterDTO();
        check.setProjectId(projectId);
        check.setName(quickFilterName);
        List<QuickFilterDTO> quickFilterDTOList = quickFilterMapper.select(check);
        return quickFilterDTOList != null && !quickFilterDTOList.isEmpty();
    }

    @Override
    public QuickFilterVO update(Long projectId, Long filterId, QuickFilterVO quickFilterVO) {
        if (!projectId.equals(quickFilterVO.getProjectId())) {
            throw new CommonException("error.projectId.notEqual");
        }
        if (quickFilterVO.getName() != null && checkNameUpdate(projectId, filterId, quickFilterVO.getName())) {
            throw new CommonException("error.quickFilterName.exist");
        }
        String sqlQuery = getSqlQuery(quickFilterVO, projectId);
        quickFilterVO.setFilterId(filterId);
        QuickFilterDTO quickFilterDTO = modelMapper.map(quickFilterVO, QuickFilterDTO.class);
        quickFilterDTO.setSqlQuery(sqlQuery);
        return updateBySelective(quickFilterDTO);
    }

    @Override
    public void deleteById(Long projectId, Long filterId) {
        QuickFilterDTO quickFilterDTO = quickFilterMapper.selectByPrimaryKey(filterId);
        if (quickFilterDTO == null) {
            throw new CommonException("error.quickFilter.get");
        }
        if (quickFilterMapper.deleteByPrimaryKey(filterId) != 1) {
            throw new CommonException("error.quickFilter.delete");
        }
    }

    @Override
    public QuickFilterVO queryById(Long projectId, Long filterId) {
        QuickFilterDTO quickFilterDTO = quickFilterMapper.selectByPrimaryKey(filterId);
        if (quickFilterDTO == null) {
            throw new CommonException("error.quickFilter.get");
        }
        return modelMapper.map(quickFilterDTO, QuickFilterVO.class);
    }

    @Override
    public List<QuickFilterVO> listByProjectId(Long projectId, QuickFilterSearchVO quickFilterSearchVO) {
        List<QuickFilterDTO> quickFilterDTOList = quickFilterMapper.queryFiltersByProjectId(projectId, quickFilterSearchVO.getFilterName(), quickFilterSearchVO.getContents());
        if (quickFilterDTOList != null && !quickFilterDTOList.isEmpty()) {
            return modelMapper.map(quickFilterDTOList, new TypeToken<List<QuickFilterVO>>(){}.getType());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public QuickFilterVO dragFilter(Long projectId, QuickFilterSequenceVO quickFilterSequenceVO) {
        if (quickFilterSequenceVO.getAfterSequence() == null && quickFilterSequenceVO.getBeforeSequence() == null) {
            throw new CommonException("error.dragFilter.noSequence");
        }
        QuickFilterDTO quickFilterDTO = modelMapper.map(quickFilterMapper.selectByPrimaryKey(quickFilterSequenceVO.getFilterId()), QuickFilterDTO.class);
        if (quickFilterDTO == null) {
            throw new CommonException(NOT_FOUND);
        } else {
            if (quickFilterSequenceVO.getAfterSequence() == null) {
                Integer maxSequence = quickFilterMapper.queryMaxAfterSequence(quickFilterSequenceVO.getBeforeSequence(), projectId);
                quickFilterSequenceVO.setAfterSequence(maxSequence);
            } else if (quickFilterSequenceVO.getBeforeSequence() == null) {
                Integer minSequence = quickFilterMapper.queryMinBeforeSequence(quickFilterSequenceVO.getAfterSequence(), projectId);
                quickFilterSequenceVO.setBeforeSequence(minSequence);
            }
            handleSequence(quickFilterSequenceVO, projectId, quickFilterDTO);
        }
        return modelMapper.map(quickFilterMapper.selectByPrimaryKey(quickFilterSequenceVO.getFilterId()), QuickFilterVO.class);

    }

    private void handleSequence(QuickFilterSequenceVO quickFilterSequenceVO, Long projectId, QuickFilterDTO quickFilterDTO) {
        if (quickFilterSequenceVO.getBeforeSequence() == null) {
            quickFilterDTO.setSequence(quickFilterSequenceVO.getAfterSequence() + 1);
            updateBySelective(quickFilterDTO);
        } else if (quickFilterSequenceVO.getAfterSequence() == null) {
            if (quickFilterDTO.getSequence() > quickFilterSequenceVO.getBeforeSequence()) {
                Integer add = quickFilterDTO.getSequence() - quickFilterSequenceVO.getBeforeSequence();
                if (add > 0) {
                    quickFilterDTO.setSequence(quickFilterSequenceVO.getBeforeSequence() - 1);
                    updateBySelective(quickFilterDTO);
                } else {
                    quickFilterMapper.batchUpdateSequence(quickFilterSequenceVO.getBeforeSequence(), projectId,
                            quickFilterDTO.getSequence() - quickFilterSequenceVO.getBeforeSequence() + 1, quickFilterDTO.getFilterId());
                }
            }
        } else {
            Integer sequence = quickFilterSequenceVO.getAfterSequence() + 1;
            quickFilterDTO.setSequence(sequence);
            updateBySelective(quickFilterDTO);
            Integer update = sequence - quickFilterSequenceVO.getBeforeSequence();
            if (update >= 0) {
                quickFilterMapper.batchUpdateSequence(quickFilterSequenceVO.getBeforeSequence(), projectId, update + 1, quickFilterDTO.getFilterId());
            }
        }
    }

    @Override
    public Boolean checkName(Long projectId, String quickFilterName) {
        QuickFilterDTO quickFilterDTO = new QuickFilterDTO();
        quickFilterDTO.setProjectId(projectId);
        quickFilterDTO.setName(quickFilterName);
        List<QuickFilterDTO> quickFilterDTOList = quickFilterMapper.select(quickFilterDTO);
        return quickFilterDTOList != null && !quickFilterDTOList.isEmpty();
    }

    public QuickFilterVO updateBySelective(QuickFilterDTO quickFilterDTO) {
        if (quickFilterMapper.updateByPrimaryKeySelective(quickFilterDTO) != 1) {
            throw new CommonException("error.quickFilter.update");
        }
        return modelMapper.map(quickFilterMapper.selectByPrimaryKey(quickFilterDTO.getFilterId()), QuickFilterVO.class);
    }

}
