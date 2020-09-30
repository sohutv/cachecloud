package com.sohu.cache.web.controller;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.options.MutableDataSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

@Controller
@RequestMapping("/wiki")
public class WikiController extends BaseController {

    @RequestMapping("/{path}/{filename}")
    public String subPages(@PathVariable String path, @PathVariable String filename,
                           @RequestParam(required = false) String entry,
                           Map<String, Object> map) throws Exception {
        String html = markdown2html(path + "/" + filename, ".md");
        //html = html.replace("${version}", "1.0.0");
        // toc
        String toc = markdown2html(path + "/" + filename, ".toc.md");
        if (toc != null) {
            map.put("toc", toc);
        }
        map.put("response", html);
        if (entry != null && entry.equals("client") && "client".equals(filename)) {
            return "wikiAccessClientTemplate";
        }

        return "wikiTemplate";
    }

    public String markdown2html(String filename, String suffix) throws Exception {
        String templatePath = "static/wiki/" + filename + suffix;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (inputStream == null) {
            return null;
        }
        String markdown = new String(read(inputStream));
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, Arrays.asList(new Extension[]{TablesExtension.create()}));
        Document document = Parser.builder(options).build().parse(markdown);
        String html = HtmlRenderer.builder(options).build().render(document);
        return html;
    }

}