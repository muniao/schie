/**
 * * Copyright &copy; 2015-2020 <a href="https://gitee.com/JeeHuangBingGui/jeeSpringCloud">JeeSpringCloud</a> All rights reserved..
 */
package com.schic.schie.modules.resource.web;

import com.alibaba.fastjson.JSON;
import com.jeespring.common.config.Global;
import com.jeespring.common.persistence.Page;
import com.jeespring.common.utils.StringUtils;
import com.jeespring.common.utils.http.HttpUtils;
import com.jeespring.common.web.AbstractBaseController;
import com.jeespring.modules.sys.dao.UserDao;
import com.jeespring.modules.sys.entity.Office;
import com.jeespring.modules.sys.entity.User;
import com.jeespring.modules.sys.security.SystemAuthorizingRealm;
import com.schic.schie.modules.resource.entity.*;
import com.schic.schie.modules.resource.service.IExResourcesService;
import com.schic.schie.modules.test.entity.tree.TestTree;
import com.schic.schie.modules.test.service.tree.TestTreeService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.schic.schie.modules.common.ExChangeConst.*;

/**
 * 资源管理Controller
 *
 * @author daihp
 * @version 2019-07-24
 */
@Controller
@RequestMapping(value = "${adminPath}/resource/exResources")
public class ExResourcesController extends AbstractBaseController {

    //调用dubbo服务器是，要去Reference注解,注解Autowired
    //@Reference(version = "1.0.0")
    @Autowired
    private IExResourcesService exResourcesService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TestTreeService treeService;

    static String resdirId = "resdirId";
    static String exResources = "exResources";
    static final String CONNPATH = HttpUtils.URL_SPLIT + "resource/exResources/list?";
    static final String ACTION = "action";
    static final String DATARESOURCE = "数据资源";

    @ModelAttribute
    public ExResources get(@RequestParam(required = false) String id) {
        ExResources entity = null;
        if (StringUtils.isNotBlank(id)) {
            entity = exResourcesService.getCache(id);
            //entity = exResourcesService.get(id);
        }
        if (entity == null) {
            entity = new ExResources();
        }
        return entity;
    }

    /**
     * 资源管理主页面
     *
     * @param
     * @param model
     * @return
     */
    @RequiresPermissions("resource:exResources:index")
    @RequestMapping(value = { "index" })
    public String index(Model model) {
        return "modules/resource/exResourcesIndex";
    }

    /**
     * 转到cron表达式参考页面
     * @param model
     * @return
     */
    @RequestMapping(value = { "findCron" })
    public String findCron(Model model) {
        return "modules/resource/cron";
    }

    /**
     * 操作成功列表页面
     */
    //    @RequiresPermissions("resource:exResources:list")
    @RequestMapping(value = { "list", "" })
    public String list(ExResources exResources, HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute(resdirId, exResources.getResdirId());
        User user = new User();
        SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) SecurityUtils.getSubject()
                .getPrincipal();
        String loginName = principal.getLoginName();
        user.setLoginName(loginName);
        User returnuser = userDao.getByLoginName(user);
        @NotNull(message = "归属部门不能为空")
        Office office = returnuser.getOffice();
        String id = office.getId();
        if (exResources.getCompanyId() == null || "".equals(exResources.getCompanyId())) {
            exResources.setCompanyId(id);
        }
        if (exResources.getResdirId() != null && exResources.getResdirId() != "") {
            TestTree tree = treeService.get(exResources.getResdirId());
            if (tree != null) {
                exResources.setResdirPath(getResdirPath(exResources.getResdirId()));
                exResources.setResdirId("");
            }
        }
        Page<ExResources> page = exResourcesService.findPageCache(new Page<ExResources>(request, response),
                exResources);
        //        List<ExResources> list = exResourcesService.findList(exResources);

        showData(page);

        //        Page<ExResources> page = exResourcesService.findPageCache(new Page<ExResources>(request, response), exResources);
        //Page<ExResources> page = exResourcesService.findPage(new Page<ExResources>(request, response), exResources);
        model.addAttribute("page", page);
        model.addAttribute("ExResources", page.getList());
        exResources.setOrderBy("totalDate");
        return "modules/resource/exResourcesList";
    }

    private void showData(Page<ExResources> page) {
        for (ExResources resources : page.getList()) {
            if ("1".equals(resources.getEnabled())) {
                resources.setEnabled("启用");
            } else {
                resources.setEnabled("停用");
            }
        }
    }

    /**
     * 操作成功列表页面
     */
    //    @RequiresPermissions("resource:exResources:list")
    @RequestMapping(value = { "listVue" })
    public String listVue(ExResources exResources, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        model.addAttribute(resdirId, exResources.getResdirId());
        User user = new User();
        SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) SecurityUtils.getSubject()
                .getPrincipal();
        String loginName = principal.getLoginName();
        user.setLoginName(loginName);
        User returnuser = userDao.getByLoginName(user);
        @NotNull(message = "归属部门不能为空")
        Office office = returnuser.getOffice();
        String id = office.getId();
        if (exResources.getCompanyId() == null || "".equals(exResources.getCompanyId())) {
            exResources.setCompanyId(id);
        }

        //        List<ExResources> list = exResourcesService.findList(exResources);
        Page<ExResources> page = exResourcesService.findPageCache(new Page<ExResources>(request, response),
                exResources);
        //Page<ExResources> page = exResourcesService.findPage(new Page<ExResources>(request, response), exResources);
        //        model.addAttribute("page", page);
        showData(page);
        model.addAttribute("ExResources", page.getList());
        model.addAttribute("page", page);
        exResources.setOrderBy("totalDate");
        return "modules/resource/exResourcesListVue";
    }

    /**
     * 实现页面跳转至HTTP资源编辑页面
     *
     * @return
     */
    @RequestMapping(value = { "http" })
    public String PageHttp(Model model) {
        ExResources exResourcess = new ExResources();
        exResourcess.setResType("http资源");
        model.addAttribute(exResources, exResourcess);
        return "modules/resource/exResourcesForm_httpTwo";
    }

    /**
     * 实现页面跳转至数据资源编辑页面
     *
     * @return
     */
    @RequestMapping(value = { "data" })
    public String PageData(Model model) {
        ExResources exResourcess = new ExResources();
        exResourcess.setResType("数据资源");
        model.addAttribute(exResources, exResourcess);
        return "modules/resource/exResourcesFormTwo";
    }

    /**
     * 操作成功列表页面
     */
    //RequiresPermissions("resource:exResources:select")
    @RequestMapping(value = { "select" })
    public String select(ExResources exResources, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        Page<ExResources> page = exResourcesService.findPageCache(new Page<ExResources>(request, response),
                exResources);
        //Page<ExResources> page = exResourcesService.findPage(new Page<ExResources>(request, response), exResources);
        model.addAttribute("page", page);
        return "modules/resource/exResourcesSelect";
    }

    /**
     * 查看，增加，编辑操作成功表单页面
     */
    @RequiresPermissions(value = { "resource:exResources:view", "resource:exResources:add",
            "resource:exResources:edit" }, logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(ExResources exResources, Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        model.addAttribute(resdirId, exResources.getResdirId());
        model.addAttribute(OLDSEARCH, request.getParameter(OLDSEARCH));
        if (exResources.getIsNewRecord()) {
            return "modules/resource/exResourcesFormTwo";
        } else {
            if ("数据资源".equals(exResources.getResType())) {
                model.addAttribute(ACTION, request.getParameter(ACTION));
                //资源详情Json
                ResourcesJson(exResources);
                //解析JSON
                analysisJson(exResources, request);
                model.addAttribute("exResources", exResources);
            }
            //         6   if (exResources.getResType().equals("http资源")) {
            //                model.addAttribute(ACTION, request.getParameter(ACTION));
            //                //解析资源详情JSON
            //                HttpResJson httpResJson = JSON.parseObject(exResources.getResJson(), HttpResJson.class);
            //                request.setAttribute("httpResJson", httpResJson);
            //                exResources.setContent(httpResJson.getContent());
            //                exResources.setMethod(httpResJson.getMethod());
            //                exResources.setUrl(httpResJson.getUrl());
            //                //解析出参json
            //                HttpOutJson httpOutJson = JSON.parseObject(exResources.getOutJson(), HttpOutJson.class);
            //                request.setAttribute("httpOutJson", httpOutJson);
            //                exResources.setTemplate(httpOutJson.getTemplate());
            //                exResources.setFormat(httpOutJson.getTemplateType());
            //                return "modules/resource/exResourcesForm_http_EditTwo";
            //            }
            return "modules/resource/exResourcesFormTwo";

        }
    }

    private void ResourcesJson(ExResources exResources) {
        if (exResources.getResJson() != null && exResources.getResJson().length() > 0) {
            String json = exResources.getResJson();
            Object json1 = JSON.parse(json);
            Map<String, String> map = (Map) json1;
            for (String s : map.keySet()) {
                if ("jdbc".equals(s)) {
                    exResources.setJdbc(map.get(s));
                } else if ("sql".equals(s)) {
                    exResources.setSql(map.get(s));
                }
            }
        }
    }

    private void analysisJson(ExResources exResources, HttpServletRequest request) {
        //        //解析订阅json
        if (exResources.getSubJson() != null && exResources.getSubJson().length() > 0) {
            String json = exResources.getSubJson();
            SubJson subJson = JSON.parseObject(json, SubJson.class);
            exResources.setDateText(subJson.getDateText());
            exResources.setDays(subJson.getDays());
            exResources.setKey(subJson.getKey());
            exResources.setBatch(subJson.getBatch());
            exResources.setCron(subJson.getCron());
        }
        if (exResources.getOutJson() != null && exResources.getOutJson().length() > 0) {
            List<OutJson> jsonList = JSON.parseArray(exResources.getOutJson(), OutJson.class);
            request.setAttribute("jsonList", jsonList);
        }
    }

    private String getOrgid() {
        //获取组织ID
        User user = new User();
        SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) SecurityUtils.getSubject()
                .getPrincipal();
        String loginName = principal.getLoginName();
        user.setLoginName(loginName);
        User returnuser = userDao.getByLoginName(user);
        @NotNull(message = "归属部门不能为空")
        Office office = returnuser.getOffice();
        return office.getId();
    }

    /**
     * 保存操作成功
     */
    @RequiresPermissions(value = { "resource:exResources:add", "resource:exResources:edit" }, logical = Logical.OR)
    @RequestMapping(value = "save")
    public String save(ExResources exResources, Model model, RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpServletResponse response) {
        if (!beanValidator(model, exResources)) {
            return form(exResources, model, request, response,redirectAttributes);
        }
        SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) SecurityUtils.getSubject()
                .getPrincipal();
        String name = principal.getName();
        if (exResources.getIsNewRecord() == true) {
            exResources.setCuser(name);
            exResources.setMuser(name);
            exResources.setStatus(STATUS_STORE);
        } else {
            exResources.setMuser(name);
        }

        //保存资源目录路径
        exResources.setResdirPath(getResdirPath(exResources.getResdirId()));

        //资源详情JSON
        HashMap<String, Object> map = new HashMap();
        map.put("jdbc", exResources.getJdbc());
        map.put("sql", exResources.getSql());
        exResources.setResJson(JSON.toJSON(map).toString());

        //出参Json
        //处理表格数据
        String outJson = exResources.getJson();
        String[] strarr = outJson.split("\\|");
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < strarr.length; i++) {
            OutJson outJson1 = new OutJson();
            outJson1.setoName(strarr[i].substring(0, strarr[i].indexOf(',')));
            outJson1.setoRemark(strarr[i].substring(strarr[i].indexOf(',') + 1, strarr[i].lastIndexOf(',')));
            outJson1.setoLevel(strarr[i].substring(strarr[i].lastIndexOf(',') + 1));
            arrayList.add(outJson1);
        }
        exResources.setOutJson(JSON.toJSON(arrayList).toString());

        //订阅json
        SubJson subJson = new SubJson();
        subJson.setBatch(exResources.getBatch());
        subJson.setCron(exResources.getCron());
        subJson.setDateText(exResources.getDateText());
        subJson.setDays(exResources.getDays());
        subJson.setKey(exResources.getKey());
        exResources.setSubJson(JSON.toJSON(subJson).toString());
        if (exResources.getCompanyId() == null || "".equals(exResources.getCompanyId())) {
            String orgid = getOrgid();
            exResources.setCompanyId(orgid);
        }
        Date date = new Date();
        exResources.setMdate(date);
        
        //判断是保存过来   还是 保存并提交过来
        if ("saveAndtj".equals(request.getParameter("tj"))) {
            exResources.setStatus(STATUS_SUBMIT);
        }
        exResourcesService.save(exResources);
        addMessage(redirectAttributes, "保存操作成功");
        return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
    }

    /**
     * 通过resdirId  获取树的路径  格式为  中国/四川/成都
     *
     * @param resdirId
     * @return
     */
    public String getResdirPath(String resdirId) {
        ArrayList<Object> list = new ArrayList<>();
        //查找到当前节点
        TestTree testTree = treeService.get(resdirId);
        String parentId;
        parentId = testTree.getParentId();
        list.add(testTree.getName());
        //循环找到最父级节点，且每一级节点名字装入list
        while (!"0".equals(parentId)) {
            TestTree testTree1 = treeService.get(parentId);
            list.add(testTree1.getName());
            parentId = testTree1.getParentId();
        }
        StringBuilder resdirPath = new StringBuilder();
        //倒序遍历，拼接路径
        ListIterator<Object> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            resdirPath.append(listIterator.previous().toString() + "/");
        }
        return resdirPath.toString().substring(0, resdirPath.toString().length() - 1);
    }

    /**
     * 保存http资源操作成功
     */
    @RequiresPermissions(value = { "resource:exResources:add", "resource:exResources:edit" }, logical = Logical.OR)
    @RequestMapping(value = "saveHttp")
    public String saveHttp(ExResources exResources, Model model, RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpServletResponse response) {

        if (!beanValidator(model, exResources)) {
            return form(exResources, model, request, response,redirectAttributes);
        }
        SystemAuthorizingRealm.Principal principal = (SystemAuthorizingRealm.Principal) SecurityUtils.getSubject()
                .getPrincipal();
        String name = principal.getName();
        if (exResources.getIsNewRecord() == true) {
            exResources.setCuser(name);
        } else {
            exResources.setMuser(name);
        }
        //资源详情JSON
        HashMap<String, Object> map = new HashMap();
        exResources.setResJson(JSON.toJSON(map).toString());
        //查询表格数据
        String queryJson = exResources.getQueryJson();
        String[] queryArray = queryJson.split("\\|");
        ArrayList arrayList = new ArrayList();
        HttpResJson httpResJson = new HttpResJson();
        httpResJson.setMethod(exResources.getMethod());
        httpResJson.setContent(exResources.getContent());
        httpResJson.setUrl(exResources.getUrl());

        for (int i = 0; i < queryArray.length; i++) {
            HttpQuery httpQuery = new HttpQuery();
            httpQuery.setqName(queryArray[i].substring(0, queryArray[i].indexOf(',')));
            httpQuery.setqValue(queryArray[i].substring(queryArray[i].lastIndexOf(',') + 1));
            arrayList.add(httpQuery);
        }
        httpResJson.setHttpQuery(arrayList);
        //Headers表格数据
        String HeaderJson = exResources.getHeadJson();
        String[] headArray = HeaderJson.split("\\|");
        ArrayList arrayList1 = new ArrayList();
        for (int i = 0; i < headArray.length; i++) {
            HttpHeader httpHeader = new HttpHeader();
            httpHeader.sethName(headArray[i].substring(0, headArray[i].indexOf(',')));
            httpHeader.sethValue(headArray[i].substring(headArray[i].lastIndexOf(',') + 1));
            arrayList1.add(httpHeader);
        }
        httpResJson.setHttpHeader(arrayList1);
        exResources.setResJson(JSON.toJSON(httpResJson).toString());

        //出参Json
        String outJson = exResources.getOutJson();
        HttpOutJson httpOutJson = new HttpOutJson();
        httpOutJson.setTemplate(exResources.getTemplate());
        httpOutJson.setTemplateType(exResources.getFormat());
        String[] outArray = outJson.split("\\|");
        ArrayList arrayList2 = new ArrayList();

        for (int i = 0; i < outArray.length; i++) {
            HttpOutTable httpOutTable = new HttpOutTable();
            httpOutTable.setName(outArray[i].substring(0, outArray[i].indexOf(',')));
            //第二次出现的位置
            int index = outArray[i].indexOf(',', outArray[i].indexOf(',') + 1);
            httpOutTable.setRemark(outArray[i].substring(outArray[i].indexOf(',') + 1, index));
            httpOutTable.setLevel(outArray[i].substring(index + 1, outArray[i].lastIndexOf(',')));
            httpOutTable.setMath(outArray[i].substring(outArray[i].lastIndexOf(',') + 1));
            arrayList2.add(httpOutTable);
        }
        httpOutJson.setList(arrayList2);
        exResources.setOutJson(JSON.toJSON(httpOutJson).toString());
        exResourcesService.save(exResources);
        addMessage(redirectAttributes, "保存操作成功");
        return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
    }

    /**
     * 删除操作成功
     */
    @RequiresPermissions("resource:exResources:del")
    @RequestMapping(value = "delete")
    public String delete(ExResources exResources, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        exResourcesService.delete(exResources);
        addMessage(redirectAttributes, "删除操作成功");
        return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
    }

    /**
     * 删除操作成功（逻辑删除，更新del_flag字段为1,在表包含字段del_flag时，可以调用此方法，将数据隐藏）
     */
    @RequiresPermissions(value = { "resource:exResources:del",
            "resource:exResources:delByLogic" }, logical = Logical.OR)
    @RequestMapping(value = "deleteByLogic")
    public String deleteByLogic(ExResources exResources, RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        if (STATUS_SUBMIT.equals(exResources.getStatus())){
            addMessage(redirectAttributes, "资源为提交状态不允许删除！！！");
            return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
        }
        if (STATUS_APPROVED.equals(exResources.getStatus())){
            addMessage(redirectAttributes, "资源为审核状态不允许删除！！！");
            return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
        }
        exResourcesService.deleteByLogic(exResources);
        addMessage(redirectAttributes, "逻辑删除操作成功");
        return REDIRECT + Global.getAdminPath() + CONNPATH + request.getParameter(OLDSEARCH);
    }

    /**
     * 批量删除操作成功
     */
    //    @RequiresPermissions("resource:exResources:del")
    //    @RequestMapping(value = "deleteAll")
    //    public String deleteAll(String ids, RedirectAttributes redirectAttributes,HttpServletRequest  request) {
    //        String idArray[] = ids.split(',');
    //        for (String id : idArray) {
    //            exResourcesService.delete(exResourcesService.get(id));
    //        }
    //        addMessage(redirectAttributes, "删除操作成功");
    //        return REDIRECT + Global.getAdminPath() +  CONNPATH+request.getParameter(OLDSEARCH);
    //    }

    /**
     * 批量删除操作成功（逻辑删除，更新del_flag字段为1,在表包含字段del_flag时，可以调用此方法，将数据隐藏）
     */
    //    @RequiresPermissions(value = {"resource:exResources:del", "resource:exResources:delByLogic"}, logical = Logical.OR)
    //    @RequestMapping(value = "deleteAllByLogic")
    //    public String deleteAllByLogic(String ids, RedirectAttributes redirectAttributes,HttpServletRequest  request) {
    //        String idArray[] = ids.split(',');
    //        for (String id : idArray) {
    //            exResourcesService.deleteByLogic(exResourcesService.get(id));
    //        }
    //        addMessage(redirectAttributes, "删除操作成功");
    //        return REDIRECT + Global.getAdminPath() +  CONNPATH+request.getParameter(OLDSEARCH);
    //    }

    /**
     * 导出excel文件
     */
    //    @RequiresPermissions("resource:exResources:export")
    //    @RequestMapping(value = "export", method = RequestMethod.POST)
    //    public String exportFile(ExResources exResources, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
    //        try {
    //            String fileName = "资源" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
    //            Page<ExResources> page = exResourcesService.findPage(new Page<ExResources>(request, response, -1), exResources);
    //            new ExportExcel("资源", ExResources.class).setDataList(page.getList()).write(response, fileName).dispose();
    //            return null;
    //        } catch (Exception e) {
    //            addMessage(redirectAttributes, "导出资源记录失败！失败信息：" + e.getMessage());
    //        }
    //        return REDIRECT + Global.getAdminPath() + "/resource111/exResources/?repage";
    //    }

    /**
     * 导入Excel数据
     */
    //    @RequiresPermissions("resource:exResources:import")
    //    @RequestMapping(value = "import", method = RequestMethod.POST)
    //    public String importFile(MultipartFile file, RedirectAttributes redirectAttributes) {
    //        try {
    //            int successNum = 0;
    //            ImportExcel ei = new ImportExcel(file, 1, 0);
    //            List<ExResources> list = ei.getDataList(ExResources.class);
    //            for (ExResources exResourcess : list) {
    //                exResourcesService.save(exResourcess);
    //            }
    //            successNum = list.size();
    //            addMessage(redirectAttributes, "已成功导入 " + successNum + " 条资源记录");
    //        } catch (Exception e) {
    //            addMessage(redirectAttributes, "导入资源失败！失败信息：" + e.getMessage());
    //        }
    //        return REDIRECT + Global.getAdminPath() + "/resource111/exResources/?repage";
    //    }

    /**
     * 下载导入资源数据模板
     */
    //    @RequiresPermissions("resource:exResources:import")
    //    @RequestMapping(value = "import/template")
    //    public String importFileTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
    //        try {
    //            String fileName = "资源数据导入模板.xlsx";
    //            List<ExResources> list = Lists.newArrayList();
    //            new ExportExcel("资源数据", ExResources.class, 1).setDataList(list).write(response, fileName).dispose();
    //            return null;
    //        } catch (Exception e) {
    //            addMessage(redirectAttributes, "导入模板下载失败！失败信息：" + e.getMessage());
    //        }
    //        return REDIRECT + Global.getAdminPath() + "/resource111/exResources/?repage";
    //    }
}
