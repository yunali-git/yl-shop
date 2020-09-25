package com.baidu.shop.web;

import com.baidu.shop.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.websocket.server.PathParam;
import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Controller
//@RequestMapping(value = "/item")
public class PageController {
    //@Autowired
    private PageService pageService;

    //@GetMapping(value = "/{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap modelMap){

        Map<String,Object> map = pageService.getPageInfoSpuId(spuId);
        modelMap.putAll(map);

        return "item";
    }
}
