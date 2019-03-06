package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** RestController = Controller + ResponseBody */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    /** 分页查询品牌列表 */
    @RequestMapping("/findBrandPage")
    public PageResult findBrandPage(
            @RequestParam(name = "page", required = true, defaultValue = "1")Integer pageNum,
            @RequestParam(name = "size", required = true, defaultValue = "10")Integer pageSize) {

        return brandService.findBrandPage(pageNum, pageSize);
    }

    /** 分页调条件查询品牌列表 */
    @RequestMapping("/findBrandPageOnCondition")
    public PageResult findBrandPageOnCondition(
            @RequestBody TbBrand tbBrand,
            @RequestParam(name = "page", required = true, defaultValue = "1")Integer pageNum,
            @RequestParam(name = "size", required = true, defaultValue = "10")Integer pageSize) {

        return brandService.findBrandPage(tbBrand, pageNum, pageSize);
    }

    /** 新增品牌,前端传送的为一个json数据，所以需要使用@RequestBody注解进行json解析封装 */
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand tbBrand) {
        // 如果需要 这里可以写一点表单校验
        try {
            Integer res = brandService.addBrand(tbBrand);
            if (res > 0) {
                return new Result(true, "新增成功");
            } else {
                return new Result(false, "插入失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "服务端异常");
        }
    }

    /** 根据id查询一个品牌 */
    @RequestMapping("/findBrandById")
    public TbBrand findBrandById(Long id) {
        return brandService.findBrandById(id);
    }

    /** 修改品牌 */
    @RequestMapping("/updateBrand")
    public Result updateBrand(@RequestBody TbBrand tbBrand) {
        try {
            Integer res = brandService.updateBrand(tbBrand);
            System.out.println(res);
            return new Result(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "品牌更新失败");
        }
    }

    /** 删除品牌 */
    @RequestMapping("/deleteBrandByIds")
    public Result deleteBrandByIds(Long[] ids) {
        try {
            brandService.deleteBrandByIds(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /** 品牌下拉数据框 */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }
}
