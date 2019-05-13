package com.report.dto

class OrgTreeListDTO implements Serializable{
    /** 根组织ID **/
    String orgTreeId
    /** 组织层级 **/
    Integer orgTreeLayer
    /** 组织机构列表 **/
    List<OrgTreeDTO> orgTreeDTOList
}
