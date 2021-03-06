/**
 * * Copyright &copy; 2015-2020 <a href="https://gitee.com/JeeHuangBingGui/jeeSpringCloud">JeeSpringCloud</a>
 * All rights reserved..
 */
package com.schic.schie.modules.exchange.web;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jeespring.common.config.Global;
import com.jeespring.common.persistence.Page;
import com.jeespring.common.utils.DateUtils;
import com.jeespring.common.utils.StringUtils;
import com.jeespring.common.utils.excel.ExportExcel;
import com.jeespring.common.utils.excel.ImportExcel;
import com.jeespring.common.utils.http.HttpUtils;
import com.jeespring.common.web.AbstractBaseController;
import com.jeespring.modules.sys.dao.UserDao;
import com.jeespring.modules.sys.entity.Office;
import com.jeespring.modules.sys.entity.User;
import com.jeespring.modules.sys.security.SystemAuthorizingRealm;
import com.schic.schie.modules.exchange.entity.ExDbStandard;
import com.schic.schie.modules.exchange.service.IExDbStandardService;
import com.schic.schie.modules.resource.entity.OutJson;
import com.schic.schie.modules.test.service.tree.TestTreeService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.schic.schie.modules.common.ExChangeConst.OLDSEARCH;
import static com.schic.schie.modules.common.ExChangeConst.REDIRECT;

/**
 * 数据表管理Controller
 * @author XC
 * @version 2019-07-31
 */
@Controller
@RequestMapping(value = "${adminPath}/exchange/exDbStandard")
public class ExDbStandardController extends AbstractBaseController {

    public static final String PATH = HttpUtils.URL_SPLIT + "exchange/exDbStandard/?repage&exTabId=";
    public static final String SAVEPATH = HttpUtils.URL_SPLIT + "exchange/exDbStandard/list?";
    public static final String DELETE_PATH = HttpUtils.URL_SPLIT + "exchange/exDbStandard/?repage";
    public static final String DELETE_SUCCESS = "删除数据表成功";
    static final String TOTALDATE = "totalDate";
    //调用dubbo服务器是，要去Reference注解,注解Autowired
    //@Reference(version = "1.0.0")
    @Autowired
    private IExDbStandardService exDbStandardService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TestTreeService treeService;

    @ModelAttribute
    public ExDbStandard get(@RequestParam(required = false) String id) {
        ExDbStandard entity = null;
        if (StringUtils.isNotBlank(id)) {
            entity = exDbStandardService.getCache(id);
            //entity = exDbStandardService.get(id);
        }
        if (entity == null) {
            entity = new ExDbStandard();
        }
        return entity;
    }

    /**
     * 数据表统计主页面
     * @param exDbStandard
     * @param model
     * @return
     */
    @RequiresPermissions("exchange:exDbStandard:index")
    @RequestMapping(value = { "index" })
    public String index(ExDbStandard exDbStandard, Model model) {
        return "modules/exchange/exDbStandardIndex";
    }

    /**
     * 数据表统计页面
     */
    @RequiresPermissions("exchange:exDbStandard:total")
    @RequestMapping(value = { "total" })
    public String totalView(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        total(exDbStandard, request, model);
        return "modules/exchange/exDbStandardTotal";
    }

    private void total(ExDbStandard exDbStandard, HttpServletRequest request, Model model) {
        if (StringUtils.isEmpty(exDbStandard.getTotalType())) {
            exDbStandard.setTotalType("%Y-%m-%d");
        }
        //X轴的数据
        List<String> xAxisData = new ArrayList<>();
        //Y轴的数据
        Map<String, List<Double>> yAxisData = new HashMap<>();
        List<Double> countList = new ArrayList<>();
        //        List<Double> sumList = new ArrayList<Double>();
        if ( "".equals(exDbStandard.getOrderBy())) {
            exDbStandard.setOrderBy(TOTALDATE);
        }
        List<ExDbStandard> list = exDbStandardService.totalCache(exDbStandard);
        //List<ExDbStandard> list = exDbStandardService.total(exDbStandard);
        model.addAttribute("list", list);
        for (ExDbStandard exDbStandardItem : list) {
            //x轴数据
            xAxisData.add(exDbStandardItem.getTotalDate());
            countList.add(Double.valueOf(exDbStandardItem.getTotalCount()));
        }
        yAxisData.put("数量", countList);
        request.setAttribute("xAxisData", xAxisData);
        request.setAttribute("yAxisData", yAxisData);
        model.addAttribute("sumTotalCount", list.stream().mapToInt(ExDbStandard::getTotalCount).sum());

        //饼图数据
        Map<String, Object> orientData = new HashMap<>();
        for (ExDbStandard exDbStandardItem : list) {
            orientData.put(exDbStandardItem.getTotalDate(), exDbStandardItem.getTotalCount());
        }
        model.addAttribute("orientData", orientData);
    }

    @RequiresPermissions("exchange:exDbStandard:total")
    @RequestMapping(value = { "totalMap" })
    public String totalMap(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        if (StringUtils.isEmpty(exDbStandard.getTotalType())) {
            exDbStandard.setTotalType("%Y-%m-%d");
        }
        List<ExDbStandard> list = exDbStandardService.totalCache(exDbStandard);
        //List<ExDbStandard> list = exDbStandardService.total(exDbStandard);
        model.addAttribute("sumTotalCount", list.stream().mapToInt(ExDbStandard::getTotalCount).sum());
        model.addAttribute("list", list);
        return "modules/exchange/exDbStandardTotalMap";
    }

    /**
     * 数据表列表页面
     */
    //    @RequiresPermissions("exchange:exDbStandard:list")
    @RequestMapping(value = { "list", "" })
    public String list(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        if ("".equals(exDbStandard.getOrgid()) || null == exDbStandard.getOrgid()) {
            //获取组织ID
            String id = getOrgid();
            exDbStandard.setOrgid(id);
        }

        //        List<ExDbStandard> list = exDbStandardService.findList(exDbStandard);
        Page<ExDbStandard> page = exDbStandardService.findPageCache(new Page<ExDbStandard>(request, response),
                exDbStandard);
        //Page<ExDbStandard> page = exDbStandardService.findPage(new Page<ExDbStandard>(request, response), exDbStandard);
        model.addAttribute("exTabId", exDbStandard.getExTabId());
        model.addAttribute("page", page);
        model.addAttribute("ExDbStandard", page.getList());
        exDbStandard.setOrderBy(TOTALDATE);
        total(exDbStandard, request, model);
        return "modules/exchange/exDbStandardList";
    }

    /**
     * ajax请求 查询某张表的所有字段
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = {"Data"," "})
    public List<OutJson> getOneDb(HttpServletRequest request, HttpServletResponse response, Model model) {
        ExDbStandard ex = new ExDbStandard();
        String exTabid = request.getParameter("exTabId");
        ex.setExTabId(exTabid);
        List<ExDbStandard> exlist = exDbStandardService.findList(ex);

        //查到之后，构建成资源中的outjson传递回前端使用
        Iterator<ExDbStandard> exDbStandardIterator = exlist.iterator();
        ArrayList arrayList = new ArrayList();
        ExDbStandard dbStandard;
        String fieldName;
        String nameCn;
        while (exDbStandardIterator.hasNext()){
            OutJson outJson = new OutJson();
            dbStandard = exDbStandardIterator.next();
            fieldName = dbStandard.getFieldName();
            nameCn = dbStandard.getNameCn();
            outJson.setoName(fieldName);
            outJson.setoRemark(nameCn);
            arrayList.add(outJson);
        }
        String jsonlistsrc = JSON.toJSON(arrayList).toString();
        List<OutJson> jsonList = JSON.parseArray(jsonlistsrc, OutJson.class);
        return jsonList;
    }

    /**
     * 数据表列表页面
     */
    //    @RequiresPermissions("exchange:exDbStandard:list")
    @RequestMapping(value = { "listVue" })
    public String listVue(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        if ("".equals(exDbStandard.getOrgid()) || null == exDbStandard.getOrgid()) {
            //获取组织ID
            String id = getOrgid();
            exDbStandard.setOrgid(id);
        }
        //        List<ExDbStandard> list = exDbStandardService.findList(exDbStandard);
        Page<ExDbStandard> page = exDbStandardService.findPageCache(new Page<ExDbStandard>(request, response),
                exDbStandard);
        //Page<ExDbStandard> page = exDbStandardService.findPage(new Page<ExDbStandard>(request, response), exDbStandard);
        model.addAttribute("exTabId", exDbStandard.getExTabId());
        model.addAttribute("page", page);
        model.addAttribute("ExDbStandard", page.getList());
        exDbStandard.setOrderBy(TOTALDATE);
        total(exDbStandard, request, model);
        return "modules/exchange/exDbStandardListVue";
    }

    /**
     * 数据表列表页面
     */
    //RequiresPermissions("exchange:exDbStandard:select")
    @RequestMapping(value = { "select" })
    public String select(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            Model model) {
        Page<ExDbStandard> page = exDbStandardService.findPageCache(new Page<ExDbStandard>(request, response),
                exDbStandard);
        //Page<ExDbStandard> page = exDbStandardService.findPage(new Page<ExDbStandard>(request, response), exDbStandard);
        model.addAttribute("page", page);
        return "modules/exchange/exDbStandardSelect";
    }

    /**
     * 查看，增加，编辑数据表表单页面
     */
    @RequiresPermissions(value = { "exchange:exDbStandard:view", "exchange:exDbStandard:add",
            "exchange:exDbStandard:edit" }, logical = Logical.OR)
    @RequestMapping(value = "form")
    public String form(ExDbStandard exDbStandard, Model model, HttpServletRequest request,
            HttpServletResponse response) {
        model.addAttribute("action", request.getParameter("action"));
        model.addAttribute("exDbStandard", exDbStandard);
        model.addAttribute(OLDSEARCH, request.getParameter(OLDSEARCH));
        if (request.getParameter("ViewFormType") != null && "FormTwo".equals(request.getParameter("ViewFormType"))) {
            return "modules/exchange/exDbStandardFormTwo";
        }
        return "modules/exchange/exDbStandardForm";
    }

    /**
     * 保存数据表
     */
    @RequiresPermissions(value = { "exchange:exDbStandard:add", "exchange:exDbStandard:edit" }, logical = Logical.OR)
    @RequestMapping(value = "save")
    public String save(ExDbStandard exDbStandard, Model model, RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpServletResponse response) {
        if (!beanValidator(model, exDbStandard)) {
            return form(exDbStandard, model, request, response);
        }
        String id = getOrgid();
        //5 代表管理员权限
        exDbStandard.setOrgid(id);
        exDbStandardService.save(exDbStandard);
        addMessage(redirectAttributes, "保存数据表成功");
        return REDIRECT + Global.getAdminPath() + SAVEPATH + request.getParameter(OLDSEARCH);
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
     * 删除数据表
     */
    @RequiresPermissions("exchange:exDbStandard:del")
    @RequestMapping(value = "delete")
    public String delete(ExDbStandard exDbStandard, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        exDbStandardService.delete(exDbStandard);
        addMessage(redirectAttributes, DELETE_SUCCESS);
        return REDIRECT + Global.getAdminPath() + SAVEPATH + request.getParameter(OLDSEARCH);
    }

    /**
     * 删除数据表（逻辑删除，更新del_flag字段为1,在表包含字段del_flag时，可以调用此方法，将数据隐藏）
     */
    @RequiresPermissions(value = { "exchange:exDbStandard:del",
            "exchange:exDbStandard:delByLogic" }, logical = Logical.OR)
    @RequestMapping(value = "deleteByLogic")
    public String deleteByLogic(ExDbStandard exDbStandard, RedirectAttributes redirectAttributes) {
        exDbStandardService.deleteByLogic(exDbStandard);
        addMessage(redirectAttributes, DELETE_SUCCESS);
        return REDIRECT + Global.getAdminPath() + DELETE_PATH;
    }

    /**
     * 批量删除数据表
     */
    @RequiresPermissions("exchange:exDbStandard:del")
    @RequestMapping(value = "deleteAll")
    public String deleteAll(String ids, RedirectAttributes redirectAttributes) {
        String idArray[] = ids.split(",");
        for (String id : idArray) {
            exDbStandardService.delete(exDbStandardService.get(id));
        }
        addMessage(redirectAttributes, DELETE_SUCCESS);
        return REDIRECT + Global.getAdminPath() + DELETE_PATH;
    }

    /**
     * 批量删除数据表（逻辑删除，更新del_flag字段为1,在表包含字段del_flag时，可以调用此方法，将数据隐藏）
     */
    @RequiresPermissions(value = { "exchange:exDbStandard:del",
            "exchange:exDbStandard:delByLogic" }, logical = Logical.OR)
    @RequestMapping(value = "deleteAllByLogic")
    public String deleteAllByLogic(String ids, RedirectAttributes redirectAttributes) {
        String idArray[] = ids.split(",");
        for (String id : idArray) {
            exDbStandardService.deleteByLogic(exDbStandardService.get(id));
        }
        addMessage(redirectAttributes, DELETE_SUCCESS);
        return REDIRECT + Global.getAdminPath() + DELETE_PATH;
    }

    /**
     * 导出excel文件
     */
    @RequiresPermissions("exchange:exDbStandard:export")
    @RequestMapping(value = "export", method = RequestMethod.POST)
    public String exportFile(ExDbStandard exDbStandard, HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        try {
            String fileName = "数据表" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
            //             List<ExDbStandard> list = exDbStandardService.findList(exDbStandard);
            Page<ExDbStandard> page = exDbStandardService.findPage(new Page<ExDbStandard>(request, response, -1),
                    exDbStandard);
            new ExportExcel("数据表", ExDbStandard.class).setDataList(page.getList()).write(response, fileName).dispose();
            return null;
        } catch (Exception e) {
            addMessage(redirectAttributes, "导出数据表记录失败！失败信息：" + e.getMessage());
        }
        return REDIRECT + Global.getAdminPath() + PATH + exDbStandard.getExTabId();
    }

    /**
     * 导入Excel数据
    
     */
    @RequiresPermissions("exchange:exDbStandard:import")
    @RequestMapping(value = "import", method = RequestMethod.POST)
    public String importFile(MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request,
            @RequestParam(value = "exTabId") String exTabId) {
        try {
            int successNum = 0;
            ImportExcel ei = new ImportExcel(file, 1, 0);
            List<ExDbStandard> list = ei.getDataList(ExDbStandard.class);
            //            TestTree tree = new TestTree();
            for (ExDbStandard exDbStandard : list) {
                //添加逻辑
                String orgid = getOrgid();
                //                 tree.setOrgid(orgid);
                //                 tree.setName(exDbStandard.getName());
                //                List<TestTree> list1 = treeService.findList(tree);
                //                String id = list1.get(0).getId();
                exDbStandard.setOrgid(orgid);
                exDbStandard.setExTabId(exTabId);
                ExDbStandard exDbStandard1 = new ExDbStandard();
                exDbStandard1.setFieldName(exDbStandard.getFieldName());
                exDbStandard1.setExTabId(exTabId);
                List<ExDbStandard> list2 = exDbStandardService.findList(exDbStandard1);
                if (!list2.isEmpty()) {
                    exDbStandard.setId(list2.get(0).getId());
                    exDbStandardService.save(exDbStandard);
                } else {
                    exDbStandardService.save(exDbStandard);
                }
            }
            successNum = list.size();
            addMessage(redirectAttributes, "已成功导入 " + successNum + " 条数据表记录");
        } catch (Exception e) {
            addMessage(redirectAttributes, "导入数据表失败！失败信息：" + e.getMessage());
        }
        return REDIRECT + Global.getAdminPath() + PATH + exTabId;
    }

    /**
     * 下载导入数据表数据模板
     */
    @RequiresPermissions("exchange:exDbStandard:import")
    @RequestMapping(value = "import/template")
    public String importFileTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            String fileName = "数据表数据导入模板.xlsx";
            List<ExDbStandard> list = Lists.newArrayList();
            new ExportExcel("数据表数据", ExDbStandard.class, 1).setDataList(list).write(response, fileName).dispose();
            return null;
        } catch (Exception e) {
            addMessage(redirectAttributes, "导入模板下载失败！失败信息：" + e.getMessage());
        }
        return REDIRECT + Global.getAdminPath() + DELETE_PATH;
    }

}
