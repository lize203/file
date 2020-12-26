package com.example.file.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;


@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

    String dirPath = "d:\\swlhy";

    @ResponseBody
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public Object uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return uploadFile(request, file);
    }

    /**
     * 上传文件
     *
     * @param file
     * @throws IllegalStateException
     * @throws IOException
     */
    private Object uploadFile(HttpServletRequest request, MultipartFile file) throws IllegalStateException, IOException {
        long startTime = System.currentTimeMillis();
        String originalFileName = file.getOriginalFilename();
        log.info("fileName：" + originalFileName);

        String dateString = DatePattern.PURE_DATE_FORMAT.format(new Date());

        int index = originalFileName.lastIndexOf('.') + 1;
        String suffix = originalFileName.substring(index);

        StringBuilder realName = new StringBuilder();
        realName.append(UUID.randomUUID().toString(true)).append(".").append(suffix);

        File file1 = new File(dirPath + File.separator + dateString);
        if (!file1.exists()) {
            file1.mkdirs();   //如果该目录不存在，就创建此抽象路径名指定的目录。
        }

        String path = dirPath + File.separator + dateString + File.separator + realName;
        File newFile = new File(path);
        if (newFile.exists()) {
            newFile.delete();
        }
        //通过CommonsMultipartFile的方法直接写文件
        file.transferTo(newFile);
        long endTime = System.currentTimeMillis();
        System.out.println("运行时间：" + String.valueOf(endTime - startTime) + "ms");
        //		return GlobalConstant.SUCCESS;
        JSONObject j = new JSONObject();
//        String filePath = dateString + realName;
        String filePath = "http://192.168.1.164:8886/swlhy/" + dateString + "/" + realName;
        j.put("filePath", filePath);
        return j;
    }

    /**
     * code 1 下载成功   -1 文件不存在   0  下载失败
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/downloadFile")
    public Object downloadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int BUFFER_SIZE = 1024 * 1024 * 10;
        InputStream in = null;
        OutputStream out = null;
        JSONObject j = new JSONObject();
        String fileName = request.getParameter("filePath");
        if (StringUtils.isBlank(fileName)) {
            j.put("msg", "找不到文件");
            return j;
        }
        try {
            fileName = fileName.substring(0, 8) + File.separator + fileName.substring(8);
            File file = new File(dirPath + File.separator + fileName);
            if (!file.exists()) {
                j.put("msg", "找不到文件");
                return j;
            }

            response.setCharacterEncoding("ISO-8859-1");
            response.addHeader("Content-Length", "" + file.length());
            response.addHeader("Accept-Ranges", "bytes");

//            String contentType = new MimetypesFileTypeMap().getContentType(file);
//            response.setContentType(contentType);
//            response.setContentType("application/*");
//            response.setContentType("application/octet-stream;charset=ISO-8859-1");
//            response.setContentType("application/x-download");
//            response.setContentType("application/*");
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
            if (iterator.hasNext()) {
                response.addHeader("Content-Type", "image/jpeg");
            } else {
                response.addHeader("Content-Disposition", "attachment;filename="
                        + new String(fileName.getBytes(), "ISO-8859-1"));
            }
            int i = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
            out = new BufferedOutputStream(response.getOutputStream());
            while ((i = in.read(buffer)) > 0) {
                out.write(buffer, 0, i);
            }
            in.close();
            out.flush();
            out.close();
            return null;
        } catch (IOException ex) {
            j.put("msg", "下载异常");
        }
        return j;
    }
}
